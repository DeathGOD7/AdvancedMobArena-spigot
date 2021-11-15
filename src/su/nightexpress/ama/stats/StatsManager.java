package su.nightexpress.ama.stats;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.*;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.manager.AbstractManager;
import su.nexmedia.engine.api.manager.ConfigHolder;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.hooks.Hooks;
import su.nexmedia.engine.hooks.external.citizens.CitizensHK;
import su.nexmedia.engine.manager.api.task.ITask;
import su.nexmedia.engine.utils.CollectionsUT;
import su.nexmedia.engine.utils.DataUT;
import su.nexmedia.engine.utils.LocUT;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.arena.IArena;
import su.nightexpress.ama.api.arena.config.IArenaConfig;
import su.nightexpress.ama.data.ArenaUser;
import su.nightexpress.ama.hooks.EHook;
import su.nightexpress.ama.hooks.external.traits.StatsTrait;
import su.nightexpress.ama.stats.command.HologramCommand;
import su.nightexpress.ama.stats.command.StatsCommand;

import java.util.*;

public class StatsManager extends AbstractManager<AMA> implements ConfigHolder {

	private JYML config;
	private int  updateInterval;
	private List<String> signFormat;
	
	private StatsPlayerMenu                                  menuStats;
	private Map<String, Map<StatType, Map<String, Integer>>> statsArena;
    private Map<StatType, Map<String, Integer>>              statsGlobal;
    private Set<Sign>                                        statSigns;
    
    private HologramHandler hologramHandler;
	private StatsTask statsTask;
	
	public static final NamespacedKey KEY_SIGN_TYPE = new NamespacedKey(AMA.getInstance(), "stats_sign_type");
	public static final NamespacedKey KEY_SIGN_ARENA = new NamespacedKey(AMA.getInstance(), "stats_sign_arena");
	public static final NamespacedKey KEY_SIGN_POSITION = new NamespacedKey(AMA.getInstance(), "stats_sign_position");
	
	private static final String EMPTY_NAME = "<?>";
	private static final Map.Entry<String, Integer> EMPTY_ENTRY = new AbstractMap.SimpleEntry<>(EMPTY_NAME, 0);

	public static final String PLACEHOLDER_TOP_POSITION = "%top_position%";
	public static final String PLACEHOLDER_TOP_NAME = "%top_name%";
	public static final String PLACEHOLDER_TOP_SCORE = "%top_score%";
	public static final String PLACEHOLDER_TOP_TYPE = "%top_type%";

	public StatsManager(@NotNull AMA plugin) {
		super(plugin);
	}
	
	@Override
	public void onLoad() {
		this.config = JYML.loadOrExtract(plugin, "/stats/settings.yml");
		this.menuStats = new StatsPlayerMenu(plugin, JYML.loadOrExtract(plugin, "/stats/gui.stats.yml"));
		
		this.statsArena = new HashMap<>();
		this.statsGlobal = new HashMap<>();
		this.updateInterval = config.getInt("Update_Interval", 600);
		this.signFormat = StringUT.color(config.getStringList("Sign_Format"));
		
		if (Hooks.hasPlugin(EHook.HOLOGRAPHIC_DISPLAYS)) {
			this.hologramHandler = new HologramHandler(this);
			this.hologramHandler.setup();
			
			this.plugin.getMainCommand().addSubCommand(new HologramCommand(this, this.hologramHandler));
		}
		
		this.statSigns = new HashSet<>();
		for (String locRaw : this.config.getStringList("Signs")) {
			Location location = LocUT.deserialize(locRaw);
			if (location == null) continue;
			
			Block block = location.getBlock();
			BlockState state = block.getState();
			if (!(state instanceof Sign)) continue;
			
			this.statSigns.add((Sign) state);
		}

		CitizensHK citizens = plugin.getCitizens();
		if (citizens != null) {
			citizens.registerTrait(this.plugin, StatsTrait.class);
		}

		this.addListener(new StatsListener(this));
		this.plugin.getMainCommand().addSubCommand(new StatsCommand(this));

		this.statsTask = new StatsTask();
		this.statsTask.start();
	}

