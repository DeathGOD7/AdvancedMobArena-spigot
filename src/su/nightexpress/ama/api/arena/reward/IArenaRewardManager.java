package su.nightexpress.ama.api.arena.reward;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.ConfigHolder;
import su.nexmedia.engine.api.manager.IEditable;
import su.nexmedia.engine.api.manager.ILoadable;
import su.nightexpress.ama.api.arena.config.IArenaObject;
import su.nightexpress.ama.api.arena.config.IProblematic;

import java.util.Set;

public interface IArenaRewardManager extends IArenaObject, ConfigHolder, ILoadable, IEditable, IProblematic {

    boolean isRetainOnDeath();

    void setRetainOnDeath(boolean isRetainOnDeath);

    boolean isRetainOnLeave();

    void setRetainOnLeave(boolean isSaveOnLeave);

    @NotNull Set<IArenaReward> getRewards();

    void setRewards(@NotNull Set<IArenaReward> rewards);
}
