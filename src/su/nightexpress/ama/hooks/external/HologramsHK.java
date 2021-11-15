package su.nightexpress.ama.hooks.external;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.config.api.ILangMsg;
import su.nexmedia.engine.hooks.HookState;
import su.nexmedia.engine.hooks.NHook;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.arena.region.IArenaRegion;
import su.nightexpress.ama.api.arena.type.ArenaLockState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HologramsHK extends NHook<AMA> {

	private Map<Location, Hologram> holograms;
	
	public HologramsHK(@NotNull AMA plugin) {
		super(plugin);
	}

	@Override
	@NotNull
	protected HookState setup() {
		this.holograms = new HashMap<>();
		return HookState.SUCCESS;
	}

	@Override
	protected void shutdown() {
		this.holograms.values().forEach(holo -> holo.delete());
		this.holograms.clear();
	}
	
	public void create(@NotNull Location loc, @NotNull List<String> text) {
		Location loc2 = loc.clone();
		Hologram holo = this.holograms.getOrDefault(loc2, HologramsAPI.createHologram(plugin, loc2));
		holo.clearLines();
		text.forEach(line -> holo.appendTextLine(line));
		this.holograms.putIfAbsent(loc2, holo);
	}
	
	public void delete(@NotNull Location loc) {
		Hologram holo = this.holograms.remove(loc);
		if (holo != null) {
			holo.delete();
		}
	}

	public void updateRegionState(@NotNull IArenaRegion region) {
		this.deleteRegionState(region);

		ILangMsg msg = (region.getState() == ArenaLockState.LOCKED ?
				plugin.lang().Arena_Region_Hologram_State_Locked :
				plugin.lang().Arena_Region_Hologram_State_Unlocked)
						.replace(region.replacePlaceholders());

		this.create(region.getHologramStateLocation(), msg.asList());
	}

	public void deleteRegionState(@NotNull IArenaRegion region) {
		this.delete(region.getHologramStateLocation());
	}
}
