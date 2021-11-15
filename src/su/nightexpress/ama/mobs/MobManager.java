package su.nightexpress.ama.mobs;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.manager.AbstractManager;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.hooks.external.MythicMobsHK;
import su.nexmedia.engine.utils.Constants;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.arena.IArena;
import su.nightexpress.ama.api.arena.wave.IArenaWaveMob;
import su.nightexpress.ama.config.Config;

import java.util.*;

public class MobManager extends AbstractManager<AMA> {

	private Map<String, ArenaCustomMob> mobs;
	
	private static final String META_ARENA_ID    = "ARENA_MOB_ARENA";
	private static final String META_TEMPLATE_ID = "ARENA_MOB_ID";
	private static final String META_LEVEL       = "ARENA_MOB_LEVEL";
	public static final String META_ARENA_ITEM = "AMA_ARENA_JUNK";
	
	private MythicMobsHK mythicMobs;
	
	public MobManager(@NotNull AMA plugin) {
		super(plugin);
	}

	@Override
	public void onLoad() {
		this.mobs = new HashMap<>();
		this.plugin.getConfigManager().extract("/mobs/");

		for (JYML cfg : JYML.loadAll(plugin.getDataFolder() + "/mobs/", false)) {
			try {
				ArenaCustomMob mob = new ArenaCustomMob(plugin, cfg);
				this.mobs.put(mob.getId().toLowerCase(), mob);
			}
			catch (Exception ex) {
				plugin.error("Could not load mob: " + cfg.getFile().getName());
				ex.printStackTrace();
			}
		}
		plugin.info("Mobs Loaded: " + mobs.size());
		
		this.mythicMobs = plugin.getMythicMobs();
	}
	
	@Override
	public void onShutdown() {
		this.mobs.values().forEach(ArenaCustomMob::clear);
		this.mobs.clear();
	}

	@NotNull
	public List<String> getMobIds() {
		return new ArrayList<>(this.mobs.keySet());
	}

	@NotNull
	public Collection<ArenaCustomMob> getMobs() {
		return this.mobs.values();
	}

	@Nullable
	public ArenaCustomMob getMobById(@NotNull String id) {
		return this.mobs.get(id.toLowerCase());
	}
	
	@NotNull
	public List<String> getSupportedMobIds() {
		List<String> list = new ArrayList<>(this.getMobIds());
		if (this.mythicMobs != null) {
			list.addAll(this.mythicMobs.getMythicIds());
		}
		return list;
	}
	
	@Nullable
	public LivingEntity spawnMob(@NotNull IArena arena, @NotNull IArenaWaveMob waveMob, @NotNull Location loc2) {
		String mobId = waveMob.getMobId();
		Location loc = loc2.clone().add(0,1,0); // Fix block position
		ArenaCustomMob customMob = this.getMobById(mobId);
		int level = waveMob.getLevel();

		LivingEntity entity;
		
		if (this.mythicMobs != null && this.mythicMobs.isValid(mobId)) {
			entity = (LivingEntity) this.mythicMobs.spawnMythicMob(mobId, loc, level);
			if (entity == null) return null;
		}
		else if (customMob != null) {
			EntityType type = customMob.getEntityType();
			entity = plugin.getPMS().spawnMob(type, loc);
			
			if (entity == null) {
				World world = loc.getWorld();
				if (world == null) return null;
				
				Entity e = world.spawnEntity(loc, type);
				if (!(e instanceof LivingEntity)) {
					e.remove();
					return null;
				}
				
				entity = (LivingEntity) e;
			}
			customMob.applySettings(entity, level);
			customMob.applyAttributes(entity, level);
			
			ArenaMobHealthBar healthBar = customMob.getHealthBar();
			if (healthBar != null) {
				healthBar.create(arena.getPlayersIngame(), entity);
			}
			this.setTemplate(entity, customMob);
		}
		else return null;
		
		this.setArena(entity, arena); // Add Arena meta
		this.setLevel(entity, level);
		
		/*if (custom != null && custom.isBoss()) {
			arena.addBoss(entity);
		}
		else {
			arena.addMob(entity);
		}*/
		arena.getMobs().add(entity);
		arena.updateMobTarget(entity, true);
    	
		entity.setRemoveWhenFarAway(false);
    	return entity;
	}
	
	public void setArena(@NotNull LivingEntity entity, @NotNull IArena arena) {
		entity.setMetadata(META_ARENA_ID, new FixedMetadataValue(plugin, arena.getId()));
	}
	
	private void setTemplate(@NotNull LivingEntity entity, @NotNull ArenaCustomMob customMob) {
		entity.setMetadata(META_TEMPLATE_ID, new FixedMetadataValue(plugin, customMob.getId()));
	}
	
	private void setLevel(@NotNull LivingEntity entity, int level) {
		entity.setMetadata(META_LEVEL, new FixedMetadataValue(plugin, level));
	}

	@NotNull
	private String getMobId(@NotNull LivingEntity entity) {
		if (this.mythicMobs != null && this.mythicMobs.isMythicMob(entity)) {
			return this.mythicMobs.getMythicNameByEntity(entity).toLowerCase();
		}
		ArenaCustomMob customMob = this.getEntityTemplate(entity);
		if (customMob != null) {
			return customMob.getId();
		}
		return entity.getType().name().toLowerCase();
	}

	public double getMobCoins(@NotNull LivingEntity entity) {
		IArena arena = this.getEntityArena(entity);
		if (arena == null) return 0D;

		String id = this.getMobId(entity);
		Map<String, Double> moneyMap = Config.MOBS_COINS_TABLE;

		return moneyMap.getOrDefault(id, moneyMap.getOrDefault(Constants.DEFAULT, 0D));
	}

	public int getMobScore(@NotNull LivingEntity entity) {
		IArena arena = this.getEntityArena(entity);
		if (arena == null) return 0;

		String id = this.getMobId(entity);
		Map<String, Integer> moneyMap = Config.MOBS_SCORE_TABLE;

		return moneyMap.getOrDefault(id, moneyMap.getOrDefault(Constants.DEFAULT, 0));
	}
	
	public boolean isArenaEntity(@NotNull Entity entity) {
		return entity.hasMetadata(META_ARENA_ID);
	}
	
	public boolean isCustomEntity(@NotNull Entity entity) {
		return entity.hasMetadata(META_TEMPLATE_ID);
	}
	
	@Nullable
	public IArena getEntityArena(@NotNull Entity entity) {
		if (!this.isArenaEntity(entity)) return null;
		
		String id = entity.getMetadata(META_ARENA_ID).get(0).asString();
		return plugin.getArenaManager().getArenaById(id);
	}
	
	@Nullable
	public ArenaCustomMob getEntityTemplate(@NotNull Entity entity) {
		if (!this.isCustomEntity(entity)) return null;
		
		String id = entity.getMetadata(META_TEMPLATE_ID).get(0).asString();
		return this.getMobById(id);
	}
	
	public int getEntityLevel(@NotNull Entity entity) {
		if (entity.hasMetadata(META_LEVEL)) {
			return entity.getMetadata(META_LEVEL).get(0).asInt();
		}
		if (this.mythicMobs != null && this.mythicMobs.isMythicMob(entity)) {
			return (int) this.mythicMobs.getLevel(entity);
		}
		return 1;
	}
}
