package su.nightexpress.ama.arena;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.config.api.ILangMsg;
import su.nexmedia.engine.utils.EffectUT;
import su.nexmedia.engine.utils.EntityUT;
import su.nexmedia.engine.utils.MsgUT;
import su.nexmedia.engine.utils.NumberUT;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.ama.Perms;
import su.nightexpress.ama.api.arena.AbstractArena;
import su.nightexpress.ama.api.arena.config.IArenaConfig;
import su.nightexpress.ama.api.arena.event.ArenaMobDeathEvent;
import su.nightexpress.ama.api.arena.event.ArenaPlayerSpectateEvent;
import su.nightexpress.ama.api.arena.event.ArenaWaveCompleteEvent;
import su.nightexpress.ama.api.arena.event.ArenaWaveStartEvent;
import su.nightexpress.ama.api.arena.game.ArenaGameEventType;
import su.nightexpress.ama.api.arena.game.event.ArenaGameEventEvent;
import su.nightexpress.ama.api.arena.game.event.ArenaGameStartEvent;
import su.nightexpress.ama.api.arena.region.IArenaRegion;
import su.nightexpress.ama.api.arena.region.IArenaRegionManager;
import su.nightexpress.ama.api.arena.region.IArenaRegionWave;
import su.nightexpress.ama.api.arena.region.event.ArenaRegionEvent;
import su.nightexpress.ama.api.arena.type.*;
import su.nightexpress.ama.api.arena.wave.IArenaWaveManager;
import su.nightexpress.ama.api.arena.wave.IArenaWaveMob;
import su.nightexpress.ama.api.arena.wave.IArenaWaveUpcoming;
import su.nightexpress.ama.api.kits.IArenaKit;
import su.nightexpress.ama.arena.config.ArenaConfig;
import su.nightexpress.ama.config.Config;
import su.nightexpress.ama.data.ArenaUser;
import su.nightexpress.ama.kits.KitManager;
import su.nightexpress.ama.stats.StatType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;

public class Arena extends AbstractArena {
    
    // TODO Final Arena Stats
    // Most Damager, Killer, etc? like KF

	public Arena(@NotNull ArenaConfig config)  {
		super(config);
    }

	@Override
	public void onArenaStop(@NotNull EndType type) {
		if (this.getState() == ArenaState.INGAME && this.getConfig().getGameplayManager().isAnnouncesEnabled()) {
			this.plugin.lang().Arena_Game_Announce_End.replace(this.replacePlaceholders()).broadcast();
		}
		new HashSet<>(this.getPlayers()).forEach(arenaPlayer -> plugin.getArenaManager().leaveArena(arenaPlayer, type.getReason()));
		this.getPlayers().clear();
		this.reset();

	}

	public boolean canJoin(@NotNull Player player, boolean isMessage) {
		// Check if arena is enabled.
		IArenaConfig config = this.getConfig();
		if (!config.isActive() || config.hasProblems()) {
			if (isMessage) plugin().lang().Arena_Error_Disabled.replace(this.replacePlaceholders()).send(player);
			return false;
		}

		// Check if arena is in-game.
		if (this.getState() == ArenaState.INGAME && !player.hasPermission(Perms.BYPASS_ARENA_JOIN_INGAME)) {
			if (isMessage) plugin().lang().Arena_Join_Error_Started.replace(this.replacePlaceholders()).send(player);
			return false;
		}

		// Check for max. players.
		int playerMax = config.getGameplayManager().getPlayerMaxAmount();
		if (playerMax > 0 && this.getPlayers().size() >= playerMax) {
			if (isMessage) plugin().lang().Arena_Join_Error_Maximum.send(player);
			return false;
		}

		// Check for permission.
		if (this.getConfig().isPermissionRequired() && !this.hasPermission(player)) {
			plugin.lang().Arena_Join_Error_Permission.send(player);
			return false;
		}

		// Check for money.
		double cost = this.getConfig().getJoinMoneyRequired();
		if (cost > 0) {
			if (plugin.getEconomy().getBalance(player) < cost) {
				plugin.lang().Arena_Join_Error_Money.replace(this.replacePlaceholders()).send(player);
				return false;
			}
			plugin.getEconomy().take(player, cost);
		}

		return true;
	}

