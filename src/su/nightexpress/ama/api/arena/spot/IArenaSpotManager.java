package su.nightexpress.ama.api.arena.spot;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.manager.IEditable;
import su.nexmedia.engine.api.manager.ILoadable;
import su.nightexpress.ama.api.arena.config.IArenaObject;
import su.nightexpress.ama.api.arena.config.IProblematic;

import java.util.Collection;
import java.util.Map;

public interface IArenaSpotManager extends IArenaObject, ILoadable, IEditable, IProblematic {

    @NotNull Map<String, IArenaSpot> getSpotsMap();

    @NotNull
    default Collection<IArenaSpot> getSpots() {
        return this.getSpotsMap().values();
    }

    void addSpot(@NotNull IArenaSpot spot);

    void removeSpot(@NotNull IArenaSpot spot);

    @Nullable
    default IArenaSpot getSpot(@NotNull String id) {
        return this.getSpotsMap().get(id.toLowerCase());
    }

    @Nullable
    default IArenaSpot getSpot(@NotNull Location location) {
        return this.getSpots().stream().filter(spot -> spot.getCuboid().contains(location)).findFirst().orElse(null);
    }
}
