package su.nightexpress.ama.arena.editor.regions;

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
import su.nightexpress.ama.api.arena.region.IArenaRegionContainer;
import su.nightexpress.ama.editor.ArenaEditorHandler;
import su.nightexpress.ama.editor.ArenaEditorType;

import java.util.ArrayList;
import java.util.List;

public class EditorRegionContainerSettings extends AbstractMenu<AMA> {

    private final IArenaRegionContainer container;

    public EditorRegionContainerSettings(@NotNull IArenaRegionContainer container) {
        super(container.getRegion().plugin(), ArenaEditorHandler.YML_ARENA_REGION_CONTAINER_SETTINGS, "");
        this.container = container;

        IMenuClick click = (player, type, e) -> {
            if (type instanceof MenuItemType type2) {
                if (type == MenuItemType.RETURN) {
                    if (!(container.getRegion().getEditor() instanceof EditorRegionMain editorRegion)) return;
                    editorRegion.getContainerList().open(player, 1);
                }
            }
            else if (type instanceof ArenaEditorType type2) {
                switch (type2) {
                    case REGION_CHANGE_CONTAINER_REFILL_AMOUNT -> {
                        type2 = e.isLeftClick() ? ArenaEditorType.REGION_CHANGE_CONTAINER_REFILL_AMOUNT_MIN : ArenaEditorType.REGION_CHANGE_CONTAINER_REFILL_AMOUNT_MAX;
                        plugin.getEditorHandlerNew().startEdit(player, container, type2);
                        EditorUtils.tipCustom(player, plugin.lang().Editor_Region_Container_Enter_Amount.getMsg());
                        player.closeInventory();
                    }
                    case REGION_CHANGE_CONTAINER_REFILL_TRIGGERS -> {
                        if (e.isRightClick()) {
                            container.getTriggers().clear();
                            break;
                        }
                        plugin.getEditorHandlerNew().startEdit(player, container, type2);
                        EditorUtils.tipCustom(player, plugin.lang().Editor_Enter_Triggers.getMsg());
                        plugin.lang().Editor_Tip_Triggers.send(player);
                        EditorUtils.sendSuggestTips(player, CollectionsUT.getEnumsList(ArenaGameEventType.class));
                        player.closeInventory();
                    }
                    case REGION_CHANGE_CONTAINER_ITEMS -> new ContainerGUI(container).open(player, 1);
                }
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
        ItemUT.replace(item, this.container.replacePlaceholders());
    }

    @Override
    public boolean cancelClick(@NotNull SlotType slotType, int slot) {
        return true;
    }


    static class ContainerGUI extends AbstractMenu<AMA> {

        private final IArenaRegionContainer container;

        public ContainerGUI(@NotNull IArenaRegionContainer container) {
            super(container.getRegion().plugin(), "Container Content", 27);
            this.container = container;
        }

        @Override
        public void onPrepare(@NotNull Player player, @NotNull Inventory inventory) {
            inventory.setContents(this.container.getItems().toArray(new ItemStack[this.getSize()]));
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
            this.container.setItems(items);
            this.container.getRegion().save();
            super.onClose(player, e);

            plugin.runTask(c -> this.container.getEditor().open(player, 1), false);
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