	public boolean joinLobby(@NotNull ArenaPlayer arenaPlayer) {
		if (!arenaPlayer.getArena().equals(this)) {
			this.plugin().warn("Attempt to add arena player to another arena!");
			return false;
		}
		if (!this.getPlayers().add(arenaPlayer)) {
			return false;
		}

		Player player = arenaPlayer.getPlayer();

		player.teleport(this.getConfig().getLocation(ArenaLocationType.LOBBY));
		EffectUT.playEffect(player.getLocation(), Particle.CLOUD.name(), 0.1f, 0.25f, 0.1f, 0.15f, 30);

		// Adding lobby items
		if (this.getConfig().getGameplayManager().isKitsEnabled()) {
			if (plugin().getKitManager().isSavePurchasedKits()) {
				LobbyItemType.KIT_SELECT.giveItem(player);
			}
			LobbyItemType.KIT_SHOP.giveItem(player);
			LobbyItemType.EXIT.giveItem(player);
			LobbyItemType.STATS.giveItem(player);
			if (arenaPlayer.isLateJoined()) {
				LobbyItemType.READY.giveItem(player);
			}
		}

		// Send messages
		plugin().lang().Arena_Game_Lobby_Enter.replace(this.replacePlaceholders()).send(player);

		this.getPlayers().stream().filter(ap -> !ap.getPlayer().equals(player)).forEach(lobbyPlayer -> {
			plugin().lang().Arena_Game_Lobby_Joined.replace(arenaPlayer.replacePlaceholders()).send(lobbyPlayer.getPlayer());
		});

		// Prepare to start
		if (this.getState() == ArenaState.INGAME) return true;

		int minPlayers = this.getConfig().getGameplayManager().getPlayerMinAmount();
		if (this.getPlayers().size() < minPlayers) {
			this.getPlayers().forEach(lobbyPlayer -> {
				plugin().lang().Arena_Game_Lobby_MinPlayers.replace("%min%", minPlayers).send(lobbyPlayer.getPlayer());
			});
		}
		return true;
	}

	@Override
	public void joinGame(@NotNull ArenaPlayer arenaPlayer) {
		if (!arenaPlayer.getArena().equals(this)) {
			this.plugin().warn("Attempt to add arena player to another arena!");
			return;
		}

		// Check if player's kit is valid and kick from the arena if it's not.
		// Do not kick players who joined after the game start, so they can select their kit
		// as long as they want.
		if (!this.validateJoinKit(arenaPlayer, !arenaPlayer.isLateJoined())) return;

		IArenaRegionManager reg = this.getConfig().getRegionManager();
		IArenaRegion regionDefault = arenaPlayer.isLateJoined() ? reg.getRegionAnyAvailable() : reg.getRegionDefault();

		// Check for valid arena's region.
		if (regionDefault == null) {
			return;
		}

		Player player = arenaPlayer.getPlayer();
		player.teleport(regionDefault.getSpawnLocation());

		// Restore player's health before the game.
		player.setHealth(EntityUT.getAttribute(player, Attribute.GENERIC_MAX_HEALTH));

		// Add arena scoreboard.
		arenaPlayer.addBoard();
		arenaPlayer.setLateJoined(false);

		plugin().lang().Arena_Game_Notify_Start.send(player);
		MsgUT.sound(player, Config.SOUND_GAME_START);
	}

