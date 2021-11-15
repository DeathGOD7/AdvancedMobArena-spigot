package su.nightexpress.ama.api.arena.spot;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.ama.api.arena.IArena;
import su.nightexpress.ama.api.arena.game.IArenaGameEventListener;
import su.nightexpress.ama.api.arena.game.event.ArenaGameEventEvent;
import su.nightexpress.ama.api.arena.spot.event.ArenaSpotStateChangeEvent;

import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

public interface IArenaSpotState extends IArenaGameEventListener {

    String PLACEHOLDER_ID = "%spot_state_id%";

    @Override
    @NotNull
    default UnaryOperator<String> replacePlaceholders() {
        return str -> IArenaGameEventListener.super.replacePlaceholders().apply(str
                .replace(PLACEHOLDER_ID, this.getId())
        );
    }

    @Override
    default boolean onGameEvent(@NotNull ArenaGameEventEvent gameEvent) {
        if (!this.isReady(gameEvent)) return false;

        this.build(gameEvent.getArena());
        return true;
    }

    @NotNull IArenaSpot getSpot();

    @NotNull String getId();

    @NotNull List<String> getSchemeRaw();

    void setSchemeRaw(@NotNull List<String> schemeRaw);

    @NotNull Map<Location, BlockData> getScheme();

    default void build() {
        this.getScheme().forEach((location, data) -> {
            Block block = location.getBlock();
            if (block.getBlockData().matches(data)) return;
            block.setBlockData(data);
        });
    }

    default void build(@NotNull IArena arena) {
        this.build();

        ArenaSpotStateChangeEvent event = new ArenaSpotStateChangeEvent(arena, this.getSpot(), this);
        this.getSpot().plugin().getPluginManager().callEvent(event);
    }
}
