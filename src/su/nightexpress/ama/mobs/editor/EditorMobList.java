package su.nightexpress.ama.mobs.editor;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.editor.EditorUtils;
import su.nexmedia.engine.api.menu.AbstractMenuAuto;
import su.nexmedia.engine.api.menu.IMenuClick;
import su.nexmedia.engine.api.menu.IMenuItem;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.editor.ArenaEditorType;
import su.nightexpress.ama.mobs.ArenaCustomMob;
import su.nightexpress.ama.mobs.MobManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class EditorMobList extends AbstractMenuAuto<AMA, ArenaCustomMob> {

    private final MobManager mobManager;

    private final String objectName;
    private final List<String> objectLore;

    public EditorMobList(@NotNull MobManager mobManager) {
        super(mobManager.plugin(), JYML.loadOrExtract(mobManager.plugin(), "/editor/mob/list.yml"), "");
        this.mobManager = mobManager;

        this.objectName = StringUT.color(cfg.getString("Object.Name", ArenaCustomMob.PLACEHOLDER_ID));
        this.objectLore = StringUT.color(cfg.getStringList("Object.Lore"));

        IMenuClick menuClick = (player, type, e) -> {
            if (type instanceof MenuItemType type2) {
                if (type2 == MenuItemType.RETURN) {
                    plugin.getEditor().open(player, 1);
                }
                else this.onItemClickDefault(player, type2);
            }
            else if (type instanceof ArenaEditorType type2) {
                if (type2 == ArenaEditorType.MOB_CREATE) {
                    plugin.getEditorHandlerNew().startEdit(player, mobManager, type2);
                    EditorUtils.tipCustom(player, plugin.lang().Editor_Kit_Enter_Create.getMsg());
                    player.closeInventory();
                }
            }
        };

        for (String sId : cfg.getSection("Content")) {
            IMenuItem menuItem = cfg.getMenuItem("Content." + sId, MenuItemType.class);

            if (menuItem.getType() != null) {
                menuItem.setClick(menuClick);
            }
            this.addItem(menuItem);
        }

        for (String sId : cfg.getSection("Editor")) {
            IMenuItem menuItem = cfg.getMenuItem("Editor." + sId, ArenaEditorType.class);

            if (menuItem.getType() != null) {
                menuItem.setClick(menuClick);
            }
            this.addItem(menuItem);
        }
    }

    @Override
    @NotNull
    protected List<ArenaCustomMob> getObjects(@NotNull Player player) {
        return new ArrayList<>(plugin.getMobManager().getMobs().stream().sorted(Comparator.comparing(ArenaCustomMob::getId)).toList());
    }

    @Override
    @NotNull
    protected ItemStack getObjectStack(@NotNull Player player, @NotNull ArenaCustomMob mob) {
        Material material = Material.getMaterial(mob.getEntityType().name() + "_SPAWN_EGG");
        if (mob.getEntityType() == EntityType.MUSHROOM_COW) material = Material.MOOSHROOM_SPAWN_EGG;
        if (material == null) material = Material.BAT_SPAWN_EGG;

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.setDisplayName(this.objectName);
        meta.setLore(this.objectLore);
        item.setItemMeta(meta);

        ItemUT.replace(item, mob.replacePlaceholders());
        return item;
    }

    @Override
    @NotNull
    protected IMenuClick getObjectClick(@NotNull Player player, @NotNull ArenaCustomMob mob) {
        return (player1, type, e) -> {
            if (e.isShiftClick()) {
                if (mob.getFile().delete()) {
                    mob.clear();
                    this.mobManager.getMobsMap().remove(mob.getId());
                    this.open(player, this.getPage(player));
                }
                return;
            }
            mob.getEditor().open(player, 1);
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
