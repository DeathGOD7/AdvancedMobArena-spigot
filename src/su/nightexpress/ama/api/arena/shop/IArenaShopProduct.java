package su.nightexpress.ama.api.arena.shop;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.ICleanable;
import su.nexmedia.engine.api.manager.IEditable;
import su.nexmedia.engine.api.manager.IPlaceholder;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.NumberUT;
import su.nexmedia.engine.utils.PlayerUT;
import su.nightexpress.ama.api.arena.game.ArenaGameEventType;
import su.nightexpress.ama.api.arena.game.IArenaGameEventListener;
import su.nightexpress.ama.api.arena.game.IArenaGameEventTrigger;
import su.nightexpress.ama.api.arena.game.event.ArenaGameEventEvent;
import su.nightexpress.ama.api.arena.shop.event.ArenaShopProductEvent;
import su.nightexpress.ama.api.arena.type.ArenaLockState;
import su.nightexpress.ama.api.kits.IArenaKit;
import su.nightexpress.ama.arena.ArenaPlayer;

import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public interface IArenaShopProduct extends IArenaGameEventListener, ICleanable, IEditable, IPlaceholder {

    String PLACEHOLDER_ID = "%shop_product_id%";
    String PLACEHOLDER_NAME = "%shop_product_name%";
    String PLACEHOLDER_COMMANDS = "%shop_product_commands%";
    String PLACEHOLDER_APPLICABLE_KITS = "%shop_product_applicable_kits%";
    String PLACEHOLDER_PRICE = "%shop_product_price%";
    String PLACEHOLDER_ITEM_LORE = "%shop_product_item_lore%";
    String PLACEHOLDER_TRIGGERS_LOCKED = "%shop_product_triggers_locked%";
    String PLACEHOLDER_TRIGGERS_UNLOCKED = "%shop_product_triggers_unlocked%";

    @Override
    @NotNull
    default UnaryOperator<String> replacePlaceholders() {
        ItemStack preview = this.getPreview();
        List<String> itemLore = new ArrayList<>();

        ItemMeta meta = preview.getItemMeta();
        if (meta != null && meta.getLore() != null) itemLore.addAll(meta.getLore());

        String format = getShopManager().plugin().lang().Arena_Game_Trigger_Format_Full.getMsg();

        return str -> IArenaGameEventListener.super.replacePlaceholders().apply(str
                .replace(PLACEHOLDER_TRIGGERS_LOCKED, this.getStateTriggers(ArenaLockState.LOCKED).stream()
                        .map(trigger -> trigger.replacePlaceholders().apply(format))
                        .collect(Collectors.joining("\n")))
                .replace(PLACEHOLDER_TRIGGERS_UNLOCKED, this.getStateTriggers(ArenaLockState.UNLOCKED).stream()
                        .map(trigger -> trigger.replacePlaceholders().apply(format))
                        .collect(Collectors.joining("\n")))
                .replace(PLACEHOLDER_ID, this.getId())
                .replace(PLACEHOLDER_NAME, ItemUT.getItemName(this.getPreview()))
                .replace(PLACEHOLDER_PRICE, NumberUT.format(this.getPrice()))
                .replace(PLACEHOLDER_APPLICABLE_KITS, this.getApplicableKits().stream()
                        .map(kidId -> getShopManager().plugin().getKitManager().getKitById(kidId))
                        .filter(Objects::nonNull).map(IArenaKit::getName).collect(Collectors.joining(", ")))
                .replace(PLACEHOLDER_COMMANDS, String.join("\n", this.getCommands()))
                .replace(PLACEHOLDER_ITEM_LORE, String.join("\n", itemLore))
        );
    }

    @Override
    default boolean onGameEvent(@NotNull ArenaGameEventEvent gameEvent) {
        if (!this.isReady(gameEvent)) return false;

        ArenaLockState state = this.getState().getOpposite();
        this.setState(state);

        ArenaGameEventType eventType = state == ArenaLockState.LOCKED ? ArenaGameEventType.SHOP_ITEM_LOCKED : ArenaGameEventType.SHOP_ITEM_UNLOCKED;
        ArenaShopProductEvent regionEvent = new ArenaShopProductEvent(gameEvent.getArena(), eventType, this);
        getShopManager().plugin().getPluginManager().callEvent(regionEvent);

        return true;
    }

    default boolean isAvailable(@NotNull ArenaPlayer arenaPlayer) {
        if (!this.getApplicableKits().isEmpty()) {
            if (arenaPlayer.getArena().getConfig().getGameplayManager().isKitsEnabled()) {
                IArenaKit kit = arenaPlayer.getKit();
                if (kit == null || !this.getApplicableKits().contains(kit.getId())) {
                    return false;
                }
            }
        }

        return true;
    }

    @NotNull IArenaShopManager getShopManager();

    @NotNull String getId();

    @NotNull ArenaLockState getState();

    void setState(@NotNull ArenaLockState state);

    @NotNull
    Map<ArenaLockState, Set<IArenaGameEventTrigger>> getStateTriggers();

    @NotNull
    default Set<IArenaGameEventTrigger> getStateTriggers(@NotNull ArenaLockState state) {
        return this.getStateTriggers().computeIfAbsent(state, k -> new HashSet<>());
    }

    double getPrice();

    void setPrice(double price);

    @NotNull
    Set<String> getApplicableKits();

    void setApplicableKits(@NotNull Set<String> applicableKits);

    @NotNull
    ItemStack getPreview();

    void setPreview(@NotNull ItemStack preview);

    @NotNull
    List<ItemStack> getItems();

    void setItems(@NotNull List<ItemStack> items);

    @NotNull
    List<String> getCommands();

    void setCommands(@NotNull List<String> commands);

    default void give(@NotNull Player player) {
        this.getCommands().forEach(cmd -> PlayerUT.execCmd(player, cmd));
        this.getItems().forEach(item -> ItemUT.addItem(player, item));
    }
}
