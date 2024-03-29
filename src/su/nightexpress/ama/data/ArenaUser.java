package su.nightexpress.ama.data;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.data.users.IAbstractUser;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.kits.IArenaKit;
import su.nightexpress.ama.stats.StatType;

import javax.annotation.Nullable;
import java.util.*;

public class ArenaUser extends IAbstractUser<AMA> {
	
    private int coins;
    private Set<String> kits;
    private Map<String, Map<StatType, Integer>> stats;
    
    public ArenaUser(@NotNull AMA plugin, @NotNull Player player) {
		this(plugin, player.getUniqueId(), player.getName(), System.currentTimeMillis(), 
			0, // Coins
			new HashSet<>(), // Kits 
			new HashMap<>() // Stats
		);
	}

	public ArenaUser(
    		@NotNull AMA plugin,
    		@NotNull UUID uuid, 
    		@NotNull String name,
    		long login,
    		
    		int coins,
    		@NotNull Set<String> kits,
    		@NotNull Map<String, Map<StatType, Integer>> stats
    		) {
    	super(plugin, uuid, name, login);
        this.setCoins(coins);
        this.setKits(kits);
        this.setStats(stats);
    }
    
    public int getCoins() {
        return this.coins;
    }
    
    public void setCoins(int coins) {
        this.coins = Math.max(0, coins);
    }
    
    @NotNull
    public Set<String> getKits() {
    	return this.kits;
    }
    
    public void setKits(@NotNull Set<String> kits) {
    	this.kits = kits;
    }
    
    public boolean addKit(@NotNull IArenaKit kit) {
    	return this.addKit(kit.getId());
    }
    
    public boolean addKit(@NotNull String kit) {
    	return this.kits.add(kit.toLowerCase());
    }
    
    public boolean hasKit(@NotNull IArenaKit kit) {
		return this.hasKit(kit.getId());
	}

	public boolean hasKit(@NotNull String kit) {
    	return this.kits.contains(kit.toLowerCase());
    }
    
    @NotNull 
    public Map<String, Map<StatType, Integer>> getStats() {
    	return this.stats;
    }
    
    @NotNull 
    public Map<StatType, Integer> getStats(@NotNull String arena) {
    	return this.stats.computeIfAbsent(arena.toLowerCase(), map -> new HashMap<>());
    }
    
    public int getStats(@NotNull StatType type) {
    	return this.getStats(type, null);
    }
    
    public int getStats(@NotNull StatType type, @Nullable String arena) {
    	if (arena != null) {
    		return this.getStats(arena).computeIfAbsent(type, score -> 0);
    	}
    	
		int count = 0;
		for (Map<StatType, Integer> map : this.stats.values()) {
			count += map.computeIfAbsent(type, score -> 0);
		}
		return count;
	}

	public void setStats(@NotNull Map<String, Map<StatType, Integer>> stats) {
		this.stats = stats;
	}
}