	@Override
	public boolean joinSpectate(@NotNull Player player) {
		// Check if arena is active and setup.
		if (!this.getConfig().isActive() || this.getConfig().hasProblems()) {
			plugin().lang().Arena_Error_Disabled.replace(this.replacePlaceholders()).send(player);
			return false;
		}

		// Check for player permissions.
		if (!this.hasPermission(player)) {
			plugin().lang().Arena_Join_Error_Permission.send(player);
			return false;
		}

		// Check if spectating is enabled.
		if (!this.getConfig().getGameplayManager().isSpectateEnabled()) {
			plugin().lang().Arena_Join_Spectate_Error_Disabled.send(player);
			return false;
		}

		ArenaPlayerSpectateEvent spectateEvent = new ArenaPlayerSpectateEvent(this, player);
		plugin().getPluginManager().callEvent(spectateEvent);
		if (spectateEvent.isCancelled()) return false;

		player.teleport(this.getConfig().getLocation(ArenaLocationType.SPECTATE));
		plugin().lang().Arena_Join_Spectate_Success.replace(this.replacePlaceholders()).send(player);

		return true;
	}

	@Deprecated
	private boolean validateJoinKit(@NotNull ArenaPlayer arenaPlayer, boolean kick) {
		if (this.getConfig().getGameplayManager().isKitsEnabled()) {
			IArenaKit kit = arenaPlayer.getKit();
			if (kit == null) {
				KitManager kitManager = plugin.getKitManager();

				// If kits are saved to account, then try to select random
				// obtained kit.
				if (kitManager.isSavePurchasedKits()) {
					ArenaUser user = plugin.getUserManager().getOrLoadUser(arenaPlayer.getPlayer());
					String userKit = user == null ? null : Rnd.get(new ArrayList<>(user.getKits()));
					kit = userKit == null ? null : kitManager.getKitById(userKit);
				}

				// If kits are not saved to account or user don't obtain any kit
				// then try to give the default kit by kit settings.
				if (kit == null || !kit.isAvailable(arenaPlayer, false)) {
					kit = plugin.getKitManager().getDefaultKit();
				}

				// If even default kit was fail, then it's unlucky game for this user,
				// he will be kicked from the arena.
				if (kit == null) {
					if (kick) plugin.getArenaManager().leaveArena(arenaPlayer, LeaveReason.NO_KIT);
					return false;
				}
			}
			arenaPlayer.setKit(kit);
			kit.give(arenaPlayer);
		}
		return true;
	}


	@Override
	public void tickLobby() {
		if (this.getState() == ArenaState.INGAME) return;

		if (this.getState() == ArenaState.WAITING) {
			if (this.getPlayers().size() >= this.getConfig().getGameplayManager().getPlayerMinAmount()) {
				this.setState(ArenaState.READY);
				this.updateGameEventListeners();

				if (this.getConfig().getGameplayManager().isAnnouncesEnabled()) {
					plugin().lang().Arena_Game_Announce_Start
							.replace(this.replacePlaceholders())
							.replace("%time%", this.getConfig().getGameplayManager().getLobbyTime())
							.broadcast();
				}
			}
		}

		if (this.getState() != ArenaState.READY) return;

		if (this.getPlayers().size() < this.getConfig().getGameplayManager().getPlayerMinAmount()) {
			this.setState(ArenaState.WAITING);
			this.setLobbyTimeleft(this.getConfig().getGameplayManager().getLobbyTime());
			this.plugin().getArenaManager().updateSigns(this);
			return;
		}

		int lobbyTimeleft = this.getLobbyTimeleft();
		boolean allReady = this.getPlayers().stream().allMatch(ArenaPlayer::isReady);
		if (allReady) {
			if (lobbyTimeleft > Config.LOBBY_READY_DROP_TIMER) {
				lobbyTimeleft = Config.LOBBY_READY_DROP_TIMER;
			}
		}
		else {
			if (Config.LOBBY_READY_FREEZE_TIMER_WHEN_DROPPED && lobbyTimeleft > 0
					&& lobbyTimeleft <= Config.LOBBY_READY_DROP_TIMER) {
				return;
			}
		}

		if (lobbyTimeleft <= 0) {
			this.getPlayers().forEach(this::joinGame);
			this.setState(ArenaState.INGAME);

			ArenaGameStartEvent event = new ArenaGameStartEvent(this);
			plugin().getPluginManager().callEvent(event);
			return;
		}

		if (lobbyTimeleft % 15 == 0 || lobbyTimeleft % 10 == 0 || lobbyTimeleft <= 10) {
			for (ArenaPlayer arenaPlayer : this.getPlayers()) {
				plugin().lang().Arena_Game_Lobby_Timer.replace("%time%", lobbyTimeleft).send(arenaPlayer.getPlayer());
				MsgUT.sound(arenaPlayer.getPlayer(), Config.SOUND_LOBBY_TICK);
			}
		}
		this.setLobbyTimeleft(lobbyTimeleft-1);
	}

