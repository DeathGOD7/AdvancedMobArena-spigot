package su.nightexpress.ama.editor.handler.arena.wave;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.manager.editor.EditorManager;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.arena.wave.IArenaWave;
import su.nightexpress.ama.arena.wave.ArenaWave;
import su.nightexpress.ama.arena.wave.ArenaWaveManager;
import su.nightexpress.ama.editor.ArenaEditorType;
import su.nightexpress.ama.editor.ArenaInputHandler;

import java.util.HashMap;

public class HandlerWaveManager extends ArenaInputHandler<ArenaWaveManager> {

    public HandlerWaveManager(@NotNull AMA plugin) {
        super(plugin);
    }

    @Override
    public boolean onType(
            @NotNull Player player, @NotNull ArenaWaveManager waves,
            @NotNull ArenaEditorType type, @NotNull String msg) {

        switch (type) {
            case WAVES_CREATE_WAVE -> {
                String id = EditorManager.fineId(msg);
                if (waves.getWave(id) != null) {
                    EditorManager.errorCustom(player, plugin.lang().Editor_Arena_Waves_Error_Wave_Exist.getMsg());
                    return false;
                }

                IArenaWave wave = new ArenaWave(waves.getArenaConfig(), id, new HashMap<>(), new HashMap<>());
                waves.getWaves().put(id, wave);
            }
            case WAVES_CHANGE_DELAY_FIRST -> {
                int delay = StringUT.getInteger(msg, 5);
                waves.setDelayFirst(delay);
            }
            case WAVES_CHANGE_DELAY_DEFAULT -> {
                int delay = StringUT.getInteger(msg, 5);
                waves.setDelayDefault(delay);
            }
            case WAVES_CHANGE_FINAL_WAVE -> {
                int wave = StringUT.getInteger(msg, 25);
                waves.setFinalWave(wave);
            }
            case WAVES_CHANGE_GRADUAL_FIRST_PERCENT -> {
                double value = StringUT.getDouble(msg, 50D);
                waves.setGradualSpawnPercentFirst(value);
            }
            case WAVES_CHANGE_GRADUAL_NEXT_PERCENT -> {
                double value = StringUT.getDouble(msg, 20D);
                waves.setGradualSpawnNextPercent(value);
            }
            case WAVES_CHANGE_GRADUAL_NEXT_INTERVAL -> {
                int value = StringUT.getInteger(msg, 5);
                waves.setGradualSpawnNextInterval(value);
            }
            case WAVES_CHANGE_GRADUAL_NEXT_KILL_PERCENT -> {
                double value = StringUT.getDouble(msg, 10D);
                waves.setGradualSpawnNextKillPercent(value);
            }
            default -> {}
        }

        waves.save();
        return true;
    }
}
