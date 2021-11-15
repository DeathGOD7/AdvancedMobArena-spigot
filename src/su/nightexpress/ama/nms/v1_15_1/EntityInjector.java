package su.nightexpress.ama.nms.v1_15_1;

import net.minecraft.server.v1_15_R1.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.utils.Reflex;

import java.util.HashMap;
import java.util.Map;

public class EntityInjector {
    
    private static Map<EntityType, EntityTypes<?>> types = new HashMap<>();
    
    public static void setup() {
    	for (EntityType eType : EntityType.values()) {
    		if (!eType.isAlive() || !eType.isSpawnable()) continue;
    		
    		Object rType = Reflex.getFieldValue(EntityTypes.class, eType.name());
    		EntityTypes<?> typez = (EntityTypes<?>) rType;
    		
    		types.put(eType, typez);
    	}
    }
    
    @Nullable
    public static Entity spawnEntity(@NotNull EntityType type, @NotNull Location location) {
    	if (!types.containsKey(type)) return null;
    	
    	org.bukkit.World bukkitWorld = location.getWorld();
    	if (bukkitWorld == null) return null;
    	
    	World world = ((CraftWorld)bukkitWorld).getHandle();

        Entity entity = types.get(type).createCreature(world,
                null,
                null,
                null,
                new BlockPosition(location.getX(), location.getY(), location.getZ()),
                EnumMobSpawn.EVENT, false, false);
        if (entity == null) return null;
        
        world.addEntity(entity, SpawnReason.CUSTOM);
        entity.getBukkitEntity().teleport(location);

        return entity;
    }
    
    public static void registerEntity(@NotNull EntityType type, @NotNull EntityTypes.b<?> customType) {
        String customName = "arena_" + type.name().toLowerCase();
        
        EntityTypes.a<Entity> typesA = EntityTypes.a.a(customType, EnumCreatureType.CREATURE);
        EntityTypes<?> eTypes = IRegistry.a(IRegistry.ENTITY_TYPE, customName, typesA.a(customName));
        
        types.put(type, eTypes);
    }
}