	@Override
	public void tickGame() {
		if (this.getState() != ArenaState.INGAME) return;

		// Time is ended, Game Over.
		if (this.getGameTimeleft() > 0) {
			this.setGameTimeleft(Math.max(0L, this.getGameTimeleft() - 1000L));
			if (this.getGameTimeleft() <= 0L) {
				this.stop(EndType.TIMELEFT);
				return;
			}
		}

		this.tickPlayers();
		this.tickMobs();
		//this.tickBoss();

		// No players left, stop the game.
		if (this.getPlayersIngame().isEmpty()) {
			this.stop(EndType.FORCE);
			return;
		}

		this.plugin.runTask(c -> this.showWaveStatus(), true);

		if (this.getWaveNextTimeleft() == 0) {
			this.newWave();

			// Stop game if no regions are available.
			//if (this.getState() == ArenaState.INGAME && gameEvent.getEventType() == ArenaGameEventType.REGION_LOCKED) {
				IArenaRegion playRegion = this.getConfig().getRegionManager().getRegionAnyAvailable();
				if (playRegion == null) {
					this.stop(EndType.NO_REGION);
					return;
				}

				// Move mobs from locked regions to the unlocked and active.
				List<LivingEntity> allMobs = new ArrayList<>();
				allMobs.addAll(this.getMobs());
				//allMobs.addAll(this.getBosses());
				allMobs.forEach(mob -> {
					IArenaRegion region = this.getConfig().getRegionManager().getRegion(mob.getLocation());
					if (region != null && region.getState() == ArenaLockState.LOCKED) {
						mob.teleport(playRegion.getSpawnLocation());
					}
				});
			//}
			return;
		}

		if (this.isNextWaveAllowed()) {
			if (this.getWaveNextTimeleft() == this.getConfig().getWaveManager().getDelayDefault()) {

				ArenaWaveCompleteEvent event = new ArenaWaveCompleteEvent(this);
				plugin().getPluginManager().callEvent(event);

				if (this.isLatestWave()) {
					this.getPlayers().stream().map(ArenaPlayer::getPlayer).forEach(player -> {
						plugin().lang().Arena_Game_Wave_Latest.send(player);
					});
				}
			}
			this.setWaveNextTimeleft(this.getWaveNextTimeleft() - 1);
		}
	}

	protected void tickPlayers() {
		for (ArenaPlayer arenaPlayer : this.getPlayersIngame()) {
			arenaPlayer.tick();

			// Notify if player region is inactive anymore.
			if (this.isNextWaveAllowed()) {
				IArenaRegion region = arenaPlayer.getRegion(false);
				if (region == null || region.getState() == ArenaLockState.LOCKED) {
					plugin.lang().Arena_Region_Locked_Notify.send(arenaPlayer.getPlayer());
				}
			}
		}
	}

	@Override
	public void onArenaGameEvent(@NotNull ArenaGameEventEvent gameEvent) {
		super.onArenaGameEvent(gameEvent);

		if (gameEvent.getEventType() == ArenaGameEventType.REGION_UNLOCKED) {
			ArenaRegionEvent regionEvent = (ArenaRegionEvent) gameEvent;
			this.getPlayersIngame().forEach(arenaPlayer -> {
				plugin.lang().Arena_Region_Unlocked_Notify.replace(regionEvent.getArenaRegion().replacePlaceholders()).send(arenaPlayer.getPlayer());
			});
			return;
		}

		if (gameEvent.getEventType() == ArenaGameEventType.MOB_KILLED) {

			if (gameEvent instanceof ArenaMobDeathEvent mobDeathEvent) {
				this.getMobs().remove(mobDeathEvent.getEntity());
				//this.removeMob(mobDeathEvent.getEntity());
				//this.removeBoss(mobDeathEvent.getEntity());
			}

			this.countGradual();
		}
	}

