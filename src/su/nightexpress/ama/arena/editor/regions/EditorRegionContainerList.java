package su.nightexpress.ama.arena.editor.regions;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
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
import su.nightexpress.ama.api.arena.region.IArenaRegion;
import su.nightexpress.ama.api.arena.region.IArenaRegionContainer;
import su.nightexpress.ama.editor.ArenaEditorHandler;
import su.nightexpress.ama.editor.ArenaEditorType;

import java.util.ArrayList;
import java.util.List;

public class EditorRegionContainerList extends AbstractMenuAuto<AMA, IArenaRegionContainer> {

    private final IArenaRegion region;

    private final String       objectName;
    private final List<String> objectLore;

    public EditorRegionContainerList(@NotNull IArenaRegion region) {
        super(region.plugin(), ArenaEditorHandler.YML_ARENA_REGION_CONTAINER_LIST, "");
        this.region = region;

        this.objectName = StringUT.color(cfg.getString("Object.Name", ""));
        this.objectLore = StringUT.color(cfg.getStringList("Object.Lore"));

        IMenuClick click = (player, type, e) -> {
            if (type instanceof MenuItemType type2) {
                if (type == MenuItemType.RETURN) {
                    region.getEditor().open(player, 1);
                }
                else this.onItemClickDefault(player, type2);
            }
            /*else if (type instanceof ArenaEditorType type2) {
                return;
            }*/
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
    protected List<IArenaRegionContainer> getObjects(@NotNull Player player) {
        return new ArrayList<>(this.region.getContainers());
    }

    @Override
    @NotNull
    protected ItemStack getObjectStack(@NotNull Player player, @NotNull IArenaRegionContainer container) {
        ItemStack item = new ItemStack(Material.CHEST);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.setDisplayName(this.objectName);
        meta.setLore(this.objectLore);
        item.setItemMeta(meta);
        ItemUT.replace(item, container.replacePlaceholders());

        return item;
    }

    @Override
    @NotNull
    protected IMenuClick getObjectClick(@NotNull Player player, @NotNull IArenaRegionContainer container) {
        return (player2, type, e) -> {
            if (e.getClick() == ClickType.MIDDLE) {
                player2.teleport(container.getLocation());
                return;
            }
            if (e.isShiftClick() && e.isRightClick()) {
                if (!this.region.getContainers().remove(container)) return;

                container.clear();
                this.region.save();
                this.open(player2, this.getPage(player2));
                return;
            }
            container.getEditor().open(player2, 1);
        };
    }

    @Override
    public void onReady(@NotNull Player player, @NotNull Inventory inventory) {

    }

    @Override
    public boolean cancelClick(@NotNull SlotType slotType, int slot) {
        return true;
    }
}
