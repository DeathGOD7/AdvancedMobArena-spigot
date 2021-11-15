package su.nightexpress.ama.arena.editor.reward;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.menu.AbstractMenuAuto;
import su.nexmedia.engine.api.menu.IMenuClick;
import su.nexmedia.engine.api.menu.IMenuItem;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.arena.game.ArenaGameTargetType;
import su.nightexpress.ama.api.arena.reward.IArenaReward;
import su.nightexpress.ama.api.arena.reward.IArenaRewardManager;
import su.nightexpress.ama.arena.reward.ArenaReward;
import su.nightexpress.ama.editor.ArenaEditorHandler;
import su.nightexpress.ama.editor.ArenaEditorType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class EditorRewardList extends AbstractMenuAuto<AMA, IArenaReward> {

    private final IArenaRewardManager rewardManager;

    private final String       objectName;
    private final List<String> objectLore;

    public EditorRewardList(@NotNull IArenaRewardManager rewardManager) {
        super(rewardManager.plugin(), ArenaEditorHandler.YML_ARENA_REWARD_LIST, "");
        this.rewardManager = rewardManager;

        this.objectName = StringUT.color(cfg.getString("Object.Name", IArenaReward.PLACEHOLDER_NAME));
        this.objectLore = StringUT.color(cfg.getStringList("Object.Lore"));

        IMenuClick click = (player, type, e) -> {
            if (type instanceof MenuItemType type2) {
                if (type == MenuItemType.RETURN) {
                    rewardManager.getArenaConfig().getEditor().open(player, 1);
                }
                else this.onItemClickDefault(player, type2);
            }
            else if (type instanceof ArenaEditorType type2) {
                switch (type2) {
                    case REWARD_CREATE -> {
                        IArenaReward reward = new ArenaReward(rewardManager.getArenaConfig(), "New Reward",
                                        true, new HashSet<>(), ArenaGameTargetType.PLAYER_ALL, 0D,
                                        new ArrayList<>(), new ArrayList<>());
                        rewardManager.getRewards().add(reward);
                    }
                    case REWARDS_CHANGE_RETAIN -> {
                        if (e.isLeftClick()) rewardManager.setRetainOnDeath(!rewardManager.isRetainOnDeath());
                        else if (e.isRightClick()) rewardManager.setRetainOnLeave(!rewardManager.isRetainOnLeave());
                    }
                }
                rewardManager.save();
                this.open(player, this.getPage(player));
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
    @NotNull
    protected List<IArenaReward> getObjects(@NotNull Player player) {
        return new ArrayList<>(this.rewardManager.getRewards());
    }

    @Override
    @NotNull
    protected ItemStack getObjectStack(@NotNull Player player, @NotNull IArenaReward reward) {
        ItemStack item = new ItemStack(Material.GOLD_INGOT);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.setDisplayName(this.objectName);
        meta.setLore(this.objectLore);
        item.setItemMeta(meta);
        ItemUT.replace(item, reward.replacePlaceholders());

        return item;
    }

    @Override
    @NotNull
    protected IMenuClick getObjectClick(@NotNull Player player, @NotNull IArenaReward reward) {
        return (player1, type, e) -> {
            if (e.isShiftClick() && e.isRightClick()) {
                this.rewardManager.getRewards().remove(reward);
                reward.clear();
                this.rewardManager.save();
                this.open(player, this.getPage(player));
                return;
            }
            reward.getEditor().open(player, 1);
        };
    }

    @Override
    public void onReady(@NotNull Player player, @NotNull Inventory inventory) {

    }

    @Override
    public void onItemPrepare(@NotNull Player player, @NotNull IMenuItem menuItem, @NotNull ItemStack item) {
        super.onItemPrepare(player, menuItem, item);

        ItemUT.replace(item, line -> line
                .replace("%rewards_retain_on_death%", plugin.lang().getBool(rewardManager.isRetainOnDeath()))
                .replace("%rewards_retain_on_leave%", plugin.lang().getBool(rewardManager.isRetainOnLeave()))
        );
    }

    @Override
    public boolean cancelClick(@NotNull SlotType slotType, int slot) {
        return true;
    }
}