	private void countGradual() {
		if (this.getConfig().getWaveManager().isGradualSpawnEnabled()) {
			this.setGradualMobsKilled(this.getGradualMobsKilled() + 1);

			boolean allSpawned = this.getUpcomingWaves().stream().allMatch(IArenaWaveUpcoming::isAllMobsSpawned);
			if (allSpawned) return;

			double lastSpawned = this.getMobsTotalAmount();
			double killedNextPc = this.getConfig().getWaveManager().getGradualSpawnNextKillPercent();
			int killedRaw = this.getGradualMobsKilled();
			int killedNeedRaw = (int) Math.max(1D, lastSpawned * killedNextPc / 100D);

			if (Config.DEBUG_MOB_SPAWN) {
				System.out.println("Game Event: Mob Killed | Gradual Spawning");
				System.out.println("[Gradual] Total Mobs (All Waves): " + lastSpawned);
				System.out.println("[Gradual] Killed (Raw): " + killedRaw);
				System.out.println("[Gradual] Need Killed (Raw): " + killedNeedRaw);
			}
			boolean isEmpty = this.getMobs().isEmpty() /*&& this.getBosses().isEmpty()*/ && this.gradualMobsPrepare == 0;

			if (killedRaw >= killedNeedRaw || isEmpty) {
				this.gradualMobsPrepare++;
				this.setGradualMobsKilled(0);
				if (Config.DEBUG_MOB_SPAWN) System.out.println("[Gradual] Prepared Groups: " + gradualMobsPrepare);
			}
		}
	}

	protected void tickMobs() {
		if (this.getConfig().getWaveManager().isGradualSpawnEnabled()) {
			if (this.gradualMobsTimer++ % this.getConfig().getWaveManager().getGradualSpawnNextInterval() == 0) {
				this.gradualMobsTimer = 0;

				if (this.gradualMobsPrepare > 0) {
					double nextPc = this.getConfig().getWaveManager().getGradualSpawnNextPercent();
					this.spawnMobs(nextPc);
					this.gradualMobsPrepare--;
				}
			}
		}

		this.getMobs().removeIf(mob -> {
			if (!mob.isValid() || mob.isDead()) {
				this.countGradual();
				return true;
			}
			return false;
		});
		this.getMobs().forEach(mob -> this.updateMobTarget(mob, false));

		// TODO Mob abilities
	}

