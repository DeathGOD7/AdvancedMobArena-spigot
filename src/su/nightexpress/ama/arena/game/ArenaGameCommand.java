package su.nightexpress.ama.arena.game;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.ama.api.arena.config.IArenaConfig;
import su.nightexpress.ama.api.arena.game.ArenaGameTargetType;
import su.nightexpress.ama.api.arena.game.IArenaGameCommand;
import su.nightexpress.ama.api.arena.game.IArenaGameEventTrigger;
import su.nightexpress.ama.arena.editor.game.EditorGameCommandSettings;

import java.util.List;
import java.util.Set;

public class ArenaGameCommand implements IArenaGameCommand {

    private final IArenaConfig arenaConfig;

    private final Set<IArenaGameEventTrigger> triggers;
    private       ArenaGameTargetType         targetType;
    private List<String> commands;

    private EditorGameCommandSettings editor;

    public ArenaGameCommand(
            @NotNull IArenaConfig arenaConfig, @NotNull Set<IArenaGameEventTrigger> triggers,
            @NotNull ArenaGameTargetType targetType, @NotNull List<String> commands) {
        this.arenaConfig = arenaConfig;

        this.triggers = triggers;
        this.setTargetType(targetType);
        this.setCommands(commands);
    }

    @Override
    public void clear() {
        if (this.editor != null) {
            this.editor.clear();
            this.editor = null;
        }
    }

    @NotNull
    @Override
    public EditorGameCommandSettings getEditor() {
        if (this.editor == null) {
            this.editor = new EditorGameCommandSettings(this);
        }
        return editor;
    }

    @NotNull
    @Override
    public IArenaConfig getArenaConfig() {
        return arenaConfig;
    }

    @NotNull
    @Override
    public Set<IArenaGameEventTrigger> getTriggers() {
        return triggers;
    }

    @NotNull
    @Override
    public ArenaGameTargetType getTargetType() {
        return targetType;
    }

    @Override
    public void setTargetType(@NotNull ArenaGameTargetType targetType) {
        this.targetType = targetType;
    }

    @NotNull
    @Override
    public List<String> getCommands() {
        return commands;
    }

    @Override
    public void setCommands(@NotNull List<String> commands) {
        this.commands = commands;
    }
}
