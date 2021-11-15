package su.nightexpress.ama.arena.shop;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.config.api.JYML;
import su.nightexpress.ama.api.arena.shop.AbstractShopMenu;
import su.nightexpress.ama.api.arena.shop.IArenaShopManager;

public class ArenaShopMenu extends AbstractShopMenu {

    public ArenaShopMenu(@NotNull IArenaShopManager shopManager, @NotNull JYML cfg) {
        super(shopManager, cfg, "");
    }

    @Override
    public void onReady(@NotNull Player player, @NotNull Inventory inventory) {

    }
}
