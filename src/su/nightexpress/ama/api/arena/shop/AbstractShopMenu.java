package su.nightexpress.ama.api.arena.shop;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.menu.AbstractMenuAuto;
import su.nexmedia.engine.api.menu.IMenuClick;
import su.nexmedia.engine.api.menu.IMenuItem;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.arena.type.ArenaLockState;
import su.nightexpress.ama.api.arena.type.ArenaState;
import su.nightexpress.ama.arena.ArenaPlayer;
import su.nightexpress.ama.stats.StatType;

import java.util.*;

public abstract class AbstractShopMenu extends AbstractMenuAuto<AMA, IArenaShopProduct> {

    protected IArenaShopManager shopManager;

    private final Map<ArenaLockState, List<String>> productLore;

    public AbstractShopMenu(@NotNull IArenaShopManager shopManager, @NotNull JYML cfg, @NotNull String path) {
        super(shopManager.plugin(), cfg, path);
        this.shopManager = shopManager;

        this.objectSlots = cfg.getIntArray("Product.Slots");
        this.productLore = new HashMap<>();
        for (ArenaLockState lockState : ArenaLockState.values()) {
            this.productLore.put(lockState, StringUT.color(cfg.getStringList("Product.Lore." + lockState.name())));
        }

        IMenuClick click = (player, type, e) -> {
            if (type instanceof MenuItemType type2) {
                this.onItemClickDefault(player, type2);
            }
        };

        for (String sId : cfg.getSection("Content")) {
            IMenuItem menuItem = cfg.getMenuItem("Content." + sId, MenuItemType.class);

            if (menuItem.getType() != null) {
                menuItem.setClick(click);
            }
            this.addItem(menuItem);
        }
    }

    @Override
    @NotNull
    protected List<IArenaShopProduct> getObjects(@NotNull Player player) {
        ArenaPlayer arenaPlayer = plugin.getArenaManager().getArenaPlayer(player);
        if (arenaPlayer == null || arenaPlayer.getArena().getState() != ArenaState.INGAME) return Collections.emptyList();

        List<IArenaShopProduct> items = new ArrayList<>(this.shopManager.getProducts());
        if (shopManager.isHideOtherKitProducts()) {
            items.removeIf(item -> !item.isAvailable(arenaPlayer));
        }
        return items;
    }

    @Override
    @NotNull
    protected ItemStack getObjectStack(@NotNull Player player, @NotNull IArenaShopProduct shopProduct) {
        ItemStack item = shopProduct.getPreview();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        List<String> lore = new ArrayList<>(this.productLore.getOrDefault(shopProduct.getState(), Collections.emptyList()));
        lore.replaceAll(shopProduct.replacePlaceholders());

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    @Override
    @NotNull
    protected IMenuClick getObjectClick(@NotNull Player player, @NotNull IArenaShopProduct shopProduct) {
        ArenaPlayer arenaPlayer = plugin.getArenaManager().getArenaPlayer(player);
        if (arenaPlayer == null || arenaPlayer.getArena().getState() != ArenaState.INGAME) return (p, type, e) -> {};

        return (player2, type, e) -> {
            if (shopProduct.getState() == ArenaLockState.LOCKED) {
                plugin.lang().Shop_Buy_Error_Locked.send(player2);
                return;
            }
            if (!shopProduct.isAvailable(arenaPlayer)) {
                plugin.lang().Shop_Buy_Error_BadKit.send(player2);
                return;
            }

            double price = shopProduct.getPrice();
            double balance = plugin.getEconomy().getBalance(player2);
            if (balance < price) {
                plugin.lang().Shop_Buy_Error_NoMoney.replace(shopProduct.replacePlaceholders()).send(player2);
                return;
            }

            plugin.getEconomy().take(player2, price);
            arenaPlayer.addStats(StatType.COINS_SPENT, (int) price);
            // TODO Item Option for single buy
            shopProduct.give(player2);

            plugin.lang().Shop_Buy_Success.replace(shopProduct.replacePlaceholders()).send(player2);
        };
    }

    @Override
    public boolean cancelClick(@NotNull SlotType slotType, int slot) {
        return true;
    }
}
