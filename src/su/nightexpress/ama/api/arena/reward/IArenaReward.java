package su.nightexpress.ama.api.arena.reward;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.ICleanable;
import su.nexmedia.engine.api.manager.IEditable;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.NumberUT;
import su.nexmedia.engine.utils.PlayerUT;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.ama.api.arena.IArena;
import su.nightexpress.ama.api.arena.config.IArenaObject;
import su.nightexpress.ama.api.arena.game.ArenaGameTargetType;
import su.nightexpress.ama.api.arena.game.IArenaGameEventListener;
import su.nightexpress.ama.api.arena.game.event.ArenaGameEventEvent;

import java.util.List;
import java.util.function.UnaryOperator;

public interface IArenaReward extends IArenaGameEventListener, IArenaObject, IEditable, ICleanable {

    String PLACEHOLDER_NAME = "%reward_name%";
    String PLACEHOLDER_IS_LATE = "%reward_is_late%";
    String PLACEHOLDER_CHANCE = "%reward_chance%";
    String PLACEHOLDER_COMMANDS = "%reward_commands%";
    String PLACEHOLDER_TARGET = "%reward_target_type%";

    @Override
    @NotNull
    default UnaryOperator<String> replacePlaceholders() {
        return str -> IArenaGameEventListener.super.replacePlaceholders().apply(str
                .replace(PLACEHOLDER_NAME, this.getName())
                .replace(PLACEHOLDER_TARGET, plugin().lang().getEnum(this.getTargetType()))
                .replace(PLACEHOLDER_CHANCE, NumberUT.format(this.getChance()))
                .replace(PLACEHOLDER_IS_LATE, plugin().lang().getBool(this.isLate()))
                .replace(PLACEHOLDER_COMMANDS, String.join("\n", this.getCommands()))
        );
    }

    @NotNull String getName();

    void setName(@NotNull String name);

    boolean isLate();

    void setLate(boolean isLate);

    @NotNull ArenaGameTargetType getTargetType();

    void setTargetType(@NotNull ArenaGameTargetType targetType);

    double getChance();

    void setChance(double chance);

    @NotNull List<String> getCommands();

    void setCommands(@NotNull List<String> commands);

    @NotNull List<ItemStack> getItems();

    void setItems(@NotNull List<ItemStack> items);

    @Override
    default boolean onGameEvent(@NotNull ArenaGameEventEvent gameEvent) {
        if (!this.isReady(gameEvent)) return false;

        this.give(gameEvent.getArena());
        return true;
    }

    default void give(@NotNull IArena arena) {
        if (Rnd.get(true) >= this.getChance()) return;

        arena.getPlayers(this.getTargetType()).forEach(arenaPlayer -> {
            if (this.isLate()) {
                arenaPlayer.getRewards().add(this);
            }
            else this.give(arenaPlayer.getPlayer());
        });
    }

    default void give(@NotNull Player player) {
        this.getItems().forEach(item -> ItemUT.addItem(player, item));
        this.getCommands().forEach(command -> PlayerUT.execCmd(player, command));

        plugin().lang().Arena_Game_Notify_Reward.replace(this.replacePlaceholders()).send(player);
    }
}
