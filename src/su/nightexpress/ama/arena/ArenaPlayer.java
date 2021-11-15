package su.nightexpress.ama.arena;

import org.bukkit.Location;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.manager.IPlaceholder;
import su.nexmedia.engine.utils.TimeUT;
import su.nightexpress.ama.api.arena.IArena;
import su.nightexpress.ama.api.arena.event.ArenaPlayerReadyEvent;
import su.nightexpress.ama.api.arena.region.IArenaRegion;
import su.nightexpress.ama.api.arena.reward.IArenaReward;
import su.nightexpress.ama.api.arena.type.ArenaLockState;
import su.nightexpress.ama.api.arena.type.ArenaState;
import su.nightexpress.ama.api.kits.IArenaKit;
import su.nightexpress.ama.config.Config;
import su.nightexpress.ama.data.ArenaUser;
import su.nightexpress.ama.stats.StatType;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.UnaryOperator;

public class ArenaPlayer implements IPlaceholder {

	public static final String PLACEHOLDER_NAME         = "%player_name%";
	public static final String PLACEHOLDER_LIVES        = "%player_lives%";
	public static final String PLACEHOLDER_STREAK       = "%player_streak%";
	public static final String PLACEHOLDER_STREAK_DECAY = "%player_streak_decay%";
	public static final String PLACEHOLDER_KILLS = "%player_kills%";
	public static final String PLACEHOLDER_SCORE = "%player_score%";
	public static final String PLACEHOLDER_BALANCE = "%player_balance%";
	public static final String PLACEHOLDER_IS_READY = "%player_is_ready%";

	private static final DateTimeFormatter FORMAT_STREAK = DateTimeFormatter.ofPattern("ss");

	private final Player       player;
    private final IArena       arena;
    private IArenaKit           kit;
    private List<IArenaReward> rewards;
    private int                lives;
    private int                score;
    private int                killStreak;
    private long             killStreakDecay;
    private boolean          isLateJoined;
    private boolean isReady;
	private ArenaBoard board;

    private final Map<StatType, Integer> stats;
    private final Map<UUID, BossBar>     mobHealthBar;
    
    public ArenaPlayer(@NotNull Player player, @NotNull IArena arena) {
		this.stats = new HashMap<>();
		this.mobHealthBar = new HashMap<>();
    	this.player = player;
        this.arena = arena;
        this.setKit(null);
        this.setRewards(new ArrayList<>());
        this.setLives(arena.getConfig().getGameplayManager().getPlayerLivesAmount());
        this.setScore(0);
        this.setKillStreak(0);
        this.setLateJoined(arena.getState() == ArenaState.INGAME);
        this.isReady = false;
    }

	@Override
	@NotNull
	public UnaryOperator<String> replacePlaceholders() {
		return str -> str
				.replace(PLACEHOLDER_NAME, this.getPlayer().getName())
				.replace(PLACEHOLDER_LIVES, String.valueOf(this.getLives()))
				.replace(PLACEHOLDER_STREAK, String.valueOf(this.getKillStreak()))
				.replace(PLACEHOLDER_STREAK_DECAY, TimeUT.getLocalTimeOf(this.getKillStreakDecay()).format(FORMAT_STREAK))
				.replace(PLACEHOLDER_SCORE, String.valueOf(this.getScore()))
				.replace(PLACEHOLDER_KILLS, String.valueOf(this.getStats(StatType.MOB_KILLS)))
				.replace(PLACEHOLDER_BALANCE, String.valueOf(arena.plugin().getEconomy().getBalance(getPlayer())))
				.replace(PLACEHOLDER_IS_READY, arena.plugin().lang().getBool(this.isReady()))
				;
	}

	@NotNull
    public IArena getArena() {
    	return this.arena;
    }
    
    @Nullable
    public IArenaKit getKit() {
        return this.kit;
    }
    
    public void setKit(@Nullable IArenaKit kit) {
        this.kit = kit;
    }
    
    @NotNull
    public List<IArenaReward> getRewards() {
        return this.rewards;
    }
    
    public void setRewards(@NotNull List<IArenaReward> rewards) {
        this.rewards = rewards;
    }
    
    @NotNull
    public Player getPlayer() {
    	return this.player;
    }
    
    public int getLives() {
    	return this.lives;
    }
    
    public void setLives(int lives) {
    	this.lives = Math.max(0, lives);
    }
    
    public void takeLive() {
    	this.setLives(this.getLives() - 1);
    }
    
    public boolean isLateJoined() {
		return isLateJoined;
	}
    
    public void setLateJoined(boolean isLateJoined) {
		this.isLateJoined = isLateJoined;
	}

	public boolean isReady() {
		return isReady;
	}

