package su.nightexpress.ama.api.arena.config;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.manager.ConfigHolder;
import su.nexmedia.engine.api.manager.IEditable;
import su.nexmedia.engine.api.manager.ILoadable;
import su.nexmedia.engine.hooks.external.VaultHK;
import su.nexmedia.engine.utils.NumberUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.Perms;
import su.nightexpress.ama.api.arena.IArena;
import su.nightexpress.ama.api.arena.game.IArenaGameplayManager;
import su.nightexpress.ama.api.arena.region.IArenaRegionManager;
import su.nightexpress.ama.api.arena.reward.IArenaRewardManager;
import su.nightexpress.ama.api.arena.shop.IArenaShopManager;
import su.nightexpress.ama.api.arena.spot.IArenaSpotManager;
import su.nightexpress.ama.api.arena.type.ArenaLocationType;
import su.nightexpress.ama.api.arena.wave.IArenaWaveManager;

import java.util.function.UnaryOperator;

public interface IArenaConfig extends ConfigHolder, ILoadable, IEditable, IProblematic {

    String PLACEHOLDER_ID = "%arena_id%";
    String PLACEHOLDER_NAME = "%arena_name%";
    String PLACEHOLDER_REQUIREMENT_PERMISSION = "%arena_requirement_permission%";
    String PLACEHOLDER_REQUIREMENT_MONEY = "%arena_requirement_money%";
    String PLACEHOLDER_PERMISSION = "%arena_permission%";

    @Override
    @NotNull
    default UnaryOperator<String> replacePlaceholders() {
        return str -> IProblematic.super.replacePlaceholders().apply(str
                .replace(PLACEHOLDER_ID, this.getId())
                .replace(PLACEHOLDER_NAME, this.getName())
                .replace(PLACEHOLDER_PERMISSION, Perms.ARENA + this.getId())
                .replace(PLACEHOLDER_REQUIREMENT_PERMISSION, plugin().lang().getBool(this.isPermissionRequired()))
                .replace(PLACEHOLDER_REQUIREMENT_MONEY, NumberUT.format(this.getJoinMoneyRequired()))
        );
    }

    @NotNull AMA plugin();

    @NotNull IArena getArena();

    @NotNull String getId();

    boolean isActive();

    void setActive(boolean isActive);

    @NotNull
    String getName();

    boolean isPermissionRequired();

    void setPermissionRequired(boolean isPermissionRequired);

    double getJoinMoneyRequired();

    void setJoinMoneyRequired(double joinMoneyRequired);

    default boolean checkJoinRequirements(@NotNull IArena arena, @NotNull Player player) {
        if (this.isPermissionRequired() && !this.getArena().hasPermission(player)) return false;
        if (this.getJoinMoneyRequired() > 0D) {
            if (plugin().getEconomy().getBalance(player) < this.getJoinMoneyRequired()) return false;
        }

        return true;
    }

    default void payJoinRequirements(@NotNull IArena arena, @NotNull Player player) {
        if (this.getJoinMoneyRequired() > 0D) {
            VaultHK vault = plugin().getVault();
            if (vault != null && vault.hasEconomy()) vault.take(player, this.getJoinMoneyRequired());
        }
    }

    @Nullable
    Location getLocation(@NotNull ArenaLocationType locationType);

    void setLocation(@NotNull ArenaLocationType locationType, @Nullable Location location);

    @NotNull IArenaWaveManager getWaveManager();

    @NotNull IArenaRegionManager getRegionManager();

    @NotNull IArenaGameplayManager getGameplayManager();

    @NotNull IArenaSpotManager getSpotManager();

    @NotNull IArenaShopManager getShopManager();

    @NotNull IArenaRewardManager getRewardManager();
}
