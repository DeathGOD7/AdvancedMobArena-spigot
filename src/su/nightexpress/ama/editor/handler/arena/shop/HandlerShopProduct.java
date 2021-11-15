package su.nightexpress.ama.editor.handler.arena.shop;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.manager.editor.EditorManager;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.arena.game.IArenaGameEventTrigger;
import su.nightexpress.ama.api.arena.shop.IArenaShopProduct;
import su.nightexpress.ama.api.arena.type.ArenaLockState;
import su.nightexpress.ama.arena.game.trigger.AbstractArenaGameEventTrigger;
import su.nightexpress.ama.editor.ArenaEditorType;
import su.nightexpress.ama.editor.ArenaInputHandler;

public class HandlerShopProduct extends ArenaInputHandler<IArenaShopProduct> {

    public HandlerShopProduct(@NotNull AMA plugin) {
        super(plugin);
    }

    @Override
    public boolean onType(
            @NotNull Player player, @NotNull IArenaShopProduct product,
            @NotNull ArenaEditorType type, @NotNull String msg) {

        switch (type) {
            case SHOP_PRODUCT_CHANGE_PRICE -> {
                double price = StringUT.getDouble(msg, 0);
                product.setPrice(price);
            }
            case SHOP_PRODUCT_CHANGE_COMMANDS -> product.getCommands().add(StringUT.colorRaw(msg));
            case SHOP_PRODUCT_CHANGE_REQUIRED_KITS -> product.getApplicableKits().add(msg);
            case SHOP_PRODUCT_CHANGE_TRIGGERS_LOCKED, SHOP_PRODUCT_CHANGE_TRIGGERS_UNLOCKED -> {
                String[] split = StringUT.colorOff(msg).split(" ");
                if (split.length < 2) {
                    EditorManager.errorCustom(player, plugin.lang().Editor_Error_Triggers.getMsg());
                    return false;
                }

                IArenaGameEventTrigger trigger = AbstractArenaGameEventTrigger.parse(product.getShopManager().getArenaConfig(), split[0], split[1]);
                if (trigger == null) {
                    EditorManager.errorCustom(player, plugin.lang().Editor_Error_Triggers.getMsg());
                    return false;
                }

                ArenaLockState state = type == ArenaEditorType.SHOP_PRODUCT_CHANGE_TRIGGERS_LOCKED ? ArenaLockState.LOCKED : ArenaLockState.UNLOCKED;
                product.getStateTriggers(state).add(trigger);
            }
            default -> {return true;}
        }

        product.getShopManager().save();
        return true;
    }
}
