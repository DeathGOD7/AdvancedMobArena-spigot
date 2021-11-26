package su.nightexpress.ama.editor.handler.mob;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.editor.EditorUtils;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.editor.ArenaEditorType;
import su.nightexpress.ama.editor.ArenaInputHandler;
import su.nightexpress.ama.mobs.ArenaCustomMob;
import su.nightexpress.ama.mobs.MobManager;

public class EditorHandlerMobManager extends ArenaInputHandler<MobManager> {

    public EditorHandlerMobManager(@NotNull AMA plugin) {
        super(plugin);
    }

    @Override
    public boolean onType(@NotNull Player player, @NotNull MobManager object,
                          @NotNull ArenaEditorType type, @NotNull String msg) {

        if (type == ArenaEditorType.MOB_CREATE) {
            String id = EditorUtils.fineId(msg);
            if (plugin.getMobManager().getMobById(id) != null) {
                EditorUtils.errorCustom(player, plugin.lang().Editor_Mob_Error_Exist.getMsg());
                return false;
            }

            ArenaCustomMob mob = new ArenaCustomMob(plugin, plugin.getDataFolder() + "/mobs/" + id + ".yml");
            mob.save();
            plugin.getMobManager().getMobsMap().put(mob.getId(), mob);
            return true;
        }
        return true;
    }
}
