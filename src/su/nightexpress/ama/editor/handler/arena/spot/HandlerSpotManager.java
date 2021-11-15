package su.nightexpress.ama.editor.handler.arena.spot;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.manager.editor.EditorManager;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.arena.spot.IArenaSpot;
import su.nightexpress.ama.api.ArenaCuboid;
import su.nightexpress.ama.arena.spot.ArenaSpot;
import su.nightexpress.ama.arena.spot.ArenaSpotManager;
import su.nightexpress.ama.editor.ArenaEditorType;
import su.nightexpress.ama.editor.ArenaInputHandler;

public class HandlerSpotManager extends ArenaInputHandler<ArenaSpotManager> {

    public HandlerSpotManager(@NotNull AMA plugin) {
        super(plugin);
    }

    @Override
    public boolean onType(@NotNull Player player, @NotNull ArenaSpotManager spotManager, @NotNull ArenaEditorType type, @NotNull String msg) {
        if (type == ArenaEditorType.SPOT_CREATE) {
            String id = EditorManager.fineId(msg);
            if (spotManager.getSpot(id) != null) {
                EditorManager.errorCustom(player, plugin.lang().Editor_Spot_Error_Id.getMsg());
                return false;
            }

            String path = spotManager.getArenaConfig().getFile().getParentFile().getAbsolutePath() + ArenaSpotManager.DIR_SPOTS + id + ".yml";
            IArenaSpot spot = new ArenaSpot(spotManager.getArenaConfig(), path, ArenaCuboid.empty());
            spotManager.addSpot(spot);
        }
        return true;
    }
}
