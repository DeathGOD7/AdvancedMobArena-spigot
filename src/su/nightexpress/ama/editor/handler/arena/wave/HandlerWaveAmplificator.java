package su.nightexpress.ama.editor.handler.arena.wave;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.manager.editor.EditorManager;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.arena.game.IArenaGameEventTrigger;
import su.nightexpress.ama.api.arena.wave.IArenaWaveAmplificator;
import su.nightexpress.ama.arena.game.trigger.AbstractArenaGameEventTrigger;
import su.nightexpress.ama.editor.ArenaEditorType;
import su.nightexpress.ama.editor.ArenaInputHandler;

public class HandlerWaveAmplificator extends ArenaInputHandler<IArenaWaveAmplificator> {

    public HandlerWaveAmplificator(@NotNull AMA plugin) {
        super(plugin);
    }

    @Override
    public boolean onType(
            @NotNull Player player, @NotNull IArenaWaveAmplificator amplificator,
            @NotNull ArenaEditorType type, @NotNull String msg) {

        switch (type) {
            case WAVES_CHANGE_WAVE_AMPLIFICATOR_VALUE_LEVEL: {
                amplificator.setValueLevel(StringUT.getInteger(msg, 0, true));
                break;
            }
            case WAVES_CHANGE_WAVE_AMPLIFICATOR_VALUE_AMOUNT: {
                amplificator.setValueAmount(StringUT.getInteger(msg, 0, true));
                break;
            }
            case WAVES_CHANGE_WAVE_AMPLIFICATOR_TRIGGERS_ADD: {
                String[] split = StringUT.colorOff(msg).split(" ");
                if (split.length < 2) {
                    EditorManager.errorCustom(player, plugin.lang().Editor_Error_Triggers.getMsg());
                    return false;
                }

                IArenaGameEventTrigger trigger = AbstractArenaGameEventTrigger.parse(amplificator.getArenaWave().getArenaConfig(), split[0], split[1]);
                if (trigger == null) {
                    EditorManager.errorCustom(player, plugin.lang().Editor_Error_Triggers.getMsg());
                    return false;
                }

                amplificator.getTriggers().add(trigger);
            }
            default: {
                return true;
            }
        }

        amplificator.getArenaWave().getArenaConfig().getWaveManager().save();
        return true;
    }
}
