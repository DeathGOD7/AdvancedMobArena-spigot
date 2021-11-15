package su.nightexpress.ama.arena.game.trigger;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.IPlaceholder;
import su.nexmedia.engine.config.api.ILangMsg;
import su.nexmedia.engine.utils.NumberUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.arena.IArena;
import su.nightexpress.ama.api.arena.config.IArenaConfig;
import su.nightexpress.ama.api.arena.game.ArenaGameEventType;
import su.nightexpress.ama.api.arena.game.event.ArenaGameEventEvent;
import su.nightexpress.ama.arena.game.trigger.value.AbstractArenaGameTriggerValue;
import su.nightexpress.ama.arena.game.trigger.value.ArenaGameTriggerValueNumber;

public class ArenaGameEventTriggerNumber extends AbstractArenaGameEventTrigger<Double> implements IPlaceholder {

    public ArenaGameEventTriggerNumber(@NotNull IArenaConfig arenaConfig, @NotNull ArenaGameEventType eventType, @NotNull String input) {
        super(arenaConfig, eventType, input);
    }

    @Override
    protected void loadValues(@NotNull String[] values) {
        for (String value : values) {
            this.values.add(new ArenaGameTriggerValueNumber(value));
        }
    }

    @Override
    @NotNull
    protected String formatValue(@NotNull AbstractArenaGameTriggerValue<Double> triggerValue) {
        AMA plugin = this.arenaConfig.plugin();
        String value = NumberUT.format(triggerValue.getValue());
        ILangMsg msg;

        if (triggerValue.isInterval()) {
            if (triggerValue.isNegated()) {
                msg = plugin.lang().Arena_Game_Trigger_Format_Value_Number_EachNot;
            }
            else msg = plugin.lang().Arena_Game_Trigger_Format_Value_Number_Each;
        }
        else {
            if (triggerValue.isNegated()) {
                msg = plugin.lang().Arena_Game_Trigger_Format_Value_Number_Not;
            }
            else msg = plugin.lang().Arena_Game_Trigger_Format_Value_Number_Raw;
        }

        return msg.getMsg().replace(PLACEHOLDER_DEEP_VALUE, value);
    }

    @Override
    public boolean isReady(@NotNull ArenaGameEventEvent event) {
        if (event.getEventType() != this.getType()) return false;

        IArena arena = event.getArena();
        int arenaValue = switch (this.getType()) {
            case WAVE_START, WAVE_END -> arena.getWaveNumber();
            case SCORE_INCREASED, SCORE_DECREASED -> arena.getGameScore();
            default -> 0;
        };

        return this.test((double) arenaValue);
    }
}
