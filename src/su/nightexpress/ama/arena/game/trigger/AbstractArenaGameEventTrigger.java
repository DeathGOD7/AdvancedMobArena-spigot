package su.nightexpress.ama.arena.game.trigger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.utils.CollectionsUT;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.arena.config.IArenaConfig;
import su.nightexpress.ama.api.arena.game.ArenaGameEventType;
import su.nightexpress.ama.api.arena.game.IArenaGameEventTrigger;
import su.nightexpress.ama.arena.game.trigger.value.AbstractArenaGameTriggerValue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;

public abstract class AbstractArenaGameEventTrigger<T> implements IArenaGameEventTrigger {

    protected IArenaConfig       arenaConfig;
    protected ArenaGameEventType eventType;
    protected List<AbstractArenaGameTriggerValue<T>> values;

    public AbstractArenaGameEventTrigger(@NotNull IArenaConfig arenaConfig, @NotNull ArenaGameEventType eventType, @NotNull String inputs) {
        this.arenaConfig = arenaConfig;
        this.eventType = eventType;
        this.values = new ArrayList<>();
        this.loadValues(StringUT.noSpace(inputs).split(","));
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders() {
        AMA plugin = this.arenaConfig.plugin();
        String separator = plugin.lang().Arena_Game_Trigger_Format_Value_Delimiter.getMsg();

        StringBuilder builder = new StringBuilder();
        for (AbstractArenaGameTriggerValue<T> triggerValue : this.getValues()) {
            if (builder.length() > 0) builder.append(separator);
            builder.append(this.formatValue(triggerValue));
        }

        return str -> str
            .replace(PLACEHOLDER_VALUE, builder.toString())
            .replace(PLACEHOLDER_TYPE, plugin.lang().getEnum(this.getType()))
            ;
    }

    @NotNull
    public static Set<IArenaGameEventTrigger> parse(@NotNull IArenaConfig arenaConfig, @NotNull JYML cfg, @NotNull String path) {
        Set<IArenaGameEventTrigger> triggers = new HashSet<>();
        for (String typeRaw : cfg.getSection(path)) {
            String input = cfg.getString(path + "." + typeRaw, "");
            IArenaGameEventTrigger trigger = parse(arenaConfig, typeRaw, input);
            if (trigger == null) continue;

            triggers.add(trigger);
        }
        return triggers;
    }

    @Nullable
    public static IArenaGameEventTrigger parse(@NotNull IArenaConfig arenaConfig, @NotNull String typeRaw, @NotNull String input) {
        ArenaGameEventType eventType = CollectionsUT.getEnum(typeRaw, ArenaGameEventType.class);
        if (eventType == null || input.isEmpty()) return null;

        return switch (eventType) {
            case GAME_START, GAME_END_LOSE, GAME_END_TIME, GAME_END_WIN -> new ArenaGameEventTriggerBoolean(arenaConfig, eventType, input);
            case WAVE_END, WAVE_START, SCORE_DECREASED, SCORE_INCREASED -> new ArenaGameEventTriggerNumber(arenaConfig, eventType, input);
            case MOB_KILLED, SHOP_ITEM_LOCKED, SHOP_ITEM_UNLOCKED, SPOT_CHANGED,
                    REGION_LOCKED, REGION_UNLOCKED,
                    PLAYER_DEATH, PLAYER_JOIN, PLAYER_LEAVE -> new ArenaGameEventTriggerString(arenaConfig, eventType, input);
        };
    }

    public void saveTo(@NotNull JYML cfg, @NotNull String path) {
        cfg.set(path + this.getType().name(), String.join(",", this.getValues().stream().map(AbstractArenaGameTriggerValue::getValueRaw).toList()));
    }

    protected abstract void loadValues(@NotNull String[] values);

    @NotNull
    protected abstract String formatValue(@NotNull AbstractArenaGameTriggerValue<T> triggerValue);

    @Override
    @NotNull
    public final ArenaGameEventType getType() {
        return this.eventType;
    }

    @NotNull
    public final List<AbstractArenaGameTriggerValue<T>> getValues() {
        return values;
    }

    public final boolean test(@NotNull T t) {
        if (this.getValues().stream().allMatch(AbstractArenaGameTriggerValue::isSimple)) {
            return this.getValues().stream().anyMatch(v -> v.test(t));
        }
        return this.getValues().stream().allMatch(v -> v.test(t));
    }
}
