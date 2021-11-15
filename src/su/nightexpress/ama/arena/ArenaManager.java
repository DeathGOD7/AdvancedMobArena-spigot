package su.nightexpress.ama.arena;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.manager.AbstractManager;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.manager.api.task.ITask;
import su.nexmedia.engine.utils.*;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.arena.IArena;
import su.nightexpress.ama.api.arena.event.ArenaPlayerJoinEvent;
import su.nightexpress.ama.api.arena.event.ArenaPlayerLeaveEvent;
import su.nightexpress.ama.api.arena.type.ArenaState;
import su.nightexpress.ama.api.arena.type.EndType;
import su.nightexpress.ama.api.arena.type.LeaveReason;
import su.nightexpress.ama.arena.config.ArenaConfig;
import su.nightexpress.ama.arena.listener.ArenaListenerGameplay;
import su.nightexpress.ama.arena.listener.ArenaListenerGeneric;
import su.nightexpress.ama.config.Config;
import su.nightexpress.ama.stats.StatType;

import java.io.File;
import java.util.*;

public class ArenaManager extends AbstractManager<AMA> {

    private Map<String, IArena>      arenas;
    private Map<UUID, ArenaPlayer>    players;
    
    private JYML configSigns;
	private Set<Sign> arenaJoinSigns;
    private Map<String, Map.Entry<String, List<String>>> scoreboards;
    
    private ArenaListMenu arenaListMenu;
	private ArenaTask     arenaTask;

	public static final String DIR_ARENAS = "/arenas/";

	public static final NamespacedKey KEY_LOBBY_ITEM = new NamespacedKey(AMA.getPlugin(AMA.class), "lobby_item_type");
	public static final NamespacedKey KEY_SIGN_JOIN = new NamespacedKey(AMA.getPlugin(AMA.class), "arena_sign_arena");
	public static final NamespacedKey KEY_SIGN_READY = new NamespacedKey(AMA.getPlugin(AMA.class), "sign_arena_player_ready");

    public ArenaManager(@NotNull AMA plugin) {
    	super(plugin);
    }
    
    @Override
	public void onLoad() {
    	this.arenas = new HashMap<>();
    	this.players = new HashMap<>();
    	
        for (File folder : FileUT.getFolders(plugin.getDataFolder() + DIR_ARENAS)) {
        	JYML cfg = new JYML(folder.getAbsolutePath(), folder.getName() + ".yml");
        	ArenaConfig config = new ArenaConfig(plugin, cfg);
        	
        	this.arenas.put(config.getId(), config.getArena());
        	if (config.hasProblems()) {
        		this.plugin.warn("Arena '" + config.getId() + "' contains some problems! Arena disabled until all problems are fixed.");
			}
        }
        plugin.info("Arenas Loaded: " + arenas.size());


		JYML configScoreboard = JYML.loadOrExtract(plugin, DIR_ARENAS + "scoreboard.yml");
        this.scoreboards = new HashMap<>();
        for (String arenaId : configScoreboard.getSection("")) {
        	String path = arenaId + ".";
        	String title = StringUT.color(configScoreboard.getString(path + "Title", ""));
        	List<String> lines = StringUT.color(configScoreboard.getStringList(path + "List"));
        	Map.Entry<String, List<String>> entry = new AbstractMap.SimpleEntry<>(title, lines);
        	
        	this.scoreboards.put(arenaId.toLowerCase(), entry);
        }
        
        
        this.arenaJoinSigns = new HashSet<>();
        this.configSigns = new JYML(plugin.getDataFolder() + DIR_ARENAS, "signs.yml");
        for (String locRaw : this.configSigns.getStringList("Join")) {
        	Location loc = LocUT.deserialize(locRaw);
        	if (loc == null) continue;
        	
        	Block block = loc.getBlock();
        	BlockState state = block.getState();
        	if (!(state instanceof Sign sign)) continue;

			this.arenaJoinSigns.add(sign);
        }
        this.getArenas().forEach(this::updateSigns);

        this.addListener(new ArenaListenerGeneric(this));
        this.addListener(new ArenaListenerGameplay(this));
        
        this.arenaTask = new ArenaTask();
        this.arenaTask.start();
    }
    
