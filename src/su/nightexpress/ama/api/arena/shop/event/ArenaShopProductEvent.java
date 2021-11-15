package su.nightexpress.ama.api.arena.shop.event;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.ama.api.arena.IArena;
import su.nightexpress.ama.api.arena.game.ArenaGameEventType;
import su.nightexpress.ama.api.arena.game.event.ArenaGameEventEvent;
import su.nightexpress.ama.api.arena.shop.IArenaShopProduct;

public class ArenaShopProductEvent extends ArenaGameEventEvent {

    private final IArenaShopProduct shopProduct;

    public ArenaShopProductEvent(@NotNull IArena arena, @NotNull ArenaGameEventType eventType, @NotNull IArenaShopProduct shopProduct) {
        super(arena, eventType);
        this.shopProduct = shopProduct;
    }

    @NotNull
    public IArenaShopProduct getShopProduct() {
        return shopProduct;
    }
}
