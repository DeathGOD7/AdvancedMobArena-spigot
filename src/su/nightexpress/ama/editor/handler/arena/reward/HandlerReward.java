package su.nightexpress.ama.editor.handler.arena.reward;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.manager.editor.EditorManager;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.arena.game.IArenaGameEventTrigger;
import su.nightexpress.ama.api.arena.reward.IArenaReward;
import su.nightexpress.ama.arena.game.trigger.AbstractArenaGameEventTrigger;
import su.nightexpress.ama.editor.ArenaEditorType;
import su.nightexpress.ama.editor.ArenaInputHandler;

public class HandlerReward extends ArenaInputHandler<IArenaReward> {

    public HandlerReward(@NotNull AMA plugin) {
        super(plugin);
    }

    @Override
    public boolean onType(@NotNull Player player, @NotNull IArenaReward reward, @NotNull ArenaEditorType type, @NotNull String msg) {
        switch (type) {
            case REWARD_CHANGE_NAME -> reward.setName(msg);
            case REWARD_CHANGE_CHANCE -> {
                double value = StringUT.getDouble(msg, -1D);
                if (value < 0) {
                    EditorManager.errorNumber(player, true);
                    return false;
                }
                reward.setChance(value);
            }
            case REWARD_CHANGE_TRIGGERS -> {
                String[] split = StringUT.colorOff(msg).split(" ");
                if (split.length < 2) {
                    EditorManager.errorCustom(player, plugin.lang().Editor_Error_Triggers.getMsg());
                    return false;
                }

                IArenaGameEventTrigger trigger = AbstractArenaGameEventTrigger.parse(reward.getArenaConfig(), split[0], split[1]);
                if (trigger == null) {
                    EditorManager.errorCustom(player, plugin.lang().Editor_Error_Triggers.getMsg());
                    return false;
                }

                reward.getTriggers().add(trigger);
            }
            case REWARD_CHANGE_COMMANDS -> reward.getCommands().add(msg);
        }
        reward.getArenaConfig().getRewardManager().save();
        return true;
    }
}
