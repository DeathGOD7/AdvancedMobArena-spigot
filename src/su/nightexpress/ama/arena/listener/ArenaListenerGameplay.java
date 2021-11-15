package su.nightexpress.ama.arena.listener;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.ArrayUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.AbstractListener;
import su.nexmedia.engine.config.api.ILangMsg;
import su.nexmedia.engine.hooks.Hooks;
import su.nexmedia.engine.hooks.external.MythicMobsHK;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.NumberUT;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.Perms;
import su.nightexpress.ama.api.arena.IArena;
import su.nightexpress.ama.api.arena.event.ArenaMobDeathEvent;
import su.nightexpress.ama.api.arena.event.ArenaPlayerDeathEvent;
import su.nightexpress.ama.api.arena.event.ArenaPlayerReadyEvent;
import su.nightexpress.ama.api.arena.game.ArenaGameEventType;
import su.nightexpress.ama.api.arena.game.event.ArenaGameEventEvent;
import su.nightexpress.ama.api.arena.region.IArenaRegion;
import su.nightexpress.ama.api.arena.type.ArenaLockState;
import su.nightexpress.ama.api.arena.type.ArenaState;
import su.nightexpress.ama.api.arena.type.LeaveReason;
import su.nightexpress.ama.api.arena.type.LobbyItemType;
import su.nightexpress.ama.arena.ArenaKillStreak;
import su.nightexpress.ama.arena.ArenaManager;
import su.nightexpress.ama.arena.ArenaPlayer;
import su.nightexpress.ama.arena.PlayerSnapshot;
import su.nightexpress.ama.config.Config;
import su.nightexpress.ama.stats.StatType;
import su.nightexpress.ama.mobs.ArenaCustomMob;
import su.nightexpress.ama.mobs.ArenaMobHealthBar;

public class ArenaListenerGameplay extends AbstractListener<AMA> {

	private final ArenaManager manager;
	
