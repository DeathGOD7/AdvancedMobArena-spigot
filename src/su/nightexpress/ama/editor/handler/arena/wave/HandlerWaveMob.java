package su.nightexpress.ama.editor.handler.arena.wave;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.manager.editor.EditorManager;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.arena.wave.IArenaWaveMob;
import su.nightexpress.ama.editor.ArenaEditorType;
import su.nightexpress.ama.editor.ArenaInputHandler;

public class HandlerWaveMob extends ArenaInputHandler<IArenaWaveMob> {

    public HandlerWaveMob(@NotNull AMA plugin) {
        super(plugin);
    }

    @Override
    public boolean onType(
            @NotNull Player player, @NotNull IArenaWaveMob waveMob,
            @NotNull ArenaEditorType type, @NotNull String msg) {

        switch (type) {
            case WAVES_CHANGE_WAVE_MOB_AMOUNT -> {
                int value = StringUT.getInteger(msg, -1);
                if (value < 0) {
                    EditorManager.errorNumber(player, false);
                    return false;
                }
                waveMob.setAmount(value);
            }
            case WAVES_CHANGE_WAVE_MOB_LEVEL -> {
                int value = StringUT.getInteger(msg, -1);
                if (value < 0) {
                    EditorManager.errorNumber(player, false);
                    return false;
                }
                waveMob.setLevel(value);
            }
            case WAVES_CHANGE_WAVE_MOB_CHANCE -> {
                double value = StringUT.getDouble(msg, -1);
                if (value < 0) {
                    EditorManager.errorNumber(player, true);
                    return false;
                }
                waveMob.setChance(value);
            }
            default -> {}
        }

        waveMob.getArenaWave().getArenaConfig().getWaveManager().save();
        return true;
    }
}
