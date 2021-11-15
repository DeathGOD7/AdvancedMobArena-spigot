package su.nightexpress.ama.arena.editor.game;

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
import su.nightexpress.ama.api.arena.game.IArenaGameCommand;
import su.nightexpress.ama.api.arena.game.IArenaGameplayManager;
import su.nightexpress.ama.arena.game.ArenaGameCommand;
import su.nightexpress.ama.editor.ArenaEditorHandler;
import su.nightexpress.ama.editor.ArenaEditorType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class EditorGameCommandList extends AbstractMenuAuto<AMA, IArenaGameCommand> {

    private final IArenaGameplayManager gameplayManager;

    private final String       objectName;
    private final List<String> objectLore;

    public EditorGameCommandList(@NotNull IArenaGameplayManager gameplayManager) {
        super(gameplayManager.plugin(), ArenaEditorHandler.YML_ARENA_GAME_COMMAND_LIST, "");
        this.gameplayManager = gameplayManager;

        this.objectName = StringUT.color(cfg.getString("Object.Name", ""));
        this.objectLore = StringUT.color(cfg.getStringList("Object.Lore"));

        IMenuClick click = (player, type, e) -> {
            if (type instanceof MenuItemType type2) {
                if (type2 == MenuItemType.RETURN) {
                    gameplayManager.getEditor().open(player, 1);
                }
                else this.onItemClickDefault(player, type2);
            }
            else if (type instanceof ArenaEditorType type2) {
                if (type2 == ArenaEditorType.GAME_COMMAND_CREATE) {
                    IArenaGameCommand gameCommand = new ArenaGameCommand(
                            gameplayManager.getArenaConfig(), new HashSet<>(),
                            ArenaGameTargetType.GLOBAL, new ArrayList<>());
                    gameplayManager.getAutoCommands().add(gameCommand);
                    gameplayManager.save();
                    this.open(player, 1);
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
    @NotNull
    protected List<IArenaGameCommand> getObjects(@NotNull Player player) {
        return new ArrayList<>(this.gameplayManager.getAutoCommands());
    }

    @Override
    @NotNull
    protected ItemStack getObjectStack(@NotNull Player player, @NotNull IArenaGameCommand gameCommand) {
        ItemStack item = new ItemStack(Material.COMMAND_BLOCK);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.setDisplayName(this.objectName);
        meta.setLore(this.objectLore);
        item.setItemMeta(meta);
        ItemUT.replace(item, gameCommand.replacePlaceholders());

        return item;
    }

    @Override
    @NotNull
    protected IMenuClick getObjectClick(@NotNull Player player, @NotNull IArenaGameCommand gameCommand) {
        return (player1, type, e) -> {
            if (e.isShiftClick() && e.isRightClick()) {
                gameCommand.clear();
                this.gameplayManager.getAutoCommands().remove(gameCommand);
                this.gameplayManager.save();
                this.open(player1, this.getPage(player1));
                return;
            }
            gameCommand.getEditor().open(player1, 1);
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
