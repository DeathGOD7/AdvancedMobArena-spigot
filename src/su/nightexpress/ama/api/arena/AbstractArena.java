package su.nightexpress.ama.api.arena;

import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.CollectionsUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.arena.game.IArenaGameEventListener;
import su.nightexpress.ama.api.arena.region.IArenaRegion;
import su.nightexpress.ama.api.arena.spot.IArenaSpot;
import su.nightexpress.ama.api.arena.type.ArenaState;
import su.nightexpress.ama.api.arena.wave.IArenaWaveUpcoming;
import su.nightexpress.ama.arena.ArenaPlayer;
import su.nightexpress.ama.arena.config.ArenaConfig;

import java.util.*;

public abstract class AbstractArena implements IArena {

    protected AMA         plugin;
    protected ArenaConfig config;
    protected ArenaState  state;
    protected Set<IArenaGameEventListener> gameEventListeners;

    protected int  lobbyTimeleft;
    protected long gameTimeleft;
    protected int  gameScore;

    protected Set<ArenaPlayer>  players;
    protected Set<LivingEntity> mobs;
    //protected Set<LivingEntity> bosses;
    protected Set<Item>         groundItems;

    protected int mobsTotalAmount;

    protected int gradualMobsTimer;
    protected int gradualMobsPrepare;
    protected int gradualMobsKilled;

    protected int waveNumber;
    protected int  waveNextTimeleft;
    protected Set<IArenaWaveUpcoming> waveUpcoming;
    protected Map<String, double[]> waveAmplificatorValues; // [Amount, Level]

    // TODO Final Arena Stats Most Damager, Killer, etc

    public AbstractArena(@NotNull ArenaConfig config)  {
        this.plugin = config.plugin();
        this.config = config;
        this.gameEventListeners = new LinkedHashSet<>();
        this.players = new HashSet<>();
        this.mobs = new HashSet<>();
        //this.bosses = new HashSet<>();
        this.groundItems = new HashSet<>();
        this.waveUpcoming = new HashSet<>();
        this.waveAmplificatorValues = new HashMap<>();

        //this.stop(EndType.FORCE);
        this.reset();
    }

    @Override
    public final void updateGameEventListeners() {
        this.getGameEventListeners().clear();
        this.getGameEventListeners().addAll(config.getRegionManager().getRegions().stream().filter(IArenaRegion::isActive).toList());
        config.getSpotManager().getSpots().stream().filter(IArenaSpot::isActive).forEach(spot -> {
            this.getGameEventListeners().addAll(spot.getStates().values());
        });
        this.getGameEventListeners().addAll(config.getShopManager().getProducts());
        config.getWaveManager().getWaves().values().forEach(arenaWave -> {
            this.getGameEventListeners().addAll(arenaWave.getAmplificators().values());
        });
        config.getRegionManager().getRegions().stream().filter(IArenaRegion::isActive).forEach(region -> {
            this.getGameEventListeners().addAll(region.getWaves());
            this.getGameEventListeners().addAll(region.getContainers());
        });
        this.getGameEventListeners().addAll(config.getRewardManager().getRewards());
        this.getGameEventListeners().addAll(config.getGameplayManager().getAutoCommands());
    }

    protected void reset() {
        this.updateGameEventListeners();
        this.killMobs();
        this.killItems();

        int gameTimeleft = this.getConfig().getGameplayManager().getTimeleft();

        this.setLobbyTimeleft(this.getConfig().getGameplayManager().getLobbyTime());
        this.setGameTimeleft(gameTimeleft > 0 ? gameTimeleft * 1000L * 60L : -1);
        this.setGameScore(0);

        this.setWaveNumber(0);
        this.setWaveNextTimeleft(this.getConfig().getWaveManager().getDelayFirst());
        this.getUpcomingWaves().clear();
        this.getWaveAmplificatorValues().clear();
        this.setMobsTotalAmount(0);
        //this.setLastBossAmount(0);

        this.gradualMobsTimer = 0;
        this.setGradualMobsKilled(0);
        this.gradualMobsPrepare = 0;

        this.emptyContainers();
        //this.refillContainers();

        this.setState(ArenaState.WAITING);
    }

