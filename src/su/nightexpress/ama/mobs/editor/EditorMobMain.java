package su.nightexpress.ama.mobs.editor;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.editor.EditorUtils;
import su.nexmedia.engine.api.menu.AbstractMenu;
import su.nexmedia.engine.api.menu.IMenuClick;
import su.nexmedia.engine.api.menu.IMenuItem;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.utils.CollectionsUT;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.NumberUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.editor.ArenaEditorType;
import su.nightexpress.ama.mobs.ArenaCustomMob;
import su.nightexpress.ama.mobs.ArenaMobHealthBar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class EditorMobMain extends AbstractMenu<AMA> {

    private final ArenaCustomMob mob;

    public EditorMobMain(@NotNull ArenaCustomMob mob) {
        super(mob.plugin(), JYML.loadOrExtract(mob.plugin(), "/editor/mob/main.yml"), "");
        this.mob = mob;

        IMenuClick menuClick = (player, type, e) -> {
            if (type instanceof MenuItemType type2) {
                if (type2 == MenuItemType.RETURN) {
                    plugin.getEditor().getMobEditor().open(player, 1);
                }
            }
            else if (type instanceof ArenaEditorType type2) {
                switch (type2) {
                    case MOB_CHANGE_NAME -> {
                        if (e.isRightClick()) {
                            mob.setNameVisible(!mob.isNameVisible());
                            break;
                        }
                        plugin.getEditorHandlerNew().startEdit(player, mob, type2);
                        EditorUtils.tipCustom(player, plugin.lang().Editor_Mob_Enter_Name.getMsg());
                        player.closeInventory();
                        return;
                    }
                    case MOB_CHANGE_ENTITY_TYPE -> {
                        plugin.getEditorHandlerNew().startEdit(player, mob, type2);
                        EditorUtils.tipCustom(player, plugin.lang().Editor_Mob_Enter_Type.getMsg());
                        EditorUtils.sendClickableTips(player, Stream.of(EntityType.values())
                            .filter(EntityType::isSpawnable).filter(EntityType::isAlive).map(Enum::name).toList());
                        player.closeInventory();
                        return;
                    }
                    case MOB_CHANGE_LEVEL -> {
                        if (e.isLeftClick()) type2 = ArenaEditorType.MOB_CHANGE_LEVEL_MIN;
                        else type2 = ArenaEditorType.MOB_CHANGE_LEVEL_MAX;

                        plugin.getEditorHandlerNew().startEdit(player, mob, type2);
                        EditorUtils.tipCustom(player, plugin.lang().Editor_Mob_Enter_Create.getMsg());
                        player.closeInventory();
                        return;
                    }
                    case MOB_CHANGE_BOSSBAR -> {
                        ArenaMobHealthBar healthBar = mob.getHealthBar();
                        if (e.isShiftClick()) {
                            if (e.isLeftClick()) {
                                healthBar.setColor(CollectionsUT.toggleEnum(healthBar.getColor()));
                            }
                            else if (e.isRightClick()) {
                                healthBar.setStyle(CollectionsUT.toggleEnum(healthBar.getStyle()));
                            }
                        }
                        else {
                            if (e.isLeftClick()) {
                                healthBar.setEnabled(!healthBar.isEnabled());
                            }
                            else if (e.isRightClick()) {
                                plugin.getEditorHandlerNew().startEdit(player, mob, ArenaEditorType.MOB_CHANGE_BOSSBAR_TITLE);
                                EditorUtils.tipCustom(player, plugin.lang().Editor_Mob_Enter_Create.getMsg());
                                player.closeInventory();
                                return;
                            }
                        }
                    }
                    case MOB_CHANGE_ATTRIBUTES -> {
                        if (e.isShiftClick()) {
                            if (e.isLeftClick()) {
                                mob.getAttributes().clear();
                            }
                            break;
                        }

                        if (e.isLeftClick()) type2 = ArenaEditorType.MOB_CHANGE_ATTRIBUTES_BASE;
                        else type2 = ArenaEditorType.MOB_CHANGE_ATTRIBUTES_LEVEL;

                        plugin.getEditorHandlerNew().startEdit(player, mob, type2);
                        EditorUtils.tipCustom(player, plugin.lang().Editor_Mob_Enter_Attribute.getMsg());
                        EditorUtils.sendSuggestTips(player, Stream.of(Attribute.values()).map(Enum::name).toList());
                        player.closeInventory();
                        return;
                    }
                    case MOB_CHANGE_EQUIPMENT -> {
                        new ArmorEditor(mob).open(player, 1);
                        return;
                    }
                    case MOB_CHANGE_SETTINGS_BABY -> mob.setBaby(!mob.isBaby());
                    case MOB_CHANGE_SETTINGS_HORSE -> {
                        if (e.isLeftClick()) {
                            mob.setHorseColor(CollectionsUT.toggleEnum(mob.getHorseColor()));
                        }
                        else {
                            mob.setHorseStyle(CollectionsUT.toggleEnum(mob.getHorseStyle()));
                        }
                    }
                    case MOB_CHANGE_SETTINGS_CREEPER -> mob.setCreeperCharged(!mob.isCreeperCharged());
                    case MOB_CHANGE_SETTINGS_SLIME -> {
                        plugin.getEditorHandlerNew().startEdit(player, mob, type2);
                        EditorUtils.tipCustom(player, plugin.lang().Editor_Mob_Enter_Slime_Size.getMsg());
                        player.closeInventory();
                        return;
                    }
                    case MOB_CHANGE_SETTINGS_PARROT -> mob.setParrotVariant(CollectionsUT.toggleEnum(mob.getParrotVariant()));
                    case MOB_CHANGE_SETTINGS_LLAMA -> mob.setLlamaColor(CollectionsUT.toggleEnum(mob.getLlamaColor()));
                    case MOB_CHANGE_SETTINGS_SHEEP -> mob.setSheepColor(CollectionsUT.toggleEnum(mob.getSheepColor()));
                    case MOB_CHANGE_SETTINGS_RABBIT -> mob.setRabbitType(CollectionsUT.toggleEnum(mob.getRabbitType()));
                    case MOB_CHANGE_SETTINGS_CAT -> mob.setCatType(CollectionsUT.toggleEnum(mob.getCatType()));
                    case MOB_CHANGE_SETTINGS_MUSHROOM_COW -> mob.setMushroomVariant(CollectionsUT.toggleEnum(mob.getMushroomVariant()));
                    case MOB_CHANGE_SETTINGS_WOLF -> mob.setWolfAngry(!mob.isWolfAngry());
                    case MOB_CHANGE_SETTINGS_VILLAGER -> mob.setVillagerProfession(CollectionsUT.toggleEnum(mob.getVillagerProfession()));
                }
                mob.save();
                this.open(player, 1);
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
    public void onPrepare(@NotNull Player player, @NotNull Inventory inventory) {

    }

    @Override
    public void onReady(@NotNull Player player, @NotNull Inventory inventory) {

    }

    @Override
    public void onItemPrepare(@NotNull Player player, @NotNull IMenuItem menuItem, @NotNull ItemStack item) {
        super.onItemPrepare(player, menuItem, item);
        ItemUT.replace(item, mob.replacePlaceholders());

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        List<String> lore = meta.getLore();
        if (lore == null) return;

        Map<Attribute, double[]> attributes = this.mob.getAttributes();

        List<String> lore2 = new ArrayList<>();
        for (String line : lore) {
            if (line.contains(ArenaCustomMob.PLACEHOLDER_ATTRIBUTE_BASE_NAME)) {
                for (Map.Entry<Attribute, double[]> e : attributes.entrySet()) {
                    lore2.add(line
                        .replace(ArenaCustomMob.PLACEHOLDER_ATTRIBUTE_BASE_NAME, e.getKey().name())
                        .replace(ArenaCustomMob.PLACEHOLDER_ATTRIBUTE_BASE_VALUE, NumberUT.format(e.getValue()[0]))
                    );
                }
                continue;
            }
            if (line.contains(ArenaCustomMob.PLACEHOLDER_ATTRIBUTE_LEVEL_NAME)) {
                for (Map.Entry<Attribute, double[]> e : attributes.entrySet()) {
                    lore2.add(line
                        .replace(ArenaCustomMob.PLACEHOLDER_ATTRIBUTE_LEVEL_NAME, e.getKey().name())
                        .replace(ArenaCustomMob.PLACEHOLDER_ATTRIBUTE_LEVEL_VALUE, NumberUT.format(e.getValue()[1]))
                    );
                }
                continue;
            }
            lore2.add(line);
        }

        meta.setLore(lore2);
        item.setItemMeta(meta);
    }

    @Override
    public boolean cancelClick(@NotNull SlotType slotType, int slot) {
        return true;
    }

    static class ArmorEditor extends AbstractMenu<AMA> {

        private final ArenaCustomMob mob;

        public ArmorEditor(@NotNull ArenaCustomMob mob) {
            super(mob.plugin(), "Kit Content", 9);
            this.mob = mob;
        }

        @Override
        public void onPrepare(@NotNull Player player, @NotNull Inventory inventory) {
            inventory.setContents(this.mob.getEquipment());
        }

        @Override
        public void onReady(@NotNull Player player, @NotNull Inventory inventory) {

        }

        @Override
        public void onClose(@NotNull Player player, @NotNull InventoryCloseEvent e) {
            Inventory inventory = e.getInventory();
            ItemStack[] items = new ItemStack[4];

            for (int slot = 0; slot < items.length; slot++) {
                ItemStack item = inventory.getItem(slot);
                if (item == null) continue;

                items[slot] = item;
            }

            this.mob.setEquipment(items);
            this.mob.save();
            super.onClose(player, e);

            plugin.runTask(c -> this.mob.getEditor().open(player, 1), false);
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
