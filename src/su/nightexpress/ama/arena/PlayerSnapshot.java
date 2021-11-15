package su.nightexpress.ama.arena;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.EntityUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.arena.IArena;
import su.nightexpress.ama.api.arena.type.ArenaLocationType;
import su.nightexpress.ama.api.arena.type.ArenaState;
import su.nightexpress.ama.config.Config;
import su.nightexpress.ama.hooks.external.EssentialsHK;
import su.nightexpress.ama.hooks.external.SunLightHK;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerSnapshot {

	private final Location                 location;
	private final ItemStack[]              inventory;
	private final ItemStack[]              armor;
	private final float                    exp;
	private final int                      level;
	private final Collection<PotionEffect> effects;
	private final GameMode                 gameMode;

	private static final Map<UUID, PlayerSnapshot> SNAPSHOTS  = new HashMap<>();
	private static final EssentialsHK              ESSENTIALS = AMA.getPlugin(AMA.class).getHook(EssentialsHK.class);
	private static final SunLightHK                SUNLIGHT   = AMA.getPlugin(AMA.class).getHook(SunLightHK.class);
	
	PlayerSnapshot(@NotNull ArenaPlayer arenaPlayer) {
		Player player = arenaPlayer.getPlayer();
		
		this.location = player.getLocation();
		this.inventory = player.getInventory().getContents();
		this.armor = player.getInventory().getArmorContents();
		this.exp = player.getExp();
		this.level = player.getLevel();
		this.effects = player.getActivePotionEffects();
		this.gameMode = player.getGameMode();
	}

	public static void doSnapshot(@NotNull ArenaPlayer arenaPlayer) {
		SNAPSHOTS.put(arenaPlayer.getPlayer().getUniqueId(), new PlayerSnapshot(arenaPlayer));
		clear(arenaPlayer);
	}

	public static void clear(@NotNull ArenaPlayer arenaPlayer) {
		Player player = arenaPlayer.getPlayer();
		IArena arena = arenaPlayer.getArena();
		
		player.setGameMode(GameMode.SURVIVAL);
    	player.setAllowFlight(false);
    	player.setFlying(false);
    	player.setGliding(false);
    	player.setSneaking(false);
    	player.setSprinting(false);
    	player.setFoodLevel(20);
    	player.setSaturation(20F);
    	player.setHealth(EntityUT.getAttribute(player, Attribute.GENERIC_MAX_HEALTH));
    	player.setFireTicks(0);
    	player.leaveVehicle();
    	player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
    	
    	if (ESSENTIALS != null) {
    		ESSENTIALS.disableGod(player);
    	}
    	if (SUNLIGHT != null) {
    		SUNLIGHT.disableGod(player);
    		SUNLIGHT.disableBoard(player);
    	}
    	
    	// When join arena lobby
    	if (arena.getState() != ArenaState.INGAME) {
    		if (!Config.GEN_DISABLE_INVENTORY_MANAGER) {
		    	if (arena.getConfig().getGameplayManager().isKitsEnabled()) {
		    		player.getInventory().clear();
		    	}
    		}
	    	if (!arena.getConfig().getGameplayManager().isPlayerExpSavingEnabled()) {
	    		player.setLevel(0);
	    		player.setExp(0.0f);
	    	}
    	}
    }
	
	public static void restore(@NotNull ArenaPlayer arenaPlayer) {
		Player player = arenaPlayer.getPlayer();
		PlayerSnapshot snapshot = SNAPSHOTS.remove(player.getUniqueId());
		if (snapshot == null) return;

		IArena arena = arenaPlayer.getArena();
		
		Location exit = arena.getConfig().getLocation(ArenaLocationType.LEAVE);
		player.teleport(exit != null ? exit : snapshot.getLocation());
		
		player.setGameMode(snapshot.getGameMode());
		if (player.getGameMode() == GameMode.CREATIVE) {
		   	player.setAllowFlight(true);
		   	player.setFlying(true);
		}
		   
		// Return player potion su.nexmedia.engine.api.effects
		for (PotionEffect pe : player.getActivePotionEffects()) {
		  	player.removePotionEffect(pe.getType());
		}
		player.addPotionEffects(snapshot.getPotionEffects());
		   
		   
		// Return player inventory before the game
		if (!Config.GEN_DISABLE_INVENTORY_MANAGER) {
			if (arena.getConfig().getGameplayManager().isKitsEnabled()) {
		    	player.getInventory().setContents(snapshot.getInventory());
		        player.getInventory().setArmorContents(snapshot.getArmor());
		    }
		}
		      
		// Return player exp he have before the game
		if (!arena.getConfig().getGameplayManager().isPlayerExpSavingEnabled()) {
		   	player.setLevel(snapshot.getLevel());
		  	player.setExp(snapshot.getExp());
		}
	}
	
	@NotNull
	public Location getLocation() {
		return this.location;
	}
	
	@NotNull
	public ItemStack[] getInventory() {
		return this.inventory;
	}
	
	public ItemStack[] getArmor() {
		return this.armor;
	}
	
	public float getExp() {
		return this.exp;
	}
	
	public int getLevel() {
		return this.level;
	}
	
	@NotNull
	public Collection<PotionEffect> getPotionEffects() {
		return this.effects;
	}
	
	@NotNull
	public GameMode getGameMode() {
		return this.gameMode;
	}
}
