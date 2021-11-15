package su.nightexpress.ama.api.arena.shop;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.ConfigHolder;
import su.nexmedia.engine.api.manager.IEditable;
import su.nexmedia.engine.api.manager.ILoadable;
import su.nightexpress.ama.api.arena.config.IArenaObject;
import su.nightexpress.ama.api.arena.config.IProblematic;

import java.util.Set;

public interface IArenaShopManager extends ConfigHolder, IArenaObject, ILoadable, IEditable, IProblematic {

    @NotNull
    AbstractShopMenu getShopMenu();

    boolean isLockedWhileMobsAlive();

    void setLockedWhileMobsAlive(boolean isLockedInWave);

    boolean isHideOtherKitProducts();

    void setHideOtherKitProducts(boolean isHideOtherKitItems);

    @NotNull Set<IArenaShopProduct> getProducts();

    void setProducts(@NotNull Set<IArenaShopProduct> products);

    boolean open(@NotNull Player player);
}
