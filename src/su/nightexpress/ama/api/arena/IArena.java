package su.nightexpress.ama.api.arena;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.manager.IPlaceholder;
import su.nexmedia.engine.utils.TimeUT;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.Perms;
import su.nightexpress.ama.api.arena.config.IArenaConfig;
import su.nightexpress.ama.api.arena.event.ArenaScoreChangeEvent;
import su.nightexpress.ama.api.arena.event.ArenaWaveCompleteEvent;
import su.nightexpress.ama.api.arena.game.ArenaGameEventType;
import su.nightexpress.ama.api.arena.game.ArenaGameTargetType;
import su.nightexpress.ama.api.arena.game.IArenaGameEventListener;
import su.nightexpress.ama.api.arena.game.event.ArenaGameEndEvent;
import su.nightexpress.ama.api.arena.game.event.ArenaGameEventEvent;
import su.nightexpress.ama.api.arena.region.IArenaRegion;
import su.nightexpress.ama.api.arena.type.ArenaState;
import su.nightexpress.ama.api.arena.type.EndType;
import su.nightexpress.ama.api.arena.wave.IArenaWaveUpcoming;
import su.nightexpress.ama.arena.ArenaPlayer;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface IArena extends IPlaceholder {

    String PLACEHOLDER_STATE = "%arena_state%";
    String PLACEHOLDER_PLAYERS = "%arena_players%";
    String PLACEHOLDER_PLAYERS_MAX = "%arena_players_max%";
    String PLACEHOLDER_MOBS = "%arena_mobs%";
    String PLACEHOLDER_MOBS_TOTAL = "%arena_mobs_total%";
    String PLACEHOLDER_WAVE_NUMBER = "%arena_wave_number%";
    String PLACEHOLDER_WAVE_NEXT_IN = "%arena_wave_next_in%";
    String PLACEHOLDER_TIMELEFT = "%arena_timeleft%";
    String PLACEHOLDER_SCORE = "%arena_score%";

    DateTimeFormatter FORMAT_TIMELEFT = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Override
    @NotNull
    default UnaryOperator<String> replacePlaceholders() {
        return str -> this.getConfig().replacePlaceholders().apply(str)
                .replace(PLACEHOLDER_STATE, plugin().lang().getEnum(this.getState()))
                .replace(PLACEHOLDER_PLAYERS, String.valueOf(this.getPlayers().size()))
                .replace(PLACEHOLDER_PLAYERS_MAX, String.valueOf(this.getConfig().getGameplayManager().getPlayerMaxAmount()))
                .replace(PLACEHOLDER_MOBS, String.valueOf(this.getMobs().size()))
                .replace(PLACEHOLDER_MOBS_TOTAL, String.valueOf(this.getMobsTotalAmount()))
                .replace(PLACEHOLDER_WAVE_NUMBER, String.valueOf(this.getWaveNumber()))
                .replace(PLACEHOLDER_WAVE_NEXT_IN, String.valueOf(this.getWaveNextTimeleft()))
                .replace(PLACEHOLDER_TIMELEFT, TimeUT.getLocalTimeOf(this.getGameTimeleft()).format(FORMAT_TIMELEFT))
                .replace(PLACEHOLDER_SCORE, String.valueOf(this.getGameScore()))
                ;
    }

    @NotNull AMA plugin();

    @NotNull IArenaConfig getConfig();

    @NotNull
    default String getId() {
        return this.getConfig().getId();
    }

    @NotNull ArenaState getState();

    default boolean hasPermission(@NotNull Player player) {
        return player.hasPermission(Perms.ARENA + this.getId());
    }

    default void stop(@NotNull EndType type) {
        if (this.getState() == ArenaState.INGAME) {
            ArenaGameEndEvent event = new ArenaGameEndEvent(this, type);
            plugin().getPluginManager().callEvent(event);
        }
        this.onArenaStop(type);
    }

    void onArenaStop(@NotNull EndType type);

    @NotNull Set<IArenaGameEventListener> getGameEventListeners();

    default void onArenaGameEvent(@NotNull ArenaGameEventEvent gameEvent) {
        this.getGameEventListeners().forEach(listener -> listener.onGameEvent(gameEvent));
    }

    void updateGameEventListeners();

    boolean canJoin(@NotNull Player player, boolean isMessage);

    boolean joinLobby(@NotNull ArenaPlayer arenaPlayer);

    void joinGame(@NotNull ArenaPlayer arenaPlayer);

    boolean joinSpectate(@NotNull Player player);


    default void tick() {
        this.tickLobby();
        this.tickGame();
    }

    void tickLobby();

    void tickGame();


    int getLobbyTimeleft();

    void setLobbyTimeleft(int lobbyTimeleft);

    long getGameTimeleft();

    void setGameTimeleft(long gameTimeleft);

    int getGameScore();

    @Deprecated
    void setGameScore(int gameScore);

    default void updateGameScore() {
        int oldScore = this.getGameScore();
        int totalScore = 0;
        for (ArenaPlayer arenaPlayer : this.getPlayersIngame()) {
            totalScore += arenaPlayer.getScore();
        }
        this.setGameScore(totalScore);

        ArenaGameEventType eventType = this.getGameScore() > oldScore ? ArenaGameEventType.SCORE_INCREASED : ArenaGameEventType.SCORE_DECREASED;
        ArenaScoreChangeEvent event = new ArenaScoreChangeEvent(this, eventType, oldScore, this.getGameScore());
        plugin().getPluginManager().callEvent(event);
    }


    @NotNull Set<ArenaPlayer> getPlayers();

    @NotNull
    default Set<ArenaPlayer> getPlayers(@NotNull ArenaGameTargetType targetType) {
        if (targetType == ArenaGameTargetType.PLAYER_ALL) return this.getPlayersIngame();
        if (targetType == ArenaGameTargetType.PLAYER_RANDOM) return Stream.of(Rnd.get(this.getPlayersIngame())).filter(Objects::nonNull).collect(Collectors.toSet());
        return Collections.emptySet();
    }

    @NotNull
    default Set<ArenaPlayer> getPlayersIngame() {
        return this.getPlayers().stream().filter(Predicate.not(ArenaPlayer::isLateJoined)).collect(Collectors.toSet());
    }

    @NotNull
    default Set<ArenaPlayer> getPlayersLate() {
        return this.getPlayers().stream().filter(ArenaPlayer::isLateJoined).collect(Collectors.toSet());
    }

    @Nullable
    default ArenaPlayer getPlayerRandom() {
        return Rnd.get(new ArrayList<>(this.getPlayersIngame()));
    }

    default void removePlayer(@NotNull ArenaPlayer arenaPlayer) {
        this.getPlayers().remove(arenaPlayer);
    }


    @NotNull Set<LivingEntity> getMobs();

    /*@Deprecated
    default void addMob(@NotNull LivingEntity mob) {
        if (mob.isValid() && !mob.isDead() && this.getMobs().add(mob)) {
            //this.setLastMobsAmount(this.getLastMobsAmount() + 1);
        }
    }

    @Deprecated
    default boolean removeMob(@NotNull LivingEntity mob) {
        if (!this.getMobs().remove(mob)) return false;

        //this.updateGradualMobsAmount();
        return true;
    }

    @NotNull Set<LivingEntity> getBosses();

    @Deprecated
    default void addBoss(@NotNull LivingEntity boss) {
        if (boss.isValid() && !boss.isDead() && this.getBosses().add(boss)) {
            //this.setLastBossAmount(this.getLastBossAmount() + 1);
        }
    }

    @Deprecated
    default boolean removeBoss(@NotNull LivingEntity boss) {
        if (!this.getBosses().remove(boss)) return false;

        for (ArenaPlayer arenaPlayer : this.getPlayersIngame()) {
            arenaPlayer.removeMobHealthBar(boss);
        }

        //this.updateGradualMobsAmount();
        return true;
    }*/

    @Nullable
    default LivingEntity getMobRandom() {
        List<LivingEntity> list = new ArrayList<>();
        list.addAll(this.getMobs());
        //list.addAll(this.getBosses());
        return Rnd.get(list);
    }

    void updateMobTarget(@NotNull LivingEntity entity, boolean force);

    default void killMobs() {
        new HashSet<>(this.getMobs()).forEach(mob -> mob.setHealth(0));
        //this.getBosses().forEach(Entity::remove);

        this.getMobs().clear();
        //this.getBosses().clear();
    }

    @NotNull Set<Item> getGroundItems();

    default void addGroundItem(@NotNull Item item) {
        this.getGroundItems().removeIf(item2 -> !item2.isValid());
        this.getGroundItems().add(item);
    }

    default void killItems() {
        this.getGroundItems().forEach(Entity::remove);
        this.getGroundItems().clear();
    }

    int getMobsTotalAmount();

    void setMobsTotalAmount(int mobsTotalAmount);


    int getWaveNumber();

    int getWaveNextTimeleft();

    void setWaveNextTimeleft(int waveNextTimeleft);

    default boolean isNextWaveAllowed() {
        return this.getWaveNumber() < 1 || (this.getMobs().isEmpty() /*&& this.getBosses().isEmpty()*/ && this.getUpcomingWaves().isEmpty());
    }

    default boolean isLatestWave() {
        return this.getWaveNumber() == this.getConfig().getWaveManager().getFinalWave();
    }

    @NotNull Set<IArenaWaveUpcoming> getUpcomingWaves();

    void newWave();

    void spawnMobs(double percent);

    default void skipWave() {
        this.getUpcomingWaves().clear();
        this.killMobs();

        // Call an event that will call arena region and spot triggers.
        ArenaWaveCompleteEvent event = new ArenaWaveCompleteEvent(this);
        plugin().getPluginManager().callEvent(event);

        this.setWaveNextTimeleft(1);
    }

    @NotNull Map<String, double[]> getWaveAmplificatorValues();

    default double[] getWaveAmplificatorValues(@NotNull String waveId) {
        return this.getWaveAmplificatorValues().computeIfAbsent(waveId.toLowerCase(), d -> new double[2]);
    }

    default double getWaveAmplificatorAmount(@NotNull String waveId) {
        return this.getWaveAmplificatorValues(waveId)[0];
    }

    default void addWaveAmplificatorAmount(@NotNull String waveId, int amount) {
        this.getWaveAmplificatorValues(waveId)[0] += amount;
    }

    default double getWaveAmplificatorLevel(@NotNull String waveId) {
        return this.getWaveAmplificatorValues(waveId)[1];
    }

    default void addWaveAmplificatorLevel(@NotNull String waveId, int amount) {
        this.getWaveAmplificatorValues(waveId)[1] += amount;
    }

    default void emptyContainers() {
        this.getConfig().getRegionManager().getRegions().forEach(IArenaRegion::emptyContainers);
    }

    /**
     * @param pos Top position.
     * @return Entry with player name and score for MOB_KILLS stat on the arena.
     */
    @NotNull
    @Deprecated
    ArenaPlayer getHighScore(int pos);
}
