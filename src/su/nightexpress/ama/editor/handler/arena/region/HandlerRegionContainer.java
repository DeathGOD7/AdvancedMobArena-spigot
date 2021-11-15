package su.nightexpress.ama.editor.handler.arena.region;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.manager.editor.EditorManager;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.arena.game.IArenaGameEventTrigger;
import su.nightexpress.ama.api.arena.region.IArenaRegionContainer;
import su.nightexpress.ama.arena.game.trigger.AbstractArenaGameEventTrigger;
import su.nightexpress.ama.editor.ArenaEditorType;
import su.nightexpress.ama.editor.ArenaInputHandler;

public class HandlerRegionContainer extends ArenaInputHandler<IArenaRegionContainer> {

    public HandlerRegionContainer(@NotNull AMA plugin) {
        super(plugin);
    }

    @Override
    public boolean onType(@NotNull Player player, @NotNull IArenaRegionContainer container,
                          @NotNull ArenaEditorType type, @NotNull String msg) {

        switch (type) {
            case REGION_CHANGE_CONTAINER_REFILL_TRIGGERS -> {
                String[] split = StringUT.colorOff(msg).split(" ");
                if (split.length < 2) {
                    EditorManager.errorCustom(player, plugin.lang().Editor_Error_Triggers.getMsg());
                    return false;
                }

                IArenaGameEventTrigger trigger = AbstractArenaGameEventTrigger.parse(container.getRegion().getArenaConfig(), split[0], split[1]);
                if (trigger == null) {
                    EditorManager.errorCustom(player, plugin.lang().Editor_Error_Triggers.getMsg());
                    return false;
                }

                container.getTriggers().add(trigger);
            }
            case REGION_CHANGE_CONTAINER_REFILL_AMOUNT_MIN, REGION_CHANGE_CONTAINER_REFILL_AMOUNT_MAX -> {
                int value = StringUT.getInteger(msg, 0);
                if (type == ArenaEditorType.REGION_CHANGE_CONTAINER_REFILL_AMOUNT_MIN) {
                    container.setMinItems(value);
                }
                else container.setMaxItems(value);
            }
        }

        container.getRegion().save();
        return true;
    }
}