	@Override
	public void onShutdown() {
		this.save();
		if (this.hologramHandler != null) {
			this.hologramHandler.shutdown();
			this.hologramHandler = null;
		}
		if (this.statsTask != null) {
			this.statsTask.stop();
			this.statsTask = null;
		}
		if (this.menuStats != null) {
			this.menuStats.clear();
			this.menuStats = null;
		}
		if (this.statSigns != null) {
			this.statSigns.clear();
		}
		if (this.statsArena != null) {
			this.statsArena.clear();
		}
		if (this.statsGlobal != null) {
			this.statsGlobal.clear();
		}
	}

	@Override
	@NotNull
	public JYML getConfig() {
		return config;
	}

	@Override
	public void onSave() {
		this.config.set("Signs", this.statSigns.stream().map(sign -> LocUT.serialize(sign.getLocation())).toList());
	}

	@NotNull
	public StatsPlayerMenu getMenuStats() {
		return menuStats;
	}

	public void update() {
		this.statsArena.clear();
		this.statsGlobal.clear();
		
		List<ArenaUser> users = plugin.getData().getUsers();
		
		for (IArena arena : plugin.getArenaManager().getArenas()) {
			String arenaId = arena.getId();
			Map<StatType, Map<String, Integer>> mapArenaType = new HashMap<>();
			
			for (StatType statType : StatType.values()) {
				Map<String, Integer> mapUserScore = new TreeMap<>();
				Map<String, Integer> mapGlobal = this.getGlobalMap(statType);
				
				for (ArenaUser user : users) {
					int score = user.getStats(statType, arenaId);
					mapUserScore.put(user.getName(), score);
				}
				mapArenaType.put(statType, CollectionsUT.sortByValueUpDown(mapUserScore));
				
				mapUserScore.forEach((name, score) -> {
					mapGlobal.merge(name, score, Integer::sum);
				});
				this.statsGlobal.put(statType, CollectionsUT.sortByValueUpDown(mapGlobal));
			}
			this.statsArena.put(arenaId, mapArenaType);
		}
		
		this.plugin.getServer().getScheduler().runTask(plugin, () -> {
			this.updateSigns();
			
			if (this.hologramHandler != null) {
				this.hologramHandler.update();
			}
		});
	}
	
	@NotNull
	public Map.Entry<String, Integer> getTopScore(@Nullable String arena, @NotNull StatType statType, int pos) {
		if (arena == null) return this.getTopScore(statType, pos);

		Map<StatType, Map<String, Integer>> mapType = this.statsArena.get(arena);
		if (mapType == null) return EMPTY_ENTRY;
		
		Map<String, Integer> mapScore = mapType.get(statType);
		if (mapScore == null) return EMPTY_ENTRY;
		
		return this.getTopScore(statType, pos, mapScore);
	}
	
	@NotNull
	public Map.Entry<String, Integer> getTopScore(@NotNull StatType statType, int pos) {
		return this.getTopScore(statType, pos, this.getGlobalMap(statType));
	}
	
	@NotNull
	public List<Map.Entry<String, Integer>> getTopScores(@Nullable String arena, @NotNull StatType statType, int amount) {
		if (arena == null) return this.getTopScores(statType, amount);

		Map<StatType, Map<String, Integer>> mapType = this.statsArena.get(arena);
		if (mapType == null) return Collections.emptyList();
		
		Map<String, Integer> mapScore = mapType.get(statType);
		if (mapScore == null) return Collections.emptyList();
		
		return this.getTopScores(statType, amount, mapScore);
	}
	
	@NotNull
	public List<Map.Entry<String, Integer>> getTopScores(@NotNull StatType statType, int amount) {
		return this.getTopScores(statType, amount, this.getGlobalMap(statType));
	}
	
	@NotNull
	private Map.Entry<String, Integer> getTopScore(@NotNull StatType statType, int pos, @NotNull Map<String, Integer> mapScore) {
		List<Map.Entry<String, Integer>> list = this.getTopScores(statType, pos + 1, mapScore);
		return (pos + 1) > list.size() ? EMPTY_ENTRY : list.get(pos - 1);
	}
	
	@NotNull
	private List<Map.Entry<String, Integer>> getTopScores(@NotNull StatType statType, int amount, @NotNull Map<String, Integer> mapScore) {
		int size = mapScore.size();
		return new ArrayList<>(mapScore.entrySet()).subList(0, Math.min(amount, size));
	}
	
	@NotNull
	private Map<String, Integer> getGlobalMap(@NotNull StatType statType) {
		return this.statsGlobal.getOrDefault(statType, new TreeMap<>());
	}
	
