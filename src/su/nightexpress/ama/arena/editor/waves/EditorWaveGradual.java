package su.nightexpress.ama.arena.editor.waves;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.editor.EditorUtils;
import su.nexmedia.engine.api.menu.*;
import su.nexmedia.engine.utils.ItemUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.arena.wave.IArenaWaveManager;
import su.nightexpress.ama.editor.ArenaEditorHandler;
import su.nightexpress.ama.editor.ArenaEditorType;

public class EditorWaveGradual extends AbstractMenu<AMA> {

    private final IArenaWaveManager waveManager;

    public EditorWaveGradual(@NotNull IArenaWaveManager waveManager) {
        super(waveManager.getArena().plugin(), ArenaEditorHandler.YML_ARENA_WAVE_GRADUAL, "");
        this.waveManager = waveManager;

        IMenuClick click = (player, type, e) -> {
            if (type instanceof MenuItemType type2) {
                if (type2 == MenuItemType.RETURN) {
                    waveManager.getEditor().open(player, 1);
                }
            }
            else if (type instanceof ArenaEditorType type2) {
                switch (type2) {
                    case WAVES_CHANGE_GRADUAL_ENABLED -> {
                        waveManager.setGradualSpawnEnabled(!waveManager.isGradualSpawnEnabled());
                        waveManager.save();
                        this.open(player, 1);
                        return;
                    }
                    case WAVES_CHANGE_GRADUAL_FIRST_PERCENT -> EditorUtils.tipCustom(player, plugin.lang().Editor_Arena_Waves_Enter_Gradual_First_Percent.getMsg());
                    case WAVES_CHANGE_GRADUAL_NEXT_PERCENT -> EditorUtils.tipCustom(player, plugin.lang().Editor_Arena_Waves_Enter_Gradual_Next_Percent.getMsg());
                    case WAVES_CHANGE_GRADUAL_NEXT_INTERVAL -> EditorUtils.tipCustom(player, plugin.lang().Editor_Arena_Waves_Enter_Gradual_Next_Interval.getMsg());
                    case WAVES_CHANGE_GRADUAL_NEXT_KILL_PERCENT -> EditorUtils.tipCustom(player, plugin.lang().Editor_Arena_Waves_Enter_Gradual_Next_KillPercent.getMsg());
                    default -> {return;}
                }
                plugin.getEditorHandlerNew().startEdit(player, waveManager, type2);
                player.closeInventory();
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
    @Nullable
    public MenuItemDisplay onItemDisplayPrepare(@NotNull Player player, @NotNull IMenuItem menuItem) {
        if (menuItem.getType() instanceof ArenaEditorType type2) {
            if (type2 == ArenaEditorType.WAVES_CHANGE_GRADUAL_ENABLED) {
                return menuItem.getDisplay(String.valueOf(waveManager.isGradualSpawnEnabled() ? 1 : 0));
            }
        }
        return super.onItemDisplayPrepare(player, menuItem);
    }

    @Override
    public void onItemPrepare(@NotNull Player player, @NotNull IMenuItem menuItem, @NotNull ItemStack item) {
        super.onItemPrepare(player, menuItem, item);
        ItemUT.replace(item, waveManager.replacePlaceholders());
    }

    @Override
    public boolean cancelClick(@NotNull SlotType slotType, int slot) {
        return true;
    }
}