    @Override
    @NotNull
    public AMA plugin() {
        return this.plugin;
    }

    @Override
    @NotNull
    public ArenaConfig getConfig() {
        return this.config;
    }

    @NotNull
    @Override
    public Set<IArenaGameEventListener> getGameEventListeners() {
        return this.gameEventListeners;
    }

    @Override
    @NotNull
    public ArenaState getState() {
        return this.state;
    }

    protected void setState(@NotNull ArenaState state) {
        this.state = state;
    }


    @Override
    public int getLobbyTimeleft() {
        return this.lobbyTimeleft;
    }

    @Override
    public void setLobbyTimeleft(int lobbyTimeleft) {
        this.lobbyTimeleft = Math.max(0, lobbyTimeleft);
    }

    @Override
    public long getGameTimeleft() {
        return this.gameTimeleft;
    }

    @Override
    public void setGameTimeleft(long gameTimeleft) {
        this.gameTimeleft = gameTimeleft;
    }

    @Override
    public int getGameScore() {
        return this.gameScore;
    }

    @Override
    public void setGameScore(int gameScore) {
        this.gameScore = gameScore;
    }


    @Override
    @NotNull
    public Set<ArenaPlayer> getPlayers() {
        return this.players;
    }

    @Override
    @NotNull
    public Set<LivingEntity> getMobs() {
        return this.mobs;
    }

    /*@Override
    @NotNull
    public Set<LivingEntity> getBosses() {
        return this.bosses;
    }*/

    @NotNull
    @Override
    public Set<Item> getGroundItems() {
        return groundItems;
    }

    @Override
    public int getMobsTotalAmount() {
        return this.mobsTotalAmount;
    }

    @Override
    public void setMobsTotalAmount(int mobsTotalAmount) {
        this.mobsTotalAmount = mobsTotalAmount;
    }

    @Override
    public int getWaveNumber() {
        return this.waveNumber;
    }

    protected void setWaveNumber(int waveNumber) {
        this.waveNumber = waveNumber;
    }

    @Override
    public int getWaveNextTimeleft() {
        return this.waveNextTimeleft;
    }

    @Override
    public void setWaveNextTimeleft(int waveNextTimeleft) {
        this.waveNextTimeleft = Math.max(0, waveNextTimeleft);
    }


    protected int getGradualMobsKilled() {
        return this.gradualMobsKilled;
    }

    protected void setGradualMobsKilled(int gradualMobsKilled) {
        this.gradualMobsKilled = gradualMobsKilled;
    }


    @Override
    @NotNull
    public Map<String, double[]> getWaveAmplificatorValues() {
        return this.waveAmplificatorValues;
    }

    @Override
    @NotNull
    public Set<IArenaWaveUpcoming> getUpcomingWaves() {
        return this.waveUpcoming;
    }

    /**
     * @param pos Top position.
     * @return Entry with player name and score for MOB_KILLS stat on the arena.
     */
    // TODO Add another stat type = InGameStatType
    // to display stats after the game, and some of them will be recorded to player global stats.
    @NotNull
    @Deprecated
    public ArenaPlayer getHighScore(int pos) {
        pos = Math.max(0, pos - 1);

        Map<ArenaPlayer, Integer> map = new HashMap<>();
        for (ArenaPlayer arenaPlayer : this.getPlayersIngame()) {
            map.put(arenaPlayer, arenaPlayer.getScore());
        }

        int count = 0;
        for (ArenaPlayer arenaPlayer : CollectionsUT.sortByValueUpDown(map).keySet()) {
            if (count == pos) {
                return arenaPlayer;
            }
        }

        return new ArrayList<>(this.getPlayersIngame()).get(0);
    }
}