    @Override
	public void onShutdown() {
		this.arenas.values().forEach(arena -> arena.stop(EndType.FORCE));
		this.arenas.clear();

    	if (this.arenaTask != null) {
    		this.arenaTask.stop();
    		this.arenaTask = null;
    	}
    	if (this.arenaListMenu != null) {
    		this.arenaListMenu.clear();
    		this.arenaListMenu = null;
    	}
    	
    	if (this.arenaJoinSigns != null) {
    		List<String> list = new ArrayList<>();
    		this.arenaJoinSigns.forEach(sign -> list.add(LocUT.serialize(sign.getLocation())));
    		this.arenaJoinSigns.clear();
    		
    		this.configSigns.set("Join", list);
	    	this.configSigns.saveChanges();
    	}

    	if (this.players != null) {
    		this.players.clear();
    		this.players = null;
    	}
    	if (this.scoreboards != null) {
    		this.scoreboards.clear();
    		this.scoreboards = null;
    	}
    }

    @NotNull
	public ArenaListMenu getArenaListMenu() {
    	if (this.arenaListMenu == null) {
    		this.arenaListMenu = new ArenaListMenu(this.plugin);
		}
		return arenaListMenu;
	}
    
    @Nullable
    public Map.Entry<String, List<String>> getScoreboard(@NotNull String arenaId) {
    	return this.scoreboards.getOrDefault(arenaId, this.scoreboards.get(Constants.DEFAULT));
    }
    
    public boolean joinLobby(@NotNull Player player, @NotNull IArena arena) {
        if (this.isPlaying(player)) {
        	plugin.lang().Arena_Join_Error_InGame.send(player);
        	return false;
        }
        if (!arena.canJoin(player, true)) {
        	return false;
        }
        
        ArenaPlayer arenaPlayer = new ArenaPlayer(player, arena);
        
        // Call custom plugin event
        ArenaPlayerJoinEvent event = new ArenaPlayerJoinEvent(arena, arenaPlayer);
        plugin.getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;
        
        // Prepare player to the arena.
        // Clear and save all player su.nexmedia.engine.api.effects and items.
        PlayerSnapshot.doSnapshot(arenaPlayer);
        
        if (arena.joinLobby(arenaPlayer)) {
        	this.players.put(player.getUniqueId(), arenaPlayer);
	        this.updateSigns(arena);
	        return true;
        }
        
        this.leaveArena(arenaPlayer, LeaveReason.KICK);
        return false;
    }
    
    public boolean joinSpectate(@NotNull Player player, @NotNull IArena arena) {
		if (this.isPlaying(player)) {
	    	plugin.lang().Arena_Join_Error_InGame.send(player);
	    	return false;
		}
	    
	    return arena.joinSpectate(player);
	}

	public void leaveArena(@NotNull ArenaPlayer arenaPlayer, @NotNull LeaveReason reason) {
	    IArena arena = arenaPlayer.getArena();
	    Player player = arenaPlayer.getPlayer();
	    player.closeInventory(); 		 		// In case if playe have opened any arena GUIs.
	    
	    arenaPlayer.delBoard(); 				// Remove scoreboard.
	    arenaPlayer.removeMobHealthBars();	 	// Remove boss bars.
	    arena.removePlayer(arenaPlayer);			// Remove player from the arena.
	    reason.sendMessage(player);		 		// Send titles depends on reason.

	    if (arena.getState() == ArenaState.INGAME) {
	    	if (reason == LeaveReason.FINISH) {
	    		arenaPlayer.addStats(StatType.GAMES_WON, 1);
	    	}
	    	else {
	        	if (reason == LeaveReason.DEATH) {
	        		if (!arena.getConfig().getRewardManager().isRetainOnDeath()) {
	                    arenaPlayer.getRewards().clear();
	                }
	        	}
	        	else if (reason != LeaveReason.TIMELEFT) {
	        		if (!arena.getConfig().getRewardManager().isRetainOnLeave()) {
	                    arenaPlayer.getRewards().clear();
	                }
	        	}
	        	arenaPlayer.addStats(StatType.GAMES_LOST, 1);
	    	}
	        if (arena.getWaveNumber() > 0) {
	        	arenaPlayer.addStats(StatType.GAMES_PLAYED, 1);
	        }
	        MsgUT.sound(player, Config.SOUND_GAME_END);
	    }
	    
	    // Restore player data before the game.
	    PlayerSnapshot.restore(arenaPlayer);
	    
	    // Save stats, give rewards.
	    arenaPlayer.saveStats();
	 	arenaPlayer.getRewards().forEach(reward -> reward.give(player));
	    
	    this.players.remove(player.getUniqueId());
	    
	    ArenaPlayerLeaveEvent event = new ArenaPlayerLeaveEvent(arena, arenaPlayer);
	    plugin.getPluginManager().callEvent(event);
	}

