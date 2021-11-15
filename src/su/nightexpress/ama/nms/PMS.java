package su.nightexpress.ama.nms;

import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PMS {
    
	void setTNTSource(@NotNull TNTPrimed tnt, @NotNull Player player);
	
    LivingEntity spawnMob(@NotNull EntityType type, @NotNull Location loc);
    
    void setTarget(@NotNull LivingEntity mob, @Nullable LivingEntity target);
    
    LivingEntity getTarget(@NotNull LivingEntity entity);
    
    void load();
    
    //
    
    int visualEntityAdd(@NotNull Player p, @NotNull String name, @NotNull Location loc);
    
    int visualGlowBlockAdd(@NotNull Player p, @NotNull Location loc);
    
    void visualEntityRemove(@NotNull Player p, int... id);
    
    // Dynamic Spots
    
	@NotNull
	public Object getChangeBlockPacket(@NotNull BlockData blockData, @NotNull Location loc);
}