	@Override
	public void spawnMobs(double spawnPercent) {
		if (spawnPercent == 0D) spawnPercent = 100D;
		spawnPercent /= 100D;

		if (Config.DEBUG_MOB_SPAWN) System.out.println("[Spawn Processor] 0. Percent of Total Mobs: " + spawnPercent);

		// Готовим список волн для спавна мобов.
		List<IArenaWaveUpcoming> upcomings = new ArrayList<>(this.getUpcomingWaves());

		// Счетчик количества мобов для спавна для каждой волны региона.
		int[] mobsSpawnPerWave = new int[upcomings.size()];

		// Спавним как минимум одного моба всегда.
		int mobsPlannedTotal = (int) Math.max(1D, (double) this.getMobsTotalAmount() * spawnPercent);

		// Спавним "поровну" мобов от каждой волны.
		// Например: При 30% спавна от 100 мобов (х30) и 3-х волнах с кол-вом мобов [5,15,30] (x50) = [3,10,17]
		// 100% of 100 with wave mobs [30,30,40] = x100, 30/30/40 * 1.0 = 30/30/40 = 100 (100%).
		// 70% of 10 with wave mobs [2,2,6] = x10, 2/2/6 * 0.7 = ~1/~1/~5 = 7 (70%).
		for (int counter = 0; counter < mobsSpawnPerWave.length; counter++) {
			IArenaWaveUpcoming wave = upcomings.get(counter);

			double mobsWaveTotal = wave.getPreparedMobs().stream().mapToInt(IArenaWaveMob::getAmount).sum();
			//double mobsWaveSpawned = wave.getMobsSpawnedAmount().values().stream().mapToInt(i -> i).sum();
			if (Config.DEBUG_MOB_SPAWN) System.out.println("[Spawn Processor] 2. Total Mobs for Wave '" + wave.getRegionWave().getId() + "': " + mobsWaveTotal);
			//System.out.println(wave.getRegionWave().getId() + " mobs have spawned: " + mobsWaveSpawned);

			//mobsWaveTotal -= mobsWaveSpawned;

			mobsSpawnPerWave[counter] = (int) Math.ceil(mobsWaveTotal * spawnPercent);
			if (Arrays.stream(mobsSpawnPerWave).sum() >= mobsPlannedTotal) break;
		}

		if (Config.DEBUG_MOB_SPAWN) System.out.println("[Spawn Processor] 3. Mobs Per Each Wave:" + Arrays.toString(mobsSpawnPerWave));

		// Подгоняем количество мобов для спавна на случай небольших расхождений при счете с процентами.
		int mobsSpawnTotal = Arrays.stream(mobsSpawnPerWave).sum();
		if (mobsSpawnTotal != mobsPlannedTotal) {
			int dif = mobsPlannedTotal - mobsSpawnTotal;
			int[] fineParts = NumberUT.splitIntoParts(dif, mobsSpawnPerWave.length);

			for (int counter = 0; counter < mobsSpawnPerWave.length; counter++) {
				mobsSpawnPerWave[counter] += fineParts[counter];
			}
		}

		// Подгоняем количество мобов дубль-два xD
		// На случай если кол-ов мобов в массиве больше, чем кол-во мобов в соответствующей ему волне.
		// В таком случае разница прибавляется к другим волнам, где кол-во наоборот больше, чем в массиве.
		int finer = 0;
		for (int counter = 0; counter < mobsSpawnPerWave.length; counter++) {
			IArenaWaveUpcoming wave = upcomings.get(counter);
			int waveToSpawn = mobsSpawnPerWave[counter];
			int waveLeft = wave.getPreparedMobs().stream().mapToInt(IArenaWaveMob::getAmount).sum();
			if (waveToSpawn > waveLeft) {
				finer += (waveToSpawn - waveLeft);
				mobsSpawnPerWave[counter] = waveLeft;
			}
			else if (waveLeft > waveToSpawn && finer > 0) {
				int diff = waveLeft - waveToSpawn;
				finer -= diff;
				mobsSpawnPerWave[counter] += diff;
			}
		}

		if (Config.DEBUG_MOB_SPAWN) {
			System.out.println("[Spawn Processor] 4. Planned For Whole Arena Round:" + mobsPlannedTotal);
			System.out.println("[Spawn Processor] 5. Calculated to Spawn:" + mobsSpawnTotal);
			System.out.println("[Spawn Processor] 6. Mobs Per Each Wave:" + Arrays.toString(mobsSpawnPerWave));
		}


		for (int counterWave = 0; counterWave < mobsSpawnPerWave.length; counterWave++) {
			IArenaWaveUpcoming waveUpcoming = upcomings.get(counterWave);
			IArenaRegionWave regionWave = waveUpcoming.getRegionWave();
			List<Location> spawners = waveUpcoming.getPreparedSpawners();
			int mobsWave = mobsSpawnPerWave[counterWave];

			int[] mobsPerSpawner = NumberUT.splitIntoParts(mobsWave, spawners.size());
			if (Config.DEBUG_MOB_SPAWN) System.out.println("[Spawn Processor] 7. Mobs Per Region Spawner for '" + regionWave.getArenaWaveIds() + "': " + Arrays.toString(mobsPerSpawner));

			for (int counterSpawner = 0; counterSpawner < mobsPerSpawner.length; counterSpawner++) {
				int mobsSpawner = mobsPerSpawner[counterSpawner];

				for (int countSpawned = 0; countSpawned < mobsSpawner; countSpawned++) {
					IArenaWaveMob waveMob = Rnd.get(waveUpcoming.getPreparedMobs());
					if (waveMob == null) {
						if (Config.DEBUG_MOB_SPAWN) System.out.println("Invalid mob");
						continue;
					}

					int mobsSpawned = Math.min(waveMob.getAmount(), /*mobsSpawner*/ 1);
					waveMob.setAmount(waveMob.getAmount() - mobsSpawned);

					for (int s = 0; s < mobsSpawned; s++) {
						plugin.getMobManager().spawnMob(this, waveMob, spawners.get(counterSpawner));
					}

					if (waveMob.getAmount() <= 0) {
						waveUpcoming.getPreparedMobs().remove(waveMob);
					}
				}
			}
		}

		this.getUpcomingWaves().removeIf(IArenaWaveUpcoming::isAllMobsSpawned);
	}

