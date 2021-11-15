package su.nightexpress.ama.arena.setup;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.utils.CollectionsUT;
import su.nexmedia.engine.utils.DataUT;
import su.nightexpress.ama.editor.ArenaEditorHandler;

public enum SetupItemType {

    REGION_CUBOID,
    REGION_SPAWN,
    REGION_SPAWNER,
    REGION_CONTAINER,
    REGION_HOLOGRAM,
    REGION_SAVE,

    SPOT_CUBOID,
    SPOT_STATE_PREVIEW,
    SPOT_STATE_EXIT,
    SPOT_SAVE,

    ARENA_LOCATION_LOBBY,
    ARENA_LOCATION_SPECTATE,
    ARENA_LOCATION_LEAVE,
    ARENA_EXIT,
    ;


    private final ItemStack item;

    SetupItemType() {
        this.item = ArenaEditorHandler.SETUP_ITEMS.getItem(this.name());
        DataUT.setData(this.item, ArenaSetupManager.KEY_ITEM_TYPE, this.name());
    }

    @NotNull
    public ItemStack getItem() {
        return item;
    }

    @Nullable
    public static SetupItemType getType(@NotNull ItemStack item) {
        String raw = DataUT.getStringData(item, ArenaSetupManager.KEY_ITEM_TYPE);
        return raw == null ? null : CollectionsUT.getEnum(raw, SetupItemType.class);
    }
}
