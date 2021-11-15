package su.nightexpress.ama.editor.handler.arena.shop;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.manager.editor.EditorManager;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.arena.shop.ArenaShopManager;
import su.nightexpress.ama.arena.shop.ArenaShopProduct;
import su.nightexpress.ama.editor.ArenaEditorType;
import su.nightexpress.ama.editor.ArenaInputHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class HandlerShopManager extends ArenaInputHandler<ArenaShopManager> {

    public HandlerShopManager(@NotNull AMA plugin) {
        super(plugin);
    }

    @Override
    public boolean onType(
            @NotNull Player player, @NotNull ArenaShopManager shopManager,
            @NotNull ArenaEditorType type, @NotNull String msg) {

        if (type == ArenaEditorType.SHOP_PRODUCT_CREATE) {
            String id = EditorManager.fineId(msg);
            boolean hasProduct = shopManager.getProducts().stream().anyMatch(product -> product.getId().equalsIgnoreCase(id));
            if (hasProduct) {
                EditorManager.errorCustom(player, plugin.lang().Editor_Arena_Shop_Error_Product_Exist.getMsg());
                return false;
            }

            ArenaShopProduct product = new ArenaShopProduct(
                    shopManager,
                    id,
                    10,
                    new HashMap<>(), // Unlock triggers
                    new HashSet<>(), // Kit Requirements
                    new ItemStack(Material.APPLE),
                    new ArrayList<>(), // Commands
                    new ArrayList<>() // Items
            );

            shopManager.getProducts().add(product);
        }

        shopManager.save();
        return true;
    }
}
