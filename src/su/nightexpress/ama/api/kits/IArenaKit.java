package su.nightexpress.ama.api.kits;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.ConfigHolder;
import su.nexmedia.engine.api.manager.ICleanable;
import su.nexmedia.engine.api.manager.IEditable;
import su.nexmedia.engine.api.manager.IPlaceholder;
import su.nexmedia.engine.api.menu.IMenu;
import su.nexmedia.engine.utils.NumberUT;
import su.nexmedia.engine.utils.PlayerUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.Perms;
import su.nightexpress.ama.arena.ArenaPlayer;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;

public interface IArenaKit extends ConfigHolder, IEditable, ICleanable, IPlaceholder {

    String PLACEHOLDER_ID = "%kit_id%";
    String PLACEHOLDER_NAME = "%kit_name%";
    String PLACEHOLDER_PERMISSION = "%kit_permission%";
    String PLACEHOLDER_DEFAULT = "%kit_is_default%";
    String PLACEHOLDER_IS_PERMISSION = "%kit_is_permission%";
    String PLACEHOLDER_COMMANDS = "%kit_commands%";
    String PLACEHOLDER_POTION_EFFECTS = "%kit_potion_effects%";
    String PLACEHOLDER_COST = "%kit_cost%";
    String PLACEHOLDER_IS_AVAILABLE = "%kit_is_available%";
    String PLACEHOLDER_ICON_LORE = "%kit_icon_lore%";

    @Override
    @NotNull
    default UnaryOperator<String> replacePlaceholders() {
        ItemMeta meta = this.getIcon().getItemMeta();
        List<String> iconLore = meta != null ? meta.getLore() : Collections.emptyList();

        return str -> str
                .replace(PLACEHOLDER_ID, this.getId())
                .replace(PLACEHOLDER_NAME, this.getName())
                .replace(PLACEHOLDER_PERMISSION, Perms.KIT + this.getId())
                .replace(PLACEHOLDER_DEFAULT, plugin().lang().getBool(this.isDefault()))
                .replace(PLACEHOLDER_IS_PERMISSION, plugin().lang().getBool(this.isPermissionRequired()))
                .replace(PLACEHOLDER_COMMANDS, String.join("\n", this.getCommands()))
                .replace(PLACEHOLDER_POTION_EFFECTS, String.join("\n", this.getPotionEffects()
                    .stream().map(effect -> effect.getType().getName() + " " + NumberUT.toRoman(effect.getAmplifier() + 1)).toList()))
                .replace(PLACEHOLDER_COST, String.valueOf(this.getCost()))
                .replace(PLACEHOLDER_ICON_LORE, iconLore != null ? (String.join("\n", iconLore)) : "")
                ;
    }

    default boolean hasPermission(@NotNull Player player) {
        return !this.isPermissionRequired() || player.hasPermission(Perms.KIT + this.getId());
    }

    default void applyPotionEffects(@NotNull Player player) {
        this.getPotionEffects().forEach(potion -> {
            PotionEffect has = player.getPotionEffect(potion.getType());
            if (has != null && has.getAmplifier() >= potion.getAmplifier()) {
                return;
            }
            player.removePotionEffect(potion.getType());
            player.addPotionEffect(potion);
        });
    }

    default void give(@NotNull ArenaPlayer arenaPlayer) {
        Player player = arenaPlayer.getPlayer();
        player.getInventory().setContents(this.getItems());
        player.getInventory().setArmorContents(this.getArmor());

        this.getCommands().forEach(cmd -> PlayerUT.execCmd(player, cmd));

        arenaPlayer.setKit(this);
    }

    boolean isAvailable(@NotNull ArenaPlayer arenaPlayer, boolean isMsg);

    boolean buy(@NotNull ArenaPlayer arenaPlayer);

    @NotNull AMA plugin();

    @NotNull String getId();

    boolean isDefault();

    void setDefault(boolean isDefault);

    @NotNull String getName();

    void setName(@NotNull String name);

    @NotNull ItemStack getIcon();

    void setIcon(@NotNull ItemStack icon);

    int getCost();

    void setCost(int cost);

    boolean isPermissionRequired();

    void setPermissionRequired(boolean isPermissionRequired);

    @NotNull List<String> getCommands();

    void setCommands(@NotNull List<String> commands);

    @NotNull Set<PotionEffect> getPotionEffects();

    void setPotionEffects(@NotNull Set<PotionEffect> potionEffects);

    @NotNull ItemStack[] getArmor();

    void setArmor(@NotNull ItemStack[] armor);

    @NotNull ItemStack[] getItems();

    void setItems(@NotNull ItemStack[] items);

    @NotNull IMenu getPreview();
}
