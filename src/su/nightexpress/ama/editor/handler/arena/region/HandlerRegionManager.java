package su.nightexpress.ama.editor.handler.arena.region;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.manager.editor.EditorManager;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.arena.region.ArenaRegion;
import su.nightexpress.ama.arena.region.ArenaRegionManager;
import su.nightexpress.ama.editor.ArenaEditorType;
import su.nightexpress.ama.editor.ArenaInputHandler;

public class HandlerRegionManager extends ArenaInputHandler<ArenaRegionManager> {

    public HandlerRegionManager(@NotNull AMA plugin) {
        super(plugin);
    }

    @Override
    public boolean onType(@NotNull Player player, @NotNull ArenaRegionManager regionManager,
                          @NotNull ArenaEditorType type, @NotNull String msg) {

        if (type == ArenaEditorType.REGION_CREATE) {
            String id = EditorManager.fineId(msg);
            if (regionManager.getRegion(id) != null) {
                EditorManager.errorCustom(player, plugin.lang().Editor_Region_Error_Create.getMsg());
                return false;
            }

            String path = regionManager.getArenaConfig().getFile().getParentFile().getAbsolutePath() + ArenaRegionManager.DIR_REGIONS + id + ".yml";
            ArenaRegion region = new ArenaRegion(regionManager.getArenaConfig(), path);
            regionManager.addRegion(region);
            return true;
        }

        return true;
    }
}
