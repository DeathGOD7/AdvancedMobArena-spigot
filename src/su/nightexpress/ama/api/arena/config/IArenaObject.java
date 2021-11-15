package su.nightexpress.ama.api.arena.config;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.arena.IArena;

public interface IArenaObject {

    @NotNull
    IArenaConfig getArenaConfig();

    @NotNull
    default AMA plugin() {
        return this.getArenaConfig().plugin();
    }

    @NotNull
    default IArena getArena() {
        return this.getArenaConfig().getArena();
    }
}