	public void delete(@NotNull IArena arena) {
		if (arena.getConfig().isActive()) {
			return;
		}
		if (FileUT.deleteRecursive(arena.getConfig().getFile().getParentFile())) {
			this.arenas.remove(arena.getId());
		}
		
		// Re-build GUI to apply arena changes (remove deleted arenas)
		if (this.arenaListMenu != null) {
    		this.arenaListMenu.clear();
    		this.arenaListMenu = null;
    	}
	}

	public boolean isArenaExists(@NotNull String id) {
		return this.getArenaById(id) != null;
	}

	public boolean isPlaying(@NotNull Player player) {
		return this.getArenaPlayer(player) != null;
	}

	@NotNull
	public Collection<IArena> getArenas() {
		return this.getArenasMap().values();
	}

	@NotNull
	public Map<String, IArena> getArenasMap() {
		return this.arenas;
	}

	@Nullable
	public IArena getArenaAtLocation(@NotNull Location loc) {
		return this.getArenas().stream()
				.filter(arena -> arena.getConfig().getRegionManager().getRegions().stream().anyMatch(reg -> reg.getCuboid().contains(loc)))
				.findFirst().orElse(null);
	}
	
	@Nullable
	public IArena getArenaById(@NotNull String id) {
		return this.getArenasMap().get(id.toLowerCase());
	}

	@NotNull
    public List<String> getArenaIds() {
    	return new ArrayList<>(this.getArenasMap().keySet());
    }
	
	@NotNull
	public Collection<ArenaPlayer> getArenaPlayers() {
    	return this.players.values();
    }
    
	@Nullable
    public ArenaPlayer getArenaPlayer(@NotNull Player player) {
    	return this.players.get(player.getUniqueId());
    }
    
    public void addJoinSign(@NotNull IArena arena, @NotNull Sign sign) {
    	DataUT.setData(sign, KEY_SIGN_JOIN, arena.getId());
    	
    	this.arenaJoinSigns.add(sign);
    	this.plugin.getServer().getScheduler().runTask(plugin, () -> {
    		this.updateSigns(arena);
    	});
    }

    public void updateSigns(@NotNull IArena arena) {
    	List<String> text = new ArrayList<>(Config.SIGNS_JOIN_FORMAT);
    	text.replaceAll(arena.replacePlaceholders());
    	
    	this.arenaJoinSigns.forEach(sign -> {
    		String arenaId = DataUT.getStringData(sign, KEY_SIGN_JOIN);
    		if (arenaId == null || !arenaId.equals(arena.getId())) return;
    		
    		for (int line = 0; line < 4; line++) {
    			sign.setLine(line, line >= text.size() ? "" : text.get(line));
    		}
    		sign.update(true);
    	});
    }
	
	class ArenaTask extends ITask<AMA> {
		
		public ArenaTask() {
			super(ArenaManager.this.plugin, 1, false);
		}
		
		@Override
		public void action() {
			getArenas().stream().filter(arena -> arena.getConfig().isActive() && !arena.getConfig().hasProblems())
					.forEach(IArena::tick);
		}
	}
}
