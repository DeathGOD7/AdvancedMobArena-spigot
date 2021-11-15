package su.nightexpress.ama.api.arena.event;

import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.ama.api.arena.IArena;
import su.nightexpress.ama.api.arena.game.ArenaGameEventType;
import su.nightexpress.ama.api.arena.game.event.ArenaGameEventEvent;
import su.nightexpress.ama.arena.ArenaPlayer;

public class ArenaMobDeathEvent extends ArenaGameEventEvent {

    private ArenaPlayer  killer;
    private final LivingEntity entity;
    private final String       mobId;

    public ArenaMobDeathEvent(@NotNull IArena arena, @NotNull LivingEntity entity, @NotNull String mobId) {
        super(arena, ArenaGameEventType.MOB_KILLED);
        this.entity = entity;
        this.mobId = mobId;
    }

    public void setKiller(@Nullable ArenaPlayer killer) {
        this.killer = killer;
    }

    @Nullable
    public ArenaPlayer getKiller() {
        return killer;
    }

    @NotNull
    public LivingEntity getEntity() {
        return entity;
    }

    @NotNull
    public String getMobId() {
        return mobId;
    }
}