	public ArenaListenerGameplay(@NotNull ArenaManager manager) {
		super(manager.plugin());
		this.manager = manager;
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onArenaGameEvent(ArenaGameEventEvent e) {
		IArena arena = e.getArena();
		ArenaGameEventType eventType = e.getEventType();

		if (eventType == ArenaGameEventType.GAME_END_LOSE || eventType == ArenaGameEventType.GAME_END_TIME
				|| eventType == ArenaGameEventType.GAME_END_WIN || eventType == ArenaGameEventType.GAME_START
				|| eventType == ArenaGameEventType.PLAYER_JOIN || eventType == ArenaGameEventType.PLAYER_LEAVE) {
			this.manager.updateSigns(arena);
		}

		arena.onArenaGameEvent(e);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onArenaPlayerReadyEvent(ArenaPlayerReadyEvent e) {
		ArenaPlayer arenaPlayer = e.getArenaPlayer();
		Player player = arenaPlayer.getPlayer();
		boolean isReady = e.isReady();

		ILangMsg msg = isReady ? plugin.lang().Arena_Game_Lobby_Ready_True : plugin.lang().Arena_Game_Lobby_Ready_False;
		arenaPlayer.getArena().getPlayers().forEach(arenaPlayer1 -> {
			msg.replace(arenaPlayer.replacePlaceholders()).send(arenaPlayer1.getPlayer());
		});
	}
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onGamePlayerItemDurability(PlayerItemDamageEvent e) {
    	Player player = e.getPlayer();
    	
    	ArenaPlayer arenaPlayer = this.manager.getArenaPlayer(player);
    	if (arenaPlayer == null) return;
    	
    	IArena arena = arenaPlayer.getArena();
    	if (!arena.getConfig().getGameplayManager().isItemDurabilityEnabled()) {
    		e.setCancelled(true);
    	}
    }
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onGamePlayerItemDrop(PlayerDropItemEvent e) {
        Player player = e.getPlayer();
        ArenaPlayer arenaPlayer = this.manager.getArenaPlayer(player);
        if (arenaPlayer == null) return;
    	
    	IArena arena = arenaPlayer.getArena();
    	// Stop drop lobby items
    	if (arena.getConfig().getGameplayManager().isKitsEnabled() && 
    			(arena.getState() != ArenaState.INGAME || arenaPlayer.isLateJoined())) {
    		e.setCancelled(true);
    		return;
    	}
    	
        if (!arena.getConfig().getGameplayManager().isItemDropEnabled()) {
            e.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onGamePlayerItemPickup(EntityPickupItemEvent e) {
    	if (!(e.getEntity() instanceof Player player)) return;

		ArenaPlayer arenaPlayer = this.manager.getArenaPlayer(player);
        if (arenaPlayer == null) return;
    	
    	IArena arena = arenaPlayer.getArena();
        if (!arena.getConfig().getGameplayManager().isItemPickupEnabled()) {
            e.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onGamePlayerRegen(EntityRegainHealthEvent e) {
        if (e.getRegainReason() != RegainReason.SATIATED) return;
        if (!(e.getEntity() instanceof Player player)) return;

		ArenaPlayer arenaPlayer = this.manager.getArenaPlayer(player);
        if (arenaPlayer == null) return;
            
        IArena arena = arenaPlayer.getArena();
        if (!arena.getConfig().getGameplayManager().isRegenerationEnabled()) {
        	e.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onGamePlayerHunger(FoodLevelChangeEvent e) {
        if (!(e.getEntity() instanceof Player player)) return;

		ArenaPlayer arenaPlayer = this.manager.getArenaPlayer(player);
        if (arenaPlayer == null) return;
        
    	IArena arena = arenaPlayer.getArena();
        if (!arena.getConfig().getGameplayManager().isHungerEnabled()) {
            player.setFoodLevel(20);
            e.setCancelled(true);
        }
    }

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onGameMobSlimeSplit(SlimeSplitEvent e) {
		Slime s = e.getEntity();
		IArena arena = this.plugin.getMobManager().getEntityArena(s);
		if (arena == null) return;

		if (!arena.getConfig().getGameplayManager().isSlimeSplitEnabled()) {
			e.setCancelled(true);
		}
	}
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onGamePlayerCmd(PlayerCommandPreprocessEvent e) {
        Player player = e.getPlayer();
        ArenaPlayer arenaPlayer = this.manager.getArenaPlayer(player);
        if (arenaPlayer == null) return;
       
        if (player.hasPermission(Perms.BYPASS_ARENA_COMMAND)) return;
        
        IArena arena = arenaPlayer.getArena();
        if (arena.getConfig().getGameplayManager().isPlayerCommandsEnabled()) return;
        
        String cmd = StringUT.extractCommandName(e.getMessage());
        if (ArrayUtils.contains(plugin.getLabels(), cmd)) return;
        if (arena.getConfig().getGameplayManager().getPlayerCommandsAllowed().contains(cmd)) return;
       	
        e.setCancelled(true);
        player.closeInventory();
        plugin.lang().Arena_Game_Restrict_Commands.send(player);
    }
    
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onGamePlayerRegionMove(PlayerMoveEvent e) {
		Location to = e.getTo();
		if (to == null) return;
		
		Location from = e.getFrom();
		if (to.getX() == from.getX() && to.getZ() == from.getZ()) return;
		
		Player player = e.getPlayer();
		ArenaPlayer arenaPlayer = this.manager.getArenaPlayer(player);
		if (arenaPlayer == null) return;
		
		IArena arena = arenaPlayer.getArena();
		IArenaRegion region = arena.getConfig().getRegionManager().getRegion(to);
		if (region == null) return;
		
		if (region.getState() == ArenaLockState.LOCKED && region.getCuboid().contains(to) && !region.getCuboid().contains(from)) {
			e.setCancelled(true);
		}
	}
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onGamePlayerDeath(EntityDamageEvent e) {
    	if (!(e.getEntity() instanceof Player player)) return;

		ArenaPlayer arenaPlayer = this.manager.getArenaPlayer(player);
        if (arenaPlayer == null) return;
        if (player.getHealth() - e.getFinalDamage() > 0D) return;
        
        e.setDamage(0);
        e.setCancelled(true);
        
        IArena arena = arenaPlayer.getArena();
        PlayerSnapshot.clear(arenaPlayer);

		ArenaPlayerDeathEvent playerDeathEvent = new ArenaPlayerDeathEvent(arena, arenaPlayer);
		plugin.getPluginManager().callEvent(playerDeathEvent);

		if (arena.getConfig().getGameplayManager().isPlayerDropItemsOnDeathEnabled()) {
			for (ItemStack itemStack : player.getInventory().getContents()) {
				if (ItemUT.isAir(itemStack)) continue;
				player.getWorld().dropItemNaturally(player.getLocation(), itemStack);
			}
			player.getInventory().clear();
		}

        if (arenaPlayer.getLives() > 1) {
        	arenaPlayer.setLives(arenaPlayer.getLives() - 1);

        	plugin.lang().Arena_Game_Death_Lives.replace(arenaPlayer.replacePlaceholders()).send(player);
        	
        	IArenaRegion defRegion = arena.getConfig().getRegionManager().getRegionAnyAvailable();
        	if (defRegion != null) {
        		player.teleport(defRegion.getSpawnLocation());
        	}
        	return;
        }
        
        arenaPlayer.addStats(StatType.DEATHS, 1);
        
        plugin.getServer().getScheduler().runTask(plugin, () -> {
        	manager.leaveArena(arenaPlayer, LeaveReason.DEATH);
	        
	        if (!arena.getPlayersIngame().isEmpty()) {
		        if (arena.getConfig().getGameplayManager().isSpectateOnDeathEnabled()) {
		        	manager.joinSpectate(player, arena);
		        }
		        
		        for (ArenaPlayer arenaPlayer1 : arena.getPlayers()) {
		        	plugin.lang().Arena_Game_Death_Player.replace(arenaPlayer.replacePlaceholders()).replace(arena.replacePlaceholders()).send(arenaPlayer1.getPlayer());
		        }
	        }
        });
    }

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onMobDeath(EntityDeathEvent e) {
		LivingEntity entity = e.getEntity();

		IArena arena = this.plugin.getMobManager().getEntityArena(entity);
		if (arena == null) return;

		if (!arena.getConfig().getGameplayManager().isMobDropLootEnabled()) {
			e.getDrops().clear();
		}
		if (!arena.getConfig().getGameplayManager().isMobDropExpEnabled()) {
			e.setDroppedExp(0);
		}

		String mobId = "null";
		ArenaCustomMob boss = this.plugin.getMobManager().getEntityTemplate(entity);
		if (boss != null) {
			mobId = boss.getId();
			ArenaMobHealthBar healthBar = boss.getHealthBar();
			if (healthBar != null) {
				healthBar.remove(arena.getPlayersIngame(), entity);
			}
		}
		else if (this.plugin.getMobManager().isArenaEntity(entity)) {
			if (Hooks.isMythic(entity)) {
				MythicMobsHK mythicMobs = arena.plugin().getMythicMobs();
				if (mythicMobs != null) mobId = mythicMobs.getMythicInstance(entity).getInternalName();
			}
		}
		arena.getMobs().remove(entity);

		Player killer = entity.getKiller();

		// Support for TNT kills.
		if (killer == null) {
			EntityDamageEvent causeLast = entity.getLastDamageCause();
			if (causeLast instanceof EntityDamageByEntityEvent ede && ede.getDamager() instanceof TNTPrimed tnt) {
				if (tnt.getSource() instanceof Player igniter) {
					killer = igniter;
				}
			}
		}

		ArenaMobDeathEvent mobDeathEvent = new ArenaMobDeathEvent(arena, entity, mobId);

		if (killer != null) {
			ArenaPlayer arenaPlayer = plugin.getArenaManager().getArenaPlayer(killer);
			if (arenaPlayer == null) return;

			mobDeathEvent.setKiller(arenaPlayer);

			double mobCoins = this.plugin.getMobManager().getMobCoins(entity);
			int mobScore = this.plugin.getMobManager().getMobScore(entity);

			if (Config.MOBS_KILL_STREAK_DECAY > 0) {
				int streak = arenaPlayer.getKillStreak() + 1;
				ArenaKillStreak killStreak = Config.getKillStreak(streak);
				if (killStreak != null) {
					mobCoins = killStreak.getExtraMoney().applyAsDouble(mobCoins);
					mobScore = (int) killStreak.getExtraScore().applyAsDouble(mobScore);
					killStreak.getMessage().send(killer);
					killStreak.executeCommands(killer);
				}
				arenaPlayer.setKillStreak(streak);
				arenaPlayer.addStats(StatType.BEST_KILL_STREAK, 1);
			}
			arenaPlayer.addStats(StatType.MOB_KILLS, 1);

			if (mobCoins > 0) {
				this.plugin.getEconomy().add(killer, mobCoins);
				this.plugin.lang().Coins_Get.replace("%coins%", NumberUT.format(mobCoins)).send(killer);
			}
			if (mobScore > 0) {
				arenaPlayer.addScore(mobScore);
			}
		}

		plugin.getPluginManager().callEvent(mobDeathEvent);
	}
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onGamePlayerBlockPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();
		Block block = e.getBlock();
        ArenaPlayer arenaPlayer = this.manager.getArenaPlayer(player);
        
    	if (arenaPlayer != null) {
    		e.setCancelled(true);
        	if (block.getType() == Material.TNT) {
        		ItemStack item = e.getItemInHand();
        		item.setAmount(item.getAmount() - 1);
        		TNTPrimed tnt = block.getWorld().spawn(block.getLocation(), TNTPrimed.class);
        		plugin.getPMS().setTNTSource(tnt, player);
        		arenaPlayer.addStats(StatType.TNT_EXPLODED, 1);
        	}
        	return;
    	}

    	if (player.hasPermission(Perms.ADMIN)) return;
    	IArena arena = this.manager.getArenaAtLocation(block.getLocation());
    	if (arena != null) {
    		e.setCancelled(true);
    	}
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onGameStatsConsume(PlayerItemConsumeEvent e) {
    	Player player = e.getPlayer();
    	ArenaPlayer arenaPlayer = this.manager.getArenaPlayer(player);
        if (arenaPlayer == null) return;
        
        ItemStack item = e.getItem();
        ItemMeta meta = item.getItemMeta();
        
        if (meta instanceof PotionMeta) {
        	arenaPlayer.addStats(StatType.POTIONS_DRUNK, 1);
        }
        else {
        	arenaPlayer.addStats(StatType.FOOD_EATEN, 1);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onGameStatsItemBreak(PlayerItemBreakEvent e) {
    	Player player = e.getPlayer();
    	ArenaPlayer arenaPlayer = this.manager.getArenaPlayer(player);
        if (arenaPlayer == null) return;
        
        arenaPlayer.addStats(StatType.EQUIPMENT_BROKEN, 1);
    }
    
    @EventHandler
    public void onGamePlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        ArenaPlayer arenaPlayer = this.manager.getArenaPlayer(player);
        if (arenaPlayer == null) return;
        
        this.manager.leaveArena(arenaPlayer, LeaveReason.SELF);
    }
    
	@EventHandler(priority = EventPriority.HIGHEST)
    public void onGameItemInteract(PlayerInteractEvent e) {
		Player player = e.getPlayer();

		ArenaPlayer arenaPlayer = this.manager.getArenaPlayer(player);
        if (arenaPlayer == null) return;
    	
		ItemStack item = e.getItem();
    	if (item == null || ItemUT.isAir(item)) return;
    	
    	IArena arena = arenaPlayer.getArena();
    	if (arena.getState() != ArenaState.INGAME || arenaPlayer.isLateJoined()) {
    		// Prevent using lobby items on arena signs
    		// and interactable blocks.
    		Block block = e.getClickedBlock();
    		if (block != null && block.getType().isInteractable()) return;

    		LobbyItemType lobbyItemType = LobbyItemType.getType(item);
    		if (lobbyItemType != null) {
    			lobbyItemType.use(arenaPlayer);
				e.setUseItemInHand(Result.DENY);
				return;
			}
    	}
    	
    	if (arena.getConfig().getGameplayManager().getBannedItems().contains(item.getType())) {
			e.setUseItemInHand(Result.DENY);
    	}
	}
}
