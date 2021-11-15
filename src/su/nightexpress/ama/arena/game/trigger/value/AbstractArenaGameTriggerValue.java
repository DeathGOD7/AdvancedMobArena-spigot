package su.nightexpress.ama.arena.game.trigger.value;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.NumberUT;

import java.util.function.Predicate;

public abstract class AbstractArenaGameTriggerValue<T> {

    protected  T                value;
    protected final     boolean isNegated;
    protected final boolean     isInterval;
    protected final boolean isGreater;
    protected final boolean isSmaller;
    protected     Predicate<T> predicate;

    protected static final String DEF_NEGATED = "!";
    protected static final String DEF_INTERVAL = "%";
    protected static final String DEF_GREATER = ">";
    protected static final String DEF_SMALLER = "<";

    public AbstractArenaGameTriggerValue(@NotNull String input) {
        this.isNegated = input.contains(DEF_NEGATED);
        this.isInterval = input.contains(DEF_INTERVAL);
        this.isGreater = input.contains(DEF_GREATER) && !input.contains(DEF_SMALLER);
        this.isSmaller = input.contains(DEF_SMALLER) && !input.contains(DEF_GREATER);

        this.loadValue(input
                .replace(DEF_INTERVAL, "").replace(DEF_NEGATED, "")
                .replace(DEF_GREATER, "").replace(DEF_SMALLER, ""));
    }

    protected abstract void loadValue(@NotNull String input);

    public boolean isSimple() {
        return !this.isNegated() && !this.isSmaller() && !this.isGreater();
    }

    public boolean isNegated() {
        return isNegated;
    }

    public boolean isInterval() {
        return isInterval;
    }

    public boolean isGreater() {
        return isGreater;
    }

    public boolean isSmaller() {
        return isSmaller;
    }

    public T getValue() {
        return value;
    }

    @NotNull
    public String getValueRaw() {
        String value = this.getValue() instanceof Double d ? NumberUT.format(d) : String.valueOf(this.getValue());
        if (this.isInterval()) value = DEF_INTERVAL + value;
        if (this.isNegated()) value = DEF_NEGATED + value;
        if (this.isGreater()) value = DEF_GREATER + value;
        if (this.isSmaller()) value = DEF_SMALLER + value;
        return value;
    }

    @NotNull
    public Predicate<T> getPredicate() {
        return predicate;
    }

    public boolean test(T of) {
        return this.getPredicate().test(of);
    }
}
