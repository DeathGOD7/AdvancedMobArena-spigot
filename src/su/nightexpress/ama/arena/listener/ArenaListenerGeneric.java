package su.nightexpress.ama.arena.listener;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.AbstractListener;
import su.nexmedia.engine.hooks.Hooks;
import su.nexmedia.engine.utils.DataUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.Perms;
import su.nightexpress.ama.api.arena.IArena;
import su.nightexpress.ama.api.arena.type.ArenaState;
import su.nightexpress.ama.api.kits.IArenaKit;
import su.nightexpress.ama.arena.ArenaManager;
import su.nightexpress.ama.arena.ArenaPlayer;
import su.nightexpress.ama.config.Config;
import su.nightexpress.ama.mobs.ArenaCustomMob;
import su.nightexpress.ama.mobs.ArenaMobHealthBar;
import su.nightexpress.ama.mobs.MobManager;

import java.util.List;

public class ArenaListenerGeneric extends AbstractListener<AMA> {

	private final ArenaManager manager;
	
	public ArenaListenerGeneric(@NotNull ArenaManager manager) {
		super(manager.plugin());
		this.manager = manager;
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onArenaSignJoinUse(PlayerInteractEvent e) {
		if (e.getHand() == EquipmentSlot.OFF_HAND) return;
		if (e.useInteractedBlock() == Event.Result.DENY) return;
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

		Block block = e.getClickedBlock();
		if (block == null) return;

		BlockState state = block.getState();
		if (!(state instanceof Sign sign)) return;

		Player player = e.getPlayer();

		if (DataUT.getBooleanData(sign, ArenaManager.KEY_SIGN_READY)) {
			ArenaPlayer arenaPlayer = this.manager.getArenaPlayer(player);
			if (arenaPlayer == null) return;

			arenaPlayer.setReady(!arenaPlayer.isReady());
			e.setUseInteractedBlock(Event.Result.DENY);
			e.setUseItemInHand(Event.Result.DENY);
			return;
		}

		String arenaId = DataUT.getStringData(sign, ArenaManager.KEY_SIGN_JOIN);
		if (arenaId == null) return;

		IArena arena = this.manager.getArenaById(arenaId);
		if (arena == null) return;

		e.setUseInteractedBlock(Event.Result.DENY);
		e.setUseItemInHand(Event.Result.DENY);

		if (!this.manager.joinLobby(player, arena)) {
			this.manager.joinSpectate(player, arena);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onArenaSignJoinCreate(SignChangeEvent e) {
		if (!e.getPlayer().hasPermission(Perms.ADMIN)) return;

		Block block = e.getBlock();
		BlockState state = block.getState();
		if (!(state instanceof Sign sign)) return;

		String line1 = e.getLine(0);
		if (line1 == null || !line1.equalsIgnoreCase("AMA")) return;

		String line2 = e.getLine(1);
		if (line2 == null) return;

		if (line2.equalsIgnoreCase("READY")) {
			this.plugin.getServer().getScheduler().runTask(plugin, () -> {
				List<String> text = Config.SIGNS_READY_FORMAT;
				for (int line = 0; line < 4; line++) {
					sign.setLine(line, line >= text.size() ? "" : text.get(line));
				}
				DataUT.setData(sign, ArenaManager.KEY_SIGN_READY, true);
				sign.update(true);
			});
		}
		else if (!line2.equalsIgnoreCase("JOIN")) {
			String line3 = e.getLine(2);
			if (line3 == null) return;

			IArena arena = this.manager.getArenaById(line3);
			if (arena == null) return;

			this.manager.addJoinSign(arena, sign);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onArenaPlayerBlockBreak(BlockBreakEvent e) {
		Player player = e.getPlayer();
		if (this.manager.isPlaying(player)) {
			e.setCancelled(true);
			return;
		}

		Block block = e.getBlock();
		IArena arena = this.manager.getArenaAtLocation(block.getLocation());
		if (arena != null && !player.hasPermission(Perms.ADMIN)) {
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onArenaDamageGeneric(EntityDamageEvent e) {
		Entity entity = e.getEntity();

		ArenaCustomMob mob = this.plugin.getMobManager().getEntityTemplate(entity);
		if (mob != null) {
			ArenaMobHealthBar healthBar = mob.getHealthBar();
			if (healthBar == null) return;

			IArena arena = this.plugin.getMobManager().getEntityArena(entity);
			if (arena == null) return;

			plugin.getServer().getScheduler().runTask(plugin, () -> {
				healthBar.update(arena.getPlayersIngame(), (LivingEntity) entity);
			});
			return;
		}

		if (!(entity instanceof Player player)) return;

		ArenaPlayer arenaPlayer = this.manager.getArenaPlayer(player);
		if (arenaPlayer == null) return;

		// Avoid damage in lobby
		if (arenaPlayer.getArena().getState() != ArenaState.INGAME || arenaPlayer.isLateJoined()) {
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onArenaDamageFriendly(EntityDamageByEntityEvent e) {
		Entity eVictim = e.getEntity();
		Entity eDamager = e.getDamager();

		if (!(eVictim instanceof  LivingEntity victim)) return;
		if (eDamager instanceof Projectile projectile) {
			if (projectile.getShooter() instanceof LivingEntity livingEntity) {
				eDamager = livingEntity;
			}
		}

		if (plugin.getMobManager().isArenaEntity(eDamager) && plugin.getMobManager().isArenaEntity(victim)) {
			e.setCancelled(true);
			return;
		}

		if (eDamager instanceof Player pDamager && victim instanceof Player pVictim) {
			if (this.manager.isPlaying(pDamager) || this.manager.isPlaying(pVictim)) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onArenaItemSpawn(ItemSpawnEvent e) {
		Item item = e.getEntity();
		IArena arena = this.manager.getArenaAtLocation(item.getLocation());
		if (arena == null) return;

		arena.addGroundItem(item);
		item.setMetadata(MobManager.META_ARENA_ITEM, new FixedMetadataValue(plugin, arena.getId()));
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onArenaMobSpawn(CreatureSpawnEvent e) {
		LivingEntity entity = e.getEntity();
		if (entity instanceof Player && !Hooks.isNPC(entity)) return;

		Location location = entity.getLocation();
		IArena arena = plugin.getArenaManager().getArenaAtLocation(location);
		if (arena == null) return;

		// Allows to spawn slimes if slime split is enabled on arena.
		CreatureSpawnEvent.SpawnReason reason = e.getSpawnReason();
		if (reason == CreatureSpawnEvent.SpawnReason.SLIME_SPLIT || reason == CreatureSpawnEvent.SpawnReason.CUSTOM
				|| arena.getConfig().getGameplayManager().getAllowedSpawnReasons().contains(reason)) {
			this.plugin.getMobManager().setArena(entity, arena); // Add Arena tag to entity
			arena.getMobs().add(entity);
			return;
		}

		e.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onArenaMobTarget(EntityTargetEvent e) {
		if (!(e.getEntity() instanceof LivingEntity agressor)) return;
		if (!(e.getTarget() instanceof LivingEntity target)) return;

		if (!this.plugin.getMobManager().isArenaEntity(agressor)) return;

		if (this.plugin.getMobManager().isArenaEntity(target)) {
			e.setCancelled(true);
			return;
		}
		if (target instanceof Player player) {
			if (!plugin.getArenaManager().isPlaying(player)) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onArenaMobTeleport(EntityTeleportEvent e) {
		Entity entity = e.getEntity();
		if (!this.plugin.getMobManager().isArenaEntity(entity)) return;

		Location to = e.getTo();
		if (to != null && this.manager.getArenaAtLocation(e.getTo()) == null) {
			e.setCancelled(true);
		}
	}

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onArenaBlockFire(BlockIgniteEvent e) {
    	if (e.getCause() == BlockIgniteEvent.IgniteCause.FLINT_AND_STEEL) return;
    	
    	Block block = e.getBlock();
    	IArena arena = this.manager.getArenaAtLocation(block.getLocation());
    	if (arena != null) {
    		e.setCancelled(true);
    	}
    }
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onArenaBlockExplode(BlockExplodeEvent e) {
		if (e.blockList().stream().anyMatch(block -> manager.getArenaAtLocation(block.getLocation()) != null)) {
			e.blockList().clear();
		}
    }
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onArenaMobExplode(EntityExplodeEvent e) {
		if (e.blockList().stream().anyMatch(block -> manager.getArenaAtLocation(block.getLocation()) != null)) {
			e.blockList().clear();
		}
    }

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onArenaBlockChange(EntityChangeBlockEvent e) {
		IArena arena = plugin.getArenaManager().getArenaAtLocation(e.getBlock().getLocation());
		if (arena != null) {
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onArenaBlockForm(EntityBlockFormEvent e) {
		IArena arena = plugin.getArenaManager().getArenaAtLocation(e.getBlock().getLocation());
		if (arena != null) {
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onArenaMobCombust(EntityCombustEvent e) {
		Entity entity = e.getEntity();
		if (this.plugin.getMobManager().isArenaEntity(entity)) {
			if (e instanceof EntityCombustByEntityEvent ec){
				if (this.plugin.getMobManager().isArenaEntity(ec.getCombuster())) {
					e.setCancelled(true);
				}
				return;
			}
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onArenaBreakVehicle(VehicleDamageEvent e) {
		Entity damager = e.getAttacker();
		if (damager instanceof Player player && plugin.getArenaManager().isPlaying(player)) {
			e.setCancelled(true);
			return;
		}
		if (damager != null && damager.hasPermission(Perms.ADMIN)) return;

		Vehicle vehicle = e.getVehicle();
		IArena arena = plugin.getArenaManager().getArenaAtLocation(vehicle.getLocation());
		if (arena == null) return;

		e.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onArenaBreakDecorations(EntityDamageByEntityEvent e) {
		Entity stand = e.getEntity();
		if (this.plugin.getMobManager().isArenaEntity(stand) || stand instanceof Player) return;

		Entity damager = e.getDamager();
		if (damager instanceof Player player && plugin.getArenaManager().isPlaying(player)) {
			e.setCancelled(true);
			return;
		}
		if (damager.hasPermission(Perms.ADMIN)) return;

		IArena arena = plugin.getArenaManager().getArenaAtLocation(stand.getLocation());
		if (arena == null) return;

		e.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onArenaBreakPainting(HangingBreakByEntityEvent e) {
		Entity damager = e.getRemover();
		if (damager instanceof Player player && plugin.getArenaManager().isPlaying(player)) {
			e.setCancelled(true);
			return;
		}
		if (damager != null && damager.hasPermission(Perms.ADMIN)) return;

		IArena arena = plugin.getArenaManager().getArenaAtLocation(e.getEntity().getLocation());
		if (arena == null) return;

		e.setCancelled(true);
	}

	/**
	 * Prevent interact with non-mob entities in arena
	 */
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onArenaPlayerInteractEntity(PlayerInteractEntityEvent e) {
		Player player = e.getPlayer();
		if (!this.manager.isPlaying(player)) return;

		Entity entity = e.getRightClicked();
		if (entity instanceof Player || plugin.getMobManager().isArenaEntity(entity)) return;

		e.setCancelled(true);
	}

	/**
	 * Prevent interact with non-mob entities in arena
	 */
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onArenaPlayerInteractEntity2(PlayerInteractAtEntityEvent e) {
		Player player = e.getPlayer();
		if (!this.manager.isPlaying(player)) return;

		Entity entity = e.getRightClicked();
		if (entity instanceof Player || plugin.getMobManager().isArenaEntity(entity)) return;

		e.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onArenaChunkUnload(ChunkUnloadEvent e) {
		Chunk chunk = e.getChunk();
		if (chunk.getPluginChunkTickets().contains(this.plugin)) return;

		for (Entity entity : e.getChunk().getEntities()) {
			if (plugin.getMobManager().isArenaEntity(entity)) {
				chunk.addPluginChunkTicket(this.plugin);
				return;
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onArenaChat(AsyncPlayerChatEvent e) {
		if (!Config.CHAT_ENABLED) return;
		if (Config.CHAT_IGNORE_GLOBAL && e.getMessage().startsWith("!")) return;
		
		Player player = e.getPlayer();
		ArenaPlayer arenaPlayer = this.manager.getArenaPlayer(player);
		if (arenaPlayer == null) return;
		
		IArena arena = arenaPlayer.getArena();
		IArenaKit kit = arenaPlayer.getKit();

		e.getRecipients().retainAll(arena.getPlayers().stream().map(ArenaPlayer::getPlayer).toList());
		
		String format = Config.CHAT_FORMAT
				.replace("%arena%", arena.getConfig().getName())
				.replace("%player%", "%1$s")
				.replace("%message%", "%2$s")
				.replace("%kit%", kit != null ? kit.getName() : "")
				;
		
		if (Hooks.hasPlugin(Hooks.PLACEHOLDER_API)) {
			format = PlaceholderAPI.setPlaceholders(player, format);
		}
		e.setFormat(format);
	}
}
