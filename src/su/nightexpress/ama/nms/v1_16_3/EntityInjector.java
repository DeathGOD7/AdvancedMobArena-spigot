package su.nightexpress.ama.nms.v1_16_3;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.Entity;
import net.minecraft.server.v1_16_R3.EntityTypes;
import net.minecraft.server.v1_16_R3.EnumMobSpawn;
import net.minecraft.server.v1_16_R3.World;
import net.minecraft.server.v1_16_R3.WorldServer;
import su.nexmedia.engine.utils.Reflex;

public class EntityInjector {
    
    private static Map<EntityType, EntityTypes<?>> types = new HashMap<>();
    
    public static void setup() {
    	for (EntityType eType : EntityType.values()) {
    		if (!eType.isAlive() || !eType.isSpawnable()) continue;
    		
    		String eName = eType == EntityType.SNOWMAN ? "SNOW_GOLEM" : eType.name();
    		
    		Object rType = Reflex.getFieldValue(EntityTypes.class, eName);
    		EntityTypes<?> typez = (EntityTypes<?>) rType;
    		
    		types.put(eType, typez);
    	}
    }
    
    @Nullable
    public static Entity spawnEntity(@NotNull EntityType type, @NotNull Location location) {
    	if (types.get(type) == null) return null;
    	
    	org.bukkit.World bukkitWorld = location.getWorld();
    	if (bukkitWorld == null) return null;
    	
    	World world = (World) ((CraftWorld)bukkitWorld).getHandle();
        Entity entity = types.get(type).createCreature((WorldServer) world,
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
}
