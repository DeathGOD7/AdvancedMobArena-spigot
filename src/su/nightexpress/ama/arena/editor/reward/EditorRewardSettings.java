package su.nightexpress.ama.arena.editor.reward;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.editor.EditorUtils;
import su.nexmedia.engine.api.menu.AbstractMenu;
import su.nexmedia.engine.api.menu.IMenuClick;
import su.nexmedia.engine.api.menu.IMenuItem;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.utils.CollectionsUT;
import su.nexmedia.engine.utils.ItemUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.arena.game.ArenaGameEventType;
import su.nightexpress.ama.api.arena.reward.IArenaReward;
import su.nightexpress.ama.editor.ArenaEditorHandler;
import su.nightexpress.ama.editor.ArenaEditorType;

import java.util.ArrayList;
import java.util.List;

public class EditorRewardSettings extends AbstractMenu<AMA> {

    private final IArenaReward reward;

    public EditorRewardSettings(@NotNull IArenaReward reward) {
        super(reward.plugin(), ArenaEditorHandler.YML_ARENA_REWARD_SETTINGS, "");
        this.reward = reward;

        IMenuClick click = (player, type, e) -> {
            if (type instanceof MenuItemType type2) {
                if (type == MenuItemType.RETURN) {
                    reward.getArenaConfig().getRewardManager().getEditor().open(player, 1);
                }
            }
            else if (type instanceof ArenaEditorType type2) {
                switch (type2) {
                    case REWARD_CHANGE_LATE -> reward.setLate(!reward.isLate());
                    case REWARD_CHANGE_NAME -> {
                        plugin.getEditorHandlerNew().startEdit(player, reward, type2);
                        //EditorManager.startEdit(player, reward, type2);
                        EditorUtils.tipCustom(player, plugin.lang().Editor_Reward_Enter_Name.getMsg());
                        player.closeInventory();
                        return;
                    }
                    case REWARD_CHANGE_ITEMS -> {
                        new RewardItems(reward).open(player, 1);
                        return;
                    }
                    case REWARD_CHANGE_CHANCE -> {
                        plugin.getEditorHandlerNew().startEdit(player, reward, type2);
                        EditorUtils.tipCustom(player, plugin.lang().Editor_Reward_Enter_Chance.getMsg());
                        player.closeInventory();
                        return;
                    }
                    case REWARD_CHANGE_COMMANDS -> {
                        if (e.isRightClick()) {
                            reward.getCommands().clear();
                            break;
                        }
                        plugin.getEditorHandlerNew().startEdit(player, reward, type2);
                        EditorUtils.tipCustom(player, plugin.lang().Editor_Reward_Enter_Command.getMsg());
                        EditorUtils.sendCommandTips(player);
                        player.closeInventory();
                        return;
                    }
                    case REWARD_CHANGE_TRIGGERS -> {
                        if (e.isRightClick()) {
                            reward.getTriggers().clear();
                            break;
                        }
                        plugin.getEditorHandlerNew().startEdit(player, reward, type2);
                        EditorUtils.tipCustom(player, plugin.lang().Editor_Enter_Triggers.getMsg());
                        plugin.lang().Editor_Tip_Triggers.send(player);
                        EditorUtils.sendSuggestTips(player, CollectionsUT.getEnumsList(ArenaGameEventType.class));
                        player.closeInventory();
                        return;
                    }
                    case REWARD_CHANGE_TARGET_TYPE -> reward.setTargetType(CollectionsUT.toggleEnum(reward.getTargetType()));
                }
                reward.getArenaConfig().getRewardManager().save();
                this.open(player, 1);
            }
        };

        for (String sId : cfg.getSection("Content")) {
            IMenuItem menuItem = cfg.getMenuItem("Content." + sId, MenuItemType.class);

            if (menuItem.getType() != null) {
                menuItem.setClick(click);
            }
            this.addItem(menuItem);
        }

        for (String sId : cfg.getSection("Editor")) {
            IMenuItem menuItem = cfg.getMenuItem("Editor." + sId, ArenaEditorType.class);

            if (menuItem.getType() != null) {
                menuItem.setClick(click);
            }
            this.addItem(menuItem);
        }
    }

    @Override
    public void onPrepare(@NotNull Player player, @NotNull Inventory inventory) {

    }

    @Override
    public void onReady(@NotNull Player player, @NotNull Inventory inventory) {

    }

    @Override
    public void onItemPrepare(@NotNull Player player, @NotNull IMenuItem menuItem, @NotNull ItemStack item) {
        super.onItemPrepare(player, menuItem, item);
        ItemUT.replace(item, this.reward.replacePlaceholders());
    }

    @Override
    public boolean cancelClick(@NotNull SlotType slotType, int slot) {
        return true;
    }

    static class RewardItems extends AbstractMenu<AMA> {

        private final IArenaReward reward;

        public RewardItems(@NotNull IArenaReward reward) {
            super(reward.plugin(), "Reward Items", 27);
            this.reward = reward;
        }

        @Override
        public void onPrepare(@NotNull Player player, @NotNull Inventory inventory) {
            inventory.setContents(this.reward.getItems().toArray(new ItemStack[this.getSize()]));
        }

        @Override
        public void onReady(@NotNull Player player, @NotNull Inventory inventory) {

        }

        @Override
        public void onClose(@NotNull Player player, @NotNull InventoryCloseEvent e) {
            Inventory inventory = e.getInventory();
            List<ItemStack> items = new ArrayList<>();

            for (int slot = 0; slot < this.getSize(); slot++) {
                ItemStack item = inventory.getItem(slot);
                if (item == null) continue;

                items.add(item);
            }
            this.reward.setItems(items);
            this.reward.getArenaConfig().getRewardManager().save();
            super.onClose(player, e);

            plugin.runTask(c -> this.reward.getEditor().open(player, 1), false);
        }

        @Override
        public boolean destroyWhenNoViewers() {
            return true;
        }

        @Override
        public boolean cancelClick(@NotNull SlotType slotType, int slot) {
            return false;
        }
    }
}
