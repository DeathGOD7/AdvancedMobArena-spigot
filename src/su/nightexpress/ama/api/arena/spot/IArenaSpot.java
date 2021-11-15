package su.nightexpress.ama.api.arena.spot;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.manager.ConfigHolder;
import su.nexmedia.engine.api.manager.ICleanable;
import su.nexmedia.engine.api.manager.IEditable;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.arena.IArena;
import su.nightexpress.ama.api.arena.config.IArenaObject;
import su.nightexpress.ama.api.arena.config.IProblematic;
import su.nightexpress.ama.api.ArenaCuboid;

import java.util.Map;
import java.util.function.UnaryOperator;

public interface IArenaSpot extends IArenaObject, ConfigHolder, IEditable, ICleanable, IProblematic {

    String PLACEHOLDER_ID = "%spot_id%";
    String PLACEHOLDER_NAME   = "%spot_name%";
    String PLACEHOLDER_ACTIVE = "%spot_active%";

    @Override
    @NotNull
    default UnaryOperator<String> replacePlaceholders() {
        return str -> IProblematic.super.replacePlaceholders().apply(str
                .replace(PLACEHOLDER_ID, this.getId())
                .replace(PLACEHOLDER_NAME, this.getName())
                .replace(PLACEHOLDER_ACTIVE, plugin().lang().getBool(this.isActive()))
        );
    }

    @NotNull AMA plugin();

    @NotNull String getId();

    boolean isActive();

    void setActive(boolean isActive);

    @NotNull String getName();

    void setName(@NotNull String name);

    @NotNull ArenaCuboid getCuboid();

    void setCuboid(@NotNull ArenaCuboid cuboid);

    @NotNull Map<String, IArenaSpotState> getStates();

    @Nullable
    default IArenaSpotState getState(@NotNull String id) {
        return this.getStates().get(id.toLowerCase());
    }

    default void setState(@NotNull IArena arena, @NotNull String id) {
        IArenaSpotState state = this.getState(id);
        if (state == null) return;
        state.build(arena);
    }
}
