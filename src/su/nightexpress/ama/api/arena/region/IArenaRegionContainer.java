package su.nightexpress.ama.api.arena.region;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.ICleanable;
import su.nexmedia.engine.api.manager.IEditable;
import su.nexmedia.engine.core.config.CoreConfig;
import su.nexmedia.engine.utils.NumberUT;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.ama.api.arena.game.IArenaGameEventListener;
import su.nightexpress.ama.api.arena.game.event.ArenaGameEventEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.UnaryOperator;

public interface IArenaRegionContainer extends IArenaGameEventListener, IEditable, ICleanable {

    String PLACEHOLDER_LOCATION_X = "%container_location_x%";
    String PLACEHOLDER_LOCATION_Y = "%container_location_y%";
    String PLACEHOLDER_LOCATION_Z = "%container_location_z%";
    String PLACEHOLDER_LOCATION_WORLD = "%container_location_world%";
    String PLACEHOLDER_REFILL_ITEMS_MIN = "%container_refill_items_min%";
    String PLACEHOLDER_REFILL_ITEMS_MAX = "%container_refill_items_max%";

    @Override
    @NotNull
    default UnaryOperator<String> replacePlaceholders() {
        Location location = this.getLocation();
        String world = location.getWorld() == null ? "null" : location.getWorld().getName();

        return str -> IArenaGameEventListener.super.replacePlaceholders().apply(str
                .replace(PLACEHOLDER_REFILL_ITEMS_MIN, String.valueOf(this.getMinItems()))
                .replace(PLACEHOLDER_REFILL_ITEMS_MAX, String.valueOf(this.getMaxItems()))
                .replace(PLACEHOLDER_LOCATION_X, NumberUT.format(location.getX()))
                .replace(PLACEHOLDER_LOCATION_Y, NumberUT.format(location.getY()))
                .replace(PLACEHOLDER_LOCATION_Z, NumberUT.format(location.getZ()))
                .replace(PLACEHOLDER_LOCATION_WORLD, CoreConfig.getWorldName(world))

        );
    }

    @NotNull IArenaRegion getRegion();

    @NotNull Chest getChest();

    @Override
    default boolean onGameEvent(@NotNull ArenaGameEventEvent gameEvent) {
        if (this.isReady(gameEvent)) {
            return this.refill();
        }
        return false;
    }

    int getMinItems();

    void setMinItems(int minItems);

    int getMaxItems();

    void setMaxItems(int maxItems);

    @NotNull List<ItemStack> getItems();

    void setItems(@NotNull List<ItemStack> items);

    @NotNull
    default Location getLocation() {
        return this.getChest().getLocation().clone();
    }

    default boolean refill() {
        List<ItemStack> items = new ArrayList<>(this.getItems());
        if (items.isEmpty()) return false;

        int min = Math.min(this.getMinItems(), this.getMaxItems());
        int max = Math.max(this.getMinItems(), this.getMaxItems());
        int roll = Math.min(items.size(), Rnd.get(min, max));
        if (roll <= 0) return false;

        Chest chest = this.getChest();
        chest.getChunk().load();

        Inventory inventory = chest.getInventory();

        Collections.shuffle(items);
        while (items.size() > roll) {
            items.remove(0);
        }
        while (items.size() < inventory.getSize()) {
            items.add(new ItemStack(Material.AIR));
        }
        Collections.shuffle(items);

        inventory.clear();
        inventory.setContents(items.toArray(new ItemStack[0]));
        return true;
    }
}
