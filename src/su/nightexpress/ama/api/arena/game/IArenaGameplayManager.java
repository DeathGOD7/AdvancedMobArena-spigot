package su.nightexpress.ama.api.arena.game;

import org.bukkit.Material;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.ConfigHolder;
import su.nexmedia.engine.api.manager.IEditable;
import su.nexmedia.engine.api.manager.ILoadable;
import su.nexmedia.engine.commands.CommandRegister;
import su.nexmedia.engine.utils.Constants;
import su.nightexpress.ama.api.arena.config.IArenaObject;
import su.nightexpress.ama.api.arena.config.IProblematic;

import java.util.Map;
import java.util.Set;

public interface IArenaGameplayManager extends IArenaObject, ConfigHolder, ILoadable, IProblematic, IEditable {

    @NotNull Set<IArenaGameCommand> getAutoCommands();

    int getTimeleft();

    void setTimeleft(int timeleft);

    int getLobbyTime();

    void setLobbyTime(int lobbyTime);

    boolean isAnnouncesEnabled();

    void setAnnouncesEnabled(boolean isAnnouncesEnabled);

    boolean isScoreboardEnabled();

    void setScoreboardEnabled(boolean isScoreboardEnabled);

    boolean isShopEnabled();

    void setShopEnabled(boolean isShopEnabled);

    boolean isHungerEnabled();

    void setHungerEnabled(boolean isHungerEnabled);

    boolean isRegenerationEnabled();

    void setRegenerationEnabled(boolean isRegenerationEnabled);

    boolean isItemDropEnabled();

    void setItemDropEnabled(boolean isItemDropEnabled);

    boolean isItemPickupEnabled();

    void setItemPickupEnabled(boolean isItemPickupEnabled);

    boolean isItemDurabilityEnabled();

    void setItemDurabilityEnabled(boolean isItemDurabilityEnabled);

    boolean isSlimeSplitEnabled();

    void setSlimeSplitEnabled(boolean isSlimeSplitEnabled);

    boolean isMobDropExpEnabled();

    void setMobDropExpEnabled(boolean isMobDropExpEnabled);

    boolean isMobDropLootEnabled();

    void setMobDropLootEnabled(boolean isMobDropLootEnabled);

    @NotNull Set<Material> getBannedItems();

    int getPlayerMinAmount();

    void setPlayerMinAmount(int playerMinAmount);

    int getPlayerMaxAmount();

    void setPlayerMaxAmount(int playerMaxAmount);

    int getPlayerLivesAmount();

    void setPlayerLivesAmount(int playerLivesAmount);

    boolean isPlayerExpSavingEnabled();

    void setPlayerExpSavingEnabled(boolean isPlayerExpSavingEnabled);

    boolean isPlayerDropItemsOnDeathEnabled();

    void setPlayerDropItemsOnDeathEnabled(boolean isPlayerDropItemsOnDeath);

    boolean isPlayerCommandsEnabled();

    void setPlayerCommandsEnabled(boolean isPlayerCommandsEnabled);

    @NotNull Set<CreatureSpawnEvent.SpawnReason> getAllowedSpawnReasons();

    @NotNull Set<String> getPlayerCommandsAllowed();

    void setPlayerCommandsAllowed(@NotNull Set<String> playerCommandsAllowed);

    default boolean isPlayerCommandAllowed(@NotNull String cmd) {
        Set<String> aliases = CommandRegister.getAliases(cmd, true);
        return aliases.stream().anyMatch(alias -> this.getPlayerCommandsAllowed().contains(alias));
    }

    boolean isSpectateEnabled();

    void setSpectateEnabled(boolean isSpectateEnabled);

    boolean isSpectateOnDeathEnabled();

    void setSpectateOnDeathEnabled(boolean isSpectateOnDeathEnabled);

    boolean isKitsEnabled();

    void setKitsEnabled(boolean isKitsEnabled);

    @NotNull Set<String> getKitsAllowed();

    void setKitsAllowed(@NotNull Set<String> kitsAllowed);

    default boolean isKitAllowed(@NotNull String id) {
        return this.getKitsAllowed().contains(id) || this.getKitsAllowed().contains(Constants.MASK_ANY);
    }

    @NotNull Map<String, Integer> getKitsLimits();

    void setKitsLimits(@NotNull Map<String, Integer> kitsLimits);

    default int getKitLimit(@NotNull String id) {
        return this.getKitsLimits().getOrDefault(id, -1);
    }

    boolean isExternalPetsEnabled();

    void setExternalPetsEnabled(boolean isExternalPetsEnabled);

    boolean isExternalMcmmoEnabled();

    void setExternalMcmmoEnabled(boolean isExternalMcmmoEnabled);
}
