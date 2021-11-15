package su.nightexpress.ama.arena.setup;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.EffectUT;
import su.nexmedia.engine.utils.LocUT;
import su.nightexpress.ama.AMA;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class ArenaSetupUtils {

	private static final AMA                        PLUGIN      = AMA.getInstance();
	private static final Map<Player, List<Integer>> VISUALS_MAP = new WeakHashMap<>();
	
	public static void removeVisuals(@NotNull Player player) {
		List<Integer> list = VISUALS_MAP.remove(player);
		if (list == null) return;
		list.forEach(id -> PLUGIN.getPMS().visualEntityRemove(player, id));
	}
	
	public static void addVisualText(@NotNull Player player, @NotNull String name, @NotNull Location loc) {
		List<Integer> list = VISUALS_MAP.computeIfAbsent(player, k -> new ArrayList<>());
		
		Location clone = new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ());
		int id = PLUGIN.getPMS().visualEntityAdd(player, name, LocUT.getCenter(LocUT.getFirstGroundBlock(clone.add(0,1,0)), false));
		list.add(id);
	}
	
	public static void addVisualBlock(@NotNull Player player, @NotNull Location location) {
		List<Integer> list = VISUALS_MAP.computeIfAbsent(player, k -> new ArrayList<>());
		
		Location clone = new Location(location.getWorld(), location.getX(), location.getY(), location.getZ());
		int id = PLUGIN.getPMS().visualGlowBlockAdd(player, LocUT.getCenter(clone, false));
		list.add(id);
	}

	public static void playCuboid(@NotNull Location[] cuboidCache) {
		String e2 = Particle.REDSTONE.name() + ":0,0,255";

		Location to = cuboidCache[1].clone();
		World w = to.getWorld();
		double height = to.getBlockY();

		Location downPointStart = LocUT.getCenter(cuboidCache[0].clone());
		Location downPointMidA = LocUT.getCenter(new Location(w, to.getBlockX(), downPointStart.getBlockY(), downPointStart.getBlockZ()));
		Location downPointEnd = LocUT.getCenter(new Location(w, to.getBlockX(), downPointStart.getBlockY(), to.getBlockZ()));
		Location downPointMidB = LocUT.getCenter(new Location(w, downPointStart.getBlockX(), downPointStart.getBlockY(), to.getBlockZ()));

		Location downPointCornerA = LocUT.getCenter(new Location(w, downPointMidA.getBlockX(), height, downPointMidA.getBlockZ()));
		Location downPointCornerB = LocUT.getCenter(new Location(w, downPointMidB.getBlockX(), height, downPointMidB.getBlockZ()));

		Location downPointCornerC = LocUT.getCenter(new Location(w, downPointStart.getBlockX(), height, downPointStart.getBlockZ()));
		Location downPointCornerD = LocUT.getCenter(new Location(w, downPointEnd.getBlockX(), height, downPointEnd.getBlockZ()));

		EffectUT.drawLine(downPointStart, downPointMidA, e2, 0, 0, 0, 0, 10);
		EffectUT.drawLine(downPointMidA, downPointEnd, e2, 0, 0, 0, 0, 10);

		EffectUT.drawLine(downPointStart, downPointMidB, e2, 0, 0, 0, 0, 10);
		EffectUT.drawLine(downPointMidB, downPointEnd, e2, 0, 0, 0, 0, 10);

		EffectUT.drawLine(downPointMidA, downPointCornerA, e2, 0, 0, 0, 0, 10);
		EffectUT.drawLine(downPointMidB, downPointCornerB, e2, 0, 0, 0, 0, 10);

		EffectUT.drawLine(downPointStart, downPointCornerC, e2, 0, 0, 0, 0, 10);
		EffectUT.drawLine(downPointEnd, downPointCornerD, e2, 0, 0, 0, 0, 10);

		//

		Location topPointStart = LocUT.getCenter(new Location(w, downPointStart.getBlockX(), to.getBlockY(), downPointStart.getBlockZ()));
		Location topPointEnd = LocUT.getCenter(new Location(w, to.getBlockX(), to.getBlockY(), to.getBlockZ()));

		Location topPointMidA = LocUT.getCenter(new Location(w, to.getBlockX(), to.getBlockY(), topPointStart.getBlockZ()));
		Location topPointMidB = LocUT.getCenter(new Location(w, topPointStart.getBlockX(), to.getBlockY(), to.getBlockZ()));

		EffectUT.drawLine(topPointStart, topPointMidA, e2, 0, 0, 0, 0, 10);
		EffectUT.drawLine(topPointMidA, topPointEnd, e2, 0, 0, 0, 0, 10);

		EffectUT.drawLine(topPointStart, topPointMidB, e2, 0, 0, 0, 0, 10);
		EffectUT.drawLine(topPointMidB, topPointEnd, e2, 0, 0, 0, 0, 10);
	}
}
