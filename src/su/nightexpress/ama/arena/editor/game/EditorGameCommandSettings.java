package su.nightexpress.ama.arena.editor.game;

import org.bukkit.entity.Player;
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
import su.nightexpress.ama.api.arena.game.IArenaGameCommand;
import su.nightexpress.ama.editor.ArenaEditorHandler;
import su.nightexpress.ama.editor.ArenaEditorType;

public class EditorGameCommandSettings extends AbstractMenu<AMA> {

    private final IArenaGameCommand gameCommand;

    public EditorGameCommandSettings(@NotNull IArenaGameCommand gameCommand) {
        super(gameCommand.plugin(), ArenaEditorHandler.YML_ARENA_GAME_COMMAND_SETTINGS, "");
        this.gameCommand = gameCommand;

        IMenuClick click = (player, type, e) -> {
            if (type instanceof MenuItemType type2) {
                if (type2 == MenuItemType.RETURN) {
                    if (!(gameCommand.getArenaConfig().getGameplayManager().getEditor() instanceof  EditorArenaGameplay editor)) return;
                    editor.getCommandList().open(player, 1);
                }
            }
            else if (type instanceof ArenaEditorType type2) {
                switch (type2) {
                    case GAME_COMMAND_CHANGE_COMMANDS -> {
                        if (e.isRightClick()) {
                            gameCommand.getCommands().clear();
                            break;
                        }
                        plugin.getEditorHandlerNew().startEdit(player, gameCommand, type2);
                        EditorUtils.tipCustom(player, plugin.lang().Editor_Reward_Enter_Command.getMsg());
                        EditorUtils.sendCommandTips(player);
                        player.closeInventory();
                        return;
                    }
                    case GAME_COMMAND_CHANGE_TARGET_TYPE -> gameCommand.setTargetType(CollectionsUT.toggleEnum(gameCommand.getTargetType()));
                    case GAME_COMMAND_CHANGE_TRIGGERS -> {
                        if (e.isRightClick()) {
                            gameCommand.getTriggers().clear();
                            break;
                        }
                        plugin.getEditorHandlerNew().startEdit(player, gameCommand, type2);
                        EditorUtils.tipCustom(player, plugin.lang().Editor_Enter_Triggers.getMsg());
                        plugin.lang().Editor_Tip_Triggers.send(player);
                        EditorUtils.sendSuggestTips(player, CollectionsUT.getEnumsList(ArenaGameEventType.class));
                        player.closeInventory();
                        return;
                    }
                }
                gameCommand.getArenaConfig().getGameplayManager().save();
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
        ItemUT.replace(item, this.gameCommand.replacePlaceholders());
    }

    @Override
    public boolean cancelClick(@NotNull SlotType slotType, int slot) {
        return true;
    }
}
