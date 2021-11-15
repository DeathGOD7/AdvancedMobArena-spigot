package su.nightexpress.ama.api.arena.region;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.manager.IEditable;
import su.nexmedia.engine.api.manager.ILoadable;
import su.nightexpress.ama.api.arena.config.IArenaObject;
import su.nightexpress.ama.api.arena.config.IProblematic;
import su.nightexpress.ama.api.arena.type.ArenaLockState;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public interface IArenaRegionManager extends IArenaObject, ILoadable, IEditable, IProblematic {

    void save();

    @NotNull Map<String, IArenaRegion> getRegionsMap();

    @NotNull Map<IArenaRegion, Set<IArenaRegion>> getLinkedRegionsMap();

    @NotNull
    default Collection<IArenaRegion> getRegions() {
        return this.getRegionsMap().values();
    }

    @Nullable
    default IArenaRegion getRegionDefault() {
        return this.getRegions().stream().filter(IArenaRegion::isDefault).findFirst().orElse(null);
    }

    @Nullable
    default IArenaRegion getRegionAnyAvailable() {
        return this.getRegions().stream().filter(reg -> reg.getState() == ArenaLockState.UNLOCKED).findFirst().orElse(null);
    }

    @Nullable
    default IArenaRegion getRegion(@NotNull String id) {
        return this.getRegionsMap().get(id.toLowerCase());
    }

    @Nullable
    default IArenaRegion getRegion(@NotNull Location location) {
        return this.getRegions().stream().filter(reg -> reg.getCuboid().contains(location)).findFirst().orElse(null);
    }

    default Set<IArenaRegion> getLinkedRegions(@NotNull IArenaRegion region) {
        return this.getLinkedRegionsMap().computeIfAbsent(region, k -> new HashSet<>());
    }

    default void addRegion(@NotNull IArenaRegion region) {
        this.getRegionsMap().put(region.getId(), region);
    }

    default boolean removeRegion(@NotNull IArenaRegion region) {
        if (region.getFile().delete()) {
            region.clear();
            this.getRegionsMap().remove(region.getId());
            this.getLinkedRegionsMap().remove(region);
            this.getLinkedRegionsMap().values().forEach(linked -> linked.remove(region));
            return true;
        }
        return false;
    }
}
