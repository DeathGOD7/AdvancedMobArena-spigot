package su.nightexpress.ama.editor.handler.kit;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.editor.EditorUtils;
import su.nexmedia.engine.manager.editor.EditorManager;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.kits.IArenaKit;
import su.nightexpress.ama.editor.ArenaEditorType;
import su.nightexpress.ama.editor.ArenaInputHandler;
import su.nightexpress.ama.kits.ArenaKit;
import su.nightexpress.ama.kits.KitManager;

public class EditorHandlerKitManager extends ArenaInputHandler<KitManager> {

    public EditorHandlerKitManager(@NotNull AMA plugin) {
        super(plugin);
    }

    @Override
    public boolean onType(@NotNull Player player, @NotNull KitManager object,
                          @NotNull ArenaEditorType type, @NotNull String msg) {

        if (type == ArenaEditorType.KIT_CREATE) {
            String id = EditorUtils.fineId(msg);
            if (plugin.getKitManager().getKitById(id) != null) {
                EditorManager.errorCustom(player, plugin.lang().Editor_Kit_Error_Exist.getMsg());
                return false;
            }

            IArenaKit kit = new ArenaKit(plugin, plugin.getDataFolder() + "/kits/kits/" + id + ".yml");
            kit.save();
            plugin.getKitManager().getKitsMap().put(kit.getId(), kit);
            return true;
        }
        return true;
    }
}
