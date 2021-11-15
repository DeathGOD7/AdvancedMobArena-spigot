package su.nightexpress.ama.api.arena.game;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.ICleanable;
import su.nexmedia.engine.api.manager.IEditable;
import su.nexmedia.engine.utils.PlayerUT;
import su.nightexpress.ama.api.arena.IArena;
import su.nightexpress.ama.api.arena.config.IArenaObject;
import su.nightexpress.ama.api.arena.game.event.ArenaGameEventEvent;

import java.util.List;
import java.util.function.UnaryOperator;

public interface IArenaGameCommand extends IArenaGameEventListener, IArenaObject, IEditable, ICleanable {

    String PLACEHOLDER_TARGET = "%game_command_target%";
    String PLACEHOLDER_COMMANDS = "%game_command_commands%";

    @Override
    @NotNull
    default UnaryOperator<String> replacePlaceholders() {
        return str -> IArenaGameEventListener.super.replacePlaceholders().apply(str
                .replace(PLACEHOLDER_TARGET, plugin().lang().getEnum(this.getTargetType()))
                .replace(PLACEHOLDER_COMMANDS, String.join("\n", this.getCommands()))
        );
    }

    @Override
    default boolean onGameEvent(@NotNull ArenaGameEventEvent gameEvent) {
        if (!this.isReady(gameEvent)) return false;

        IArena arena = gameEvent.getArena();
        if (this.getTargetType() == ArenaGameTargetType.GLOBAL) {
            this.getCommands().forEach(cmd -> arena.plugin().getServer().dispatchCommand(plugin().getServer().getConsoleSender(), cmd));
            return true;
        }

        arena.getPlayers(this.getTargetType()).forEach(arenaPlayer -> {
            this.getCommands().forEach(cmd -> PlayerUT.execCmd(arenaPlayer.getPlayer(), cmd));
        });
        return true;
    }

    @NotNull ArenaGameTargetType getTargetType();

    void setTargetType(@NotNull ArenaGameTargetType targetType);

    @NotNull List<String> getCommands();

    void setCommands(@NotNull List<String> commands);
}
