package su.nightexpress.ama.api.arena.region;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.manager.ConfigHolder;
import su.nexmedia.engine.api.manager.ICleanable;
import su.nexmedia.engine.api.manager.IEditable;
import su.nightexpress.ama.api.arena.config.IArenaObject;
import su.nightexpress.ama.api.arena.config.IProblematic;
import su.nightexpress.ama.api.arena.game.ArenaGameEventType;
import su.nightexpress.ama.api.arena.game.IArenaGameEventListener;
import su.nightexpress.ama.api.arena.game.IArenaGameEventTrigger;
import su.nightexpress.ama.api.arena.game.event.ArenaGameEventEvent;
import su.nightexpress.ama.api.arena.region.event.ArenaRegionEvent;
import su.nightexpress.ama.api.arena.type.ArenaLockState;
import su.nightexpress.ama.api.ArenaCuboid;
import su.nightexpress.ama.arena.ArenaPlayer;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public interface IArenaRegion extends IArenaGameEventListener, IArenaObject, ConfigHolder, IProblematic, IEditable, ICleanable {

    String PLACEHOLDER_FILE = "%region_file%";
    String PLACEHOLDER_ID = "%region_id%";
    String PLACEHOLDER_NAME = "%region_name%";
    String PLACEHOLDER_ACTIVE = "%region_active%";
    String PLACEHOLDER_DEFAULT = "%region_default%";
    String PLACEHOLDER_STATE = "%region_state%";
    String PLACEHOLDER_HOLOGRAM_ENABLED = "%region_hologram_enabled%";
    String PLACEHOLDER_TRIGGERS_LOCKED = "%region_triggers_locked%";
    String PLACEHOLDER_TRIGGERS_UNLOCKED = "%region_triggers_unlocked%";

    @Override
    @NotNull
    default UnaryOperator<String> replacePlaceholders() {
        String format = plugin().lang().Arena_Game_Trigger_Format_Full.getMsg();

        return str -> {
            str = IArenaGameEventListener.super.replacePlaceholders().apply(str);
            str = IProblematic.super.replacePlaceholders().apply(str);
            return str
                    .replace(PLACEHOLDER_TRIGGERS_LOCKED, this.getStateTriggers(ArenaLockState.LOCKED).stream()
                            .map(trigger -> trigger.replacePlaceholders().apply(format))
                            .collect(Collectors.joining("\n")))
                    .replace(PLACEHOLDER_TRIGGERS_UNLOCKED, this.getStateTriggers(ArenaLockState.UNLOCKED).stream()
                            .map(trigger -> trigger.replacePlaceholders().apply(format))
                            .collect(Collectors.joining("\n")))
                    .replace(PLACEHOLDER_FILE, this.getFile().getName())
                    .replace(PLACEHOLDER_ID, this.getId())
                    .replace(PLACEHOLDER_NAME, this.getName())
                    .replace(PLACEHOLDER_ACTIVE, this.plugin().lang().getBool(this.isActive()))
                    .replace(PLACEHOLDER_DEFAULT, this.plugin().lang().getBool(this.isDefault()))
                    .replace(PLACEHOLDER_STATE, this.plugin().lang().getEnum(this.getState()))
                    .replace(PLACEHOLDER_HOLOGRAM_ENABLED, plugin().lang().getBool(this.isHologramStateEnabled()));
        };
    }

    @Override
    default boolean onGameEvent(@NotNull ArenaGameEventEvent gameEvent) {
        if (!this.isReady(gameEvent)) return false;

        ArenaLockState state = this.getState().getOpposite();
        this.setState(state);

        ArenaGameEventType eventType = state == ArenaLockState.LOCKED ? ArenaGameEventType.REGION_LOCKED : ArenaGameEventType.REGION_UNLOCKED;
        ArenaRegionEvent regionEvent = new ArenaRegionEvent(gameEvent.getArena(), eventType, this);
        plugin().getPluginManager().callEvent(regionEvent);

        return true;
    }

    @NotNull
    default Set<ArenaPlayer> getPlayers() {
        return this.getArena().getPlayersIngame().stream().filter(arenaPlayer -> {
            return this.equals(arenaPlayer.getRegion(false));
        }).collect(Collectors.toSet());
    }

    @NotNull String getId();

    boolean isActive();

    void setActive(boolean isActive);

    boolean isDefault();

    void setDefault(boolean isDefault);

    @NotNull ArenaLockState getState();

    void setState(@NotNull ArenaLockState state);

    @NotNull
    Map<ArenaLockState, Set<IArenaGameEventTrigger>> getStateTriggers();

    @NotNull
    default Set<IArenaGameEventTrigger> getStateTriggers(@NotNull ArenaLockState state) {
        return this.getStateTriggers().computeIfAbsent(state, k -> new HashSet<>());
    }

    @NotNull String getName();

    void setName(@NotNull String name);

    @NotNull ArenaCuboid getCuboid();

    void setCuboid(@NotNull ArenaCuboid cuboid);

    Location getSpawnLocation();

    void setSpawnLocation(Location spawnLocation);

    @NotNull Set<String> getLinkedRegions();

    default void addLinkedRegion(@NotNull IArenaRegion region) {
        this.getLinkedRegions().add(region.getId());
        this.getArenaConfig().getRegionManager().getLinkedRegions(this).add(region);
    }

    default void removeLinkedRegion(@NotNull IArenaRegion region) {
        this.removeLinkedRegion(region.getId());
    }

    default void removeLinkedRegion(@NotNull String id) {
        this.getLinkedRegions().remove(id);
        this.getArenaConfig().getRegionManager().getLinkedRegions(this).removeIf(reg -> reg.getId().equalsIgnoreCase(id));
    }

    default void removeLinkedRegions() {
        this.getLinkedRegions().clear();
        this.getArenaConfig().getRegionManager().getLinkedRegions(this).clear();
    }

    @NotNull Map<String, Location> getMobSpawners();

    //void setMobSpawners(@NotNull Map<String, Location> mobSpawners);

    @Nullable
    default Location getMobSpawner(@NotNull String id) {
        return this.getMobSpawners().get(id.toLowerCase()).clone();
    }

    default boolean addMobSpawner(@NotNull Location location) {
        if (this.getMobSpawners().containsValue(location)) {
            return false;
        }

        Block block = location.getBlock();
        String id = "spawner_on_" + block.getType().name().toLowerCase() + "_";
        int count = 0;

        String idFinal = id + count;
        while (this.getMobSpawners().containsKey(idFinal)) {
            idFinal = id + (++count);
        }

        this.getMobSpawners().put(idFinal, location);
        return true;
    }

    @NotNull Set<IArenaRegionWave> getWaves();

    @NotNull Set<IArenaRegionContainer> getContainers();

    default void emptyContainers() {
        this.getContainers().forEach(container -> {
            container.getChest().getInventory().clear();
        });
    }

    boolean isHologramStateEnabled();

    void setHologramStateEnabled(boolean isHologramStateEnabled);

    void updateHologramState();

    void deleteHologramState();

    Location getHologramStateLocation();

    void setHologramStateLocation(Location hologramStateLocation);
}
