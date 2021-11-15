package su.nightexpress.ama.editor.handler.arena.game;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.manager.editor.EditorManager;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.arena.game.IArenaGameCommand;
import su.nightexpress.ama.api.arena.game.IArenaGameEventTrigger;
import su.nightexpress.ama.arena.game.trigger.AbstractArenaGameEventTrigger;
import su.nightexpress.ama.editor.ArenaEditorType;
import su.nightexpress.ama.editor.ArenaInputHandler;

public class HandlerGameplayCommand extends ArenaInputHandler<IArenaGameCommand> {

    public HandlerGameplayCommand(@NotNull AMA plugin) {
        super(plugin);
    }

    @Override
    public boolean onType(@NotNull Player player, @NotNull IArenaGameCommand gameCommand,
                          @NotNull ArenaEditorType type, @NotNull String msg) {

        switch (type) {
            case GAME_COMMAND_CHANGE_TRIGGERS -> {
                String[] split = StringUT.colorOff(msg).split(" ");
                if (split.length < 2) {
                    EditorManager.errorCustom(player, plugin.lang().Editor_Error_Triggers.getMsg());
                    return false;
                }

                IArenaGameEventTrigger trigger = AbstractArenaGameEventTrigger.parse(gameCommand.getArenaConfig(), split[0], split[1]);
                if (trigger == null) {
                    EditorManager.errorCustom(player, plugin.lang().Editor_Error_Triggers.getMsg());
                    return false;
                }

                gameCommand.getTriggers().add(trigger);
            }
            case GAME_COMMAND_CHANGE_COMMANDS -> gameCommand.getCommands().add(StringUT.colorRaw(msg));
        }

        gameCommand.getArenaConfig().getGameplayManager().save();
        return true;
    }
}
