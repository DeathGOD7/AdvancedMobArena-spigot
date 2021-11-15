package su.nightexpress.ama.editor.handler.arena.wave;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.editor.EditorUtils;
import su.nexmedia.engine.hooks.external.MythicMobsHK;
import su.nexmedia.engine.manager.editor.EditorManager;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.arena.wave.IArenaWave;
import su.nightexpress.ama.api.arena.wave.IArenaWaveAmplificator;
import su.nightexpress.ama.api.arena.wave.IArenaWaveMob;
import su.nightexpress.ama.arena.wave.ArenaWaveAmplificator;
import su.nightexpress.ama.arena.wave.ArenaWaveMob;
import su.nightexpress.ama.editor.ArenaEditorType;
import su.nightexpress.ama.editor.ArenaInputHandler;
import su.nightexpress.ama.mobs.ArenaCustomMob;

import java.util.HashSet;

public class HandlerWave extends ArenaInputHandler<IArenaWave> {

    public HandlerWave(@NotNull AMA plugin) {
        super(plugin);
    }

    @Override
    public boolean onType(
            @NotNull Player player, @NotNull IArenaWave wave,
            @NotNull ArenaEditorType type, @NotNull String msg) {

        switch (type) {
            case WAVES_CREATE_WAVE_MOB -> {
                if (wave.getMobs().get(msg) != null) {
                    EditorUtils.errorCustom(player, plugin.lang().Editor_Arena_Waves_Error_Mob_Exist.getMsg());
                    return false;
                }

                MythicMobsHK mm = plugin.getMythicMobs();
                ArenaCustomMob customMob = plugin.getMobManager().getMobById(msg);
                boolean mmValid = mm != null && mm.isValid(msg);
                if (customMob == null && !mmValid) {
                    EditorManager.errorCustom(player, plugin.lang().Editor_Arena_Waves_Error_Mob_Invalid.getMsg());
                    return false;
                }

                IArenaWaveMob waveMob = new ArenaWaveMob(wave, msg, 1, 1, 100D);
                wave.getMobs().put(waveMob.getMobId(), waveMob);
            }
            case WAVES_CREATE_WAVE_AMPLIFICATOR -> {
                String id = EditorUtils.fineId(msg);
                if (wave.getAmplificators().get(id) != null) {
                    EditorManager.errorCustom(player, plugin.lang().Editor_Arena_Waves_Error_Amplificator_Exist.getMsg());
                    return false;
                }

                IArenaWaveAmplificator amplificator = new ArenaWaveAmplificator(wave, id, new HashSet<>(), 0, 0);
                wave.getAmplificators().put(amplificator.getId(), amplificator);
            }
            default -> {}
        }

        wave.getArenaConfig().getWaveManager().save();
        return true;
    }
}
