package su.nightexpress.ama.editor.handler.arena.spot;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.manager.editor.EditorManager;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.arena.spot.IArenaSpotState;
import su.nightexpress.ama.arena.spot.ArenaSpot;
import su.nightexpress.ama.arena.spot.ArenaSpotState;
import su.nightexpress.ama.editor.ArenaEditorType;
import su.nightexpress.ama.editor.ArenaInputHandler;

import java.util.ArrayList;
import java.util.HashSet;

public class HandlerSpot extends ArenaInputHandler<ArenaSpot> {

    public HandlerSpot(@NotNull AMA plugin) {
        super(plugin);
    }

    @Override
    public boolean onType(
            @NotNull Player player, @NotNull ArenaSpot spot,
            @NotNull ArenaEditorType type, @NotNull String msg) {

        switch (type) {
            case SPOT_STATE_CREATE -> {
                String id = EditorManager.fineId(msg);
                if (spot.getState(id) != null) {
                    EditorManager.errorCustom(player, plugin.lang().Editor_Spot_State_Error_Id.getMsg());
                    return false;
                }

                IArenaSpotState state = new ArenaSpotState(spot, id, new HashSet<>(), new ArrayList<>());
                spot.getStates().put(state.getId(), state);
            }
            case SPOT_CHANGE_NAME -> spot.setName(msg);
        }

        spot.save();
        return true;
    }
}
