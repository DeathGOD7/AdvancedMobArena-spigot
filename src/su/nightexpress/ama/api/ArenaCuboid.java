package su.nightexpress.ama.api;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArenaCuboid {
	
	private int xMin;
    private int xMax;
    
    private int yMin;
    private int yMax;
    
    private int zMin;
    private int zMax;
    
    private Location locMin;
    private Location locMax;
    
    private Location center;

    @NotNull
    public static ArenaCuboid empty() {
        return new ArenaCuboid();
    }

    private ArenaCuboid() {
        this.xMin = 0; this.xMax = 0;
        this.yMin = 0; this.yMax = 0;
        this.zMin = 0; this.zMax = 0;
    }

    public ArenaCuboid(@NotNull Location from, @NotNull Location to) {
    	this.redefine(from, to);
    }
    
    public void redefine(@NotNull Location from, @NotNull Location to) {
        this.xMin = Math.min(from.getBlockX(), to.getBlockX());
        this.yMin = Math.min(from.getBlockY(), to.getBlockY());
        this.zMin = Math.min(from.getBlockZ(), to.getBlockZ());
        
        this.xMax = Math.max(from.getBlockX(), to.getBlockX());
        this.yMax = Math.max(from.getBlockY(), to.getBlockY());
        this.zMax = Math.max(from.getBlockZ(), to.getBlockZ());
        
        this.locMin = new Location(from.getWorld(), this.xMin, this.yMin, this.zMin);
        this.locMax = new Location(from.getWorld(), this.xMax, this.yMax, this.zMax);
        
        double cx = xMin + (xMax - xMin) / 2D;
        double cy = yMin + (yMax - yMin) / 2D;
        double cz = zMin + (zMax - zMin) / 2D;
        
        this.center = new Location(from.getWorld(), cx, cy, cz);
    }

    public boolean isEmpty() {
        return this.xMin == 0 && this.xMax == 0 && this.yMin == 0 && this.yMax == 0
                && this.zMin == 0 && this.zMax == 0;
    }
    
    public boolean contains(@NotNull Location location) {
        if (this.isEmpty()) return false;

    	World world = location.getWorld();
    	if (world == null || !world.equals(this.locMin.getWorld())) return false;
    	
    	int x = location.getBlockX();
    	int y = location.getBlockY();
    	int z = location.getBlockZ();
    	
        return x >= this.xMin && x <= this.xMax 
        		&& y >= this.yMin && y <= this.yMax 
        		&& z >= this.zMin && z <= this.zMax;
    }
    
    @NotNull
    public List<Block> getBlocks() {
        if (this.isEmpty()) return Collections.emptyList();

    	List<Block> list = new ArrayList<>();
    	World world = this.center.getWorld();
    	if (world == null) return list;
    	
    	for (int x = this.xMin; x <= this.xMax; ++x) {
    		for (int y = this.yMin; y <= this.yMax; ++y) {
    			for (int z = this.zMin; z <= this.zMax; ++z) {
    				Block blockAt = world.getBlockAt(x, y, z);
    				list.add(blockAt);
    			}
    		}
    	}
    	
    	return list;
    }
    
    public int getSize() {
        return (this.xMax - this.xMin + 1) * (this.yMax - this.yMin + 1) * (this.zMax - this.zMin + 1);
    }
    
    @NotNull
    public Location getLocationMin() {
    	return this.locMin;
    }
    
    @NotNull
    public Location getLocationMax() {
    	return this.locMax;
    }
    
    @NotNull
    public Location getCenter() {
    	return this.center;
    }
}
