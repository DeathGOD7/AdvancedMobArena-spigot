package su.nightexpress.ama.api.arena.type;

import org.jetbrains.annotations.NotNull;

public enum ArenaLockState {

    UNLOCKED, LOCKED;

    @NotNull
    public ArenaLockState getOpposite() {
        return this == LOCKED ? UNLOCKED : LOCKED;
    }
}