	void addSign(@NotNull Sign sign, @NotNull StatType type, int pos, @Nullable IArena arena) {
		DataUT.setData(sign, KEY_SIGN_TYPE, type.name());
		DataUT.setData(sign, KEY_SIGN_POSITION, pos);
		if (arena != null) DataUT.setData(sign, KEY_SIGN_ARENA, arena.getId());

		this.statSigns.add(sign);
		this.plugin.getServer().getScheduler().runTask(plugin, this::updateSigns);
		this.updateSigns();
	}

	private void updateSigns() {
		this.statSigns.removeIf(sign -> !(sign.getBlock().getState() instanceof Sign));
		this.statSigns.forEach(sign -> {
			String typeRaw = DataUT.getStringData(sign, KEY_SIGN_TYPE);
			StatType statType = typeRaw != null ? CollectionsUT.getEnum(typeRaw, StatType.class) : null;
			if (statType == null) return;
			
			int pos = DataUT.getIntData(sign, KEY_SIGN_POSITION);
			if (pos < 1) return;
			
			String arenaId = DataUT.getStringData(sign, KEY_SIGN_ARENA);
			IArena arena = arenaId != null ? plugin.getArenaManager().getArenaById(arenaId) : null;
			
			Map.Entry<String, Integer> holder = this.getTopScore(arenaId, statType, pos);
			
	    	List<String> text = new ArrayList<>(this.signFormat);
	    	text.replaceAll(line -> line
					.replace(IArenaConfig.PLACEHOLDER_NAME, arena != null ? arena.getConfig().getName() : "")
	    			.replace(PLACEHOLDER_TOP_POSITION, String.valueOf(pos))
	    			.replace(PLACEHOLDER_TOP_SCORE, String.valueOf(holder.getValue()))
	    			.replace(PLACEHOLDER_TOP_NAME, holder.getKey())
	    			.replace(PLACEHOLDER_TOP_TYPE, plugin.lang().getEnum(statType))
			);
	    	for (int line = 0; line < 4; line++) {
	    		sign.setLine(line, line >= text.size() ? "" : text.get(line));
	    	}
	    	sign.update(true);

	    	String skullOwner = holder.getKey().equals(EMPTY_NAME) ? "MHF_Question" : holder.getKey();
	    	Block blockUponSign = sign.getBlock().getRelative(BlockFace.UP);
			if (blockUponSign.getState() instanceof Skull skull) {
				skull.setOwner(skullOwner);
				skull.update();
			}

			if (sign.getBlockData() instanceof Directional directional) {
				Block blockSign = sign.getBlock().getRelative(directional.getFacing().getOppositeFace());
    			Block blockOnSignBlock = blockSign.getRelative(BlockFace.UP);
    			
    			// Skull skin on the block that holds Sign
    			if (blockOnSignBlock.getState() instanceof Skull skull) {
					skull.setOwner(skullOwner);
    				skull.update();
    			}
    			// NPC Skin for entity that are on block that holds Sign
    			else {
    				this.setLeaderSkin(blockOnSignBlock, skullOwner);
    			}
			}
		});
    }

	private void setLeaderSkin(@NotNull Block block, @NotNull String name) {
		if (!Hooks.hasPlugin(Hooks.CITIZENS)) return;

		Location location = block.getLocation();
		World world = block.getWorld();

		name = StringUT.color("&a" + name);
		if (name.length() > 16) name = name.substring(0, 16);

		for (Entity entity : world.getNearbyEntities(location, 0.25, 1, 0.25)) {
			if (!(entity instanceof Player || !Hooks.isNPC(entity))) continue;

			NPC npc = CitizensAPI.getNPCRegistry().getNPC(entity);
			npc.setName(name);
			npc.data().set(NPC.PLAYER_SKIN_UUID_METADATA, name);
			npc.data().set(NPC.PLAYER_SKIN_USE_LATEST, false);
			npc.despawn(DespawnReason.PENDING_RESPAWN);
			npc.spawn(npc.getStoredLocation());
			return;
		}
	}
	
	class StatsTask extends ITask<AMA> {
	    
	    public StatsTask() {
	    	super(StatsManager.this.plugin, StatsManager.this.updateInterval, true);
	    }
	    
	    @Override
	    public void action() {
	    	update();
	    }
	}
}