	@Override
	public void newWave() {
		this.getUpcomingWaves().clear();
		this.setWaveNumber(this.getWaveNumber() + 1);
		this.gradualMobsTimer = 0;
		this.killMobs();

		int finalWave = this.getConfig().getWaveManager().getFinalWave();
		if (finalWave > 0 && this.getWaveNumber() > finalWave) {
			this.stop(EndType.FINISH);
			return;
		}

		// Move all players that are outside of the active region
		// to the first active one.
		IArenaRegion regionActive = this.getConfig().getRegionManager().getRegionAnyAvailable();
		this.getPlayersIngame().forEach(arenaPlayer -> {
			arenaPlayer.addStats(StatType.WAVES_PASSED, 1);

			IArenaRegion regionPlayer = arenaPlayer.getRegion(false);
			if ((regionPlayer == null || regionPlayer.getState() == ArenaLockState.LOCKED) && regionActive != null) {
				arenaPlayer.getPlayer().teleport(regionActive.getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
			}
		});

		ArenaWaveStartEvent event = new ArenaWaveStartEvent(this);
		plugin().getPluginManager().callEvent(event);

		// Join all late joined players at the start of the new round.
		this.getPlayersLate().forEach(this::joinGame);

		// Set time until next wave
		this.setWaveNextTimeleft(this.getConfig().getWaveManager().getDelayDefault());

		//this.setSpawnedMobsAmount(0);
		this.setGradualMobsKilled(0);

		// New wave is started, store the complete amount of mobs from all upcoming waves.
		// This value is TOTAL amount of mobs that arena is about to spawn this round.
		this.setMobsTotalAmount(this.getUpcomingWaves().stream()
				.filter(Predicate.not(IArenaWaveUpcoming::isAllMobsSpawned))
				.mapToInt(wave -> wave.getPreparedMobs().stream().mapToInt(IArenaWaveMob::getAmount).sum()).sum());

		// Spawn mobs for new wave
		IArenaWaveManager waveManager = this.getConfig().getWaveManager();
		this.spawnMobs(waveManager.isGradualSpawnEnabled() ? waveManager.getGradualSpawnPercentFirst() : 100D);

		//this.setLastBossAmount(this.getBosses().size());

		this.getPlayers().stream().map(ArenaPlayer::getPlayer).forEach(player -> {
			plugin().lang().Arena_Game_Wave_Start.replace(this.replacePlaceholders()).send(player);
		});
	}

	protected void showWaveStatus() {
		ILangMsg label;
		if (this.isNextWaveAllowed()) {
			label = this.isLatestWave() ? plugin.lang().Arena_Game_Wave_TimerEnd : plugin.lang().Arena_Game_Wave_Timer;
		}
		else {
			label = plugin.lang().Arena_Game_Wave_Progress;
		}

		this.getPlayers().forEach(arenaPlayer -> label.replace(this.replacePlaceholders()).send(arenaPlayer.getPlayer()));
	}

	@Override
	public void updateMobTarget(@NotNull LivingEntity entity, boolean force) {
		if (!force && plugin().getPMS().getTarget(entity) != null) {
			return;
		}

		ArenaPlayer arenaPlayer = this.getPlayerRandom();
		if (arenaPlayer == null) return;

		plugin().getPMS().setTarget(entity, arenaPlayer.getPlayer());
	}
}