	public void setReady(boolean ready) {
		isReady = ready;

		ArenaPlayerReadyEvent readyEvent = new ArenaPlayerReadyEvent(this.getArena(), this);
		this.getArena().plugin().getPluginManager().callEvent(readyEvent);
	}

	@Nullable
    public BossBar getMobHealthBar(@NotNull LivingEntity mob) {
    	return this.getMobHealthBar(mob.getUniqueId());
    }
    
    @Nullable
    public BossBar getMobHealthBar(@NotNull UUID id) {
    	return mobHealthBar.get(id);
    }
    
    public void addMobHealthBar(@NotNull LivingEntity boss, @NotNull BossBar bar) {
    	if (this.getMobHealthBar(boss) != null) return;
    	
    	bar.addPlayer(this.getPlayer());
    	this.mobHealthBar.put(boss.getUniqueId(), bar);
    }
    
    public void removeMobHealthBar(@NotNull LivingEntity boss) {
    	BossBar bar = this.getMobHealthBar(boss);
    	if (bar == null) return;
    	
    	bar.removePlayer(this.getPlayer());
    	this.mobHealthBar.remove(boss.getUniqueId());
    }
    
    public void removeMobHealthBars() {
    	this.mobHealthBar.values().forEach(bar -> bar.removePlayer(this.getPlayer()));
    	this.mobHealthBar.clear();
    }
    
    public void addBoard() {
    	if (!this.arena.getConfig().getGameplayManager().isScoreboardEnabled()) return;
        
    	Map.Entry<String, List<String>> en = arena.plugin().getArenaManager().getScoreboard(arena.getId());
    	if (en == null) return;
    	
    	this.board = new ArenaBoard(this, en);
    }
    
	public void delBoard() {
		if (this.board == null) return;
		
		this.board.remove();
	}

    public void tick() {
    	this.setKillStreakDecay(this.getKillStreakDecay() - 1000L);
    	if (this.getKillStreakDecay() == 0) {
    		this.setKillStreak(0);
    		this.getStats().remove(StatType.BEST_KILL_STREAK);
    	}
    	
    	if (this.kit != null) {
    		this.kit.applyPotionEffects(this.player);
    	}
    	if (this.board != null) {
    		this.board.update();
    	}
    }
    
	/**
     * @return Returns ArenaRegion where player is.
     */
    @Nullable
    public IArenaRegion getRegion(boolean includeNearby) {
    	Location loc = this.getPlayer().getLocation();
    	
    	IArenaRegion region = this.getArena().getConfig().getRegionManager().getRegion(loc);
    	if (region != null || !includeNearby) {
    		return region;
    	}

    	// Get nearest region to player if he is outside of any
		return this.getArena().getConfig().getRegionManager().getRegions().stream()
				.filter(reg -> reg.getState() == ArenaLockState.UNLOCKED)
				.min((r1, r2) -> (int) (loc.distance(r1.getSpawnLocation()) - loc.distance(r2.getSpawnLocation())))
				.orElse(null);
    }
    
    public int getScore() {
		return score;
	}
    
    public void setScore(int score) {
		this.score = Math.max(0, score);
		this.getArena().updateGameScore();
	}
    
    public void addScore(int amount) {
    	this.setScore(this.getScore() + amount);
    }
    
    public int getKillStreak() {
		return killStreak;
	}
    
    public void setKillStreak(int killStreak) {
		this.killStreak = killStreak;
		if (this.getKillStreak() > 0) {
			this.setKillStreakDecay(Config.MOBS_KILL_STREAK_DECAY);
		}
	}
    
    public long getKillStreakDecay() {
		return killStreakDecay;
	}
    
    public void setKillStreakDecay(long killStreakDecay) {
		this.killStreakDecay = Math.max(0, killStreakDecay);
	}
    
    @NotNull
    public Map<StatType, Integer> getStats() {
    	return this.stats;
    }
    
    public int getStats(@NotNull StatType type) {
    	return this.stats.getOrDefault(type, 0);
    }
    
    public void addStats(@NotNull StatType type, int amount) {
    	this.stats.put(type, amount + this.getStats(type));
    }
    
	public void saveStats() {
    	String arena = this.getArena().getId();
    	Player player = this.getPlayer();
    	ArenaUser user = this.getArena().plugin().getUserManager().getOrLoadUser(player);
        if (user == null) return;
    	
    	Map<StatType, Integer> gameStats = this.getStats();
    	Map<StatType, Integer> userStats = user.getStats(arena);
    	
    	gameStats.forEach((stat, amount) -> {
    		userStats.merge(stat, amount, (oldVal, newVal) -> {
    			if (stat == StatType.WAVES_PASSED || stat == StatType.BEST_KILL_STREAK) {
    				return Math.max(oldVal, newVal);
    			}
    			return oldVal + newVal;
    		});
    	});
    }
}
