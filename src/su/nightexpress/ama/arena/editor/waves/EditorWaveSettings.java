package su.nightexpress.ama.arena.editor.waves;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.menu.AbstractMenu;
import su.nexmedia.engine.api.menu.IMenuClick;
import su.nexmedia.engine.api.menu.IMenuItem;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.utils.ItemUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.arena.wave.IArenaWave;
import su.nightexpress.ama.editor.ArenaEditorHandler;
import su.nightexpress.ama.editor.ArenaEditorType;

public class EditorWaveSettings extends AbstractMenu<AMA> {

    private final IArenaWave arenaWave;

    private EditorWaveMobList          editorMobs;
    private EditorWaveAmplificatorList editorAmplificators;

    public EditorWaveSettings(@NotNull IArenaWave arenaWave) {
        super(arenaWave.getArena().plugin(), ArenaEditorHandler.YML_ARENA_WAVE_MAIN, "");
        this.arenaWave = arenaWave;

        IMenuClick click = (player, type, e) -> {
            if (type instanceof MenuItemType type2) {
                if (type2 == MenuItemType.RETURN) {
                    if (arenaWave.getArenaConfig().getWaveManager().getEditor() instanceof EditorWaveManager editorManager) {
                        editorManager.getEditorWaveList().open(player, 1);
                    }
                }
            }
            else if (type instanceof ArenaEditorType type2) {
                switch (type2) {
                    case WAVES_CHANGE_WAVE_MOBS -> this.getEditorMobs().open(player, 1);
                    case WAVES_CHANGE_WAVE_AMPLIFICATORS -> this.getEditorAmplificators().open(player, 1);
                    default -> {}
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
    public void clear() {
        if (this.editorMobs != null) {
            this.editorMobs.clear();
            this.editorMobs = null;
        }
        if (this.editorAmplificators != null) {
            this.editorAmplificators.clear();
            this.editorAmplificators = null;
        }
        super.clear();
    }

    @NotNull
    public EditorWaveMobList getEditorMobs() {
        if (this.editorMobs == null) {
            this.editorMobs = new EditorWaveMobList(this.arenaWave);
        }
        return editorMobs;
    }

    @NotNull
    public EditorWaveAmplificatorList getEditorAmplificators() {
        if (this.editorAmplificators == null) {
            this.editorAmplificators = new EditorWaveAmplificatorList(this.arenaWave);
        }
        return this.editorAmplificators;
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
        ItemUT.replace(item, this.arenaWave.replacePlaceholders());
    }

    @Override
    public boolean cancelClick(@NotNull SlotType slotType, int slot) {
        return true;
    }
}
