package su.nightexpress.ama.arena.spot;

import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.LocUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.arena.game.IArenaGameEventTrigger;
import su.nightexpress.ama.api.arena.spot.IArenaSpot;
import su.nightexpress.ama.api.arena.spot.IArenaSpotState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ArenaSpotState implements IArenaSpotState {

	private final IArenaSpot spot;
	
	private final String                      id;
	private final Set<IArenaGameEventTrigger> triggers;
	private       List<String>                schemeRaw;
	private final Map<Location, BlockData>    scheme;
	
	public ArenaSpotState(
			@NotNull IArenaSpot spot,
			@NotNull String id,
			@NotNull Set<IArenaGameEventTrigger> triggers,
			@NotNull List<String> schemeRaw
			) {
		this.spot = spot;
		this.id = id.toLowerCase();
		
		this.triggers = triggers;
		this.scheme = new HashMap<>();
		this.setSchemeRaw(schemeRaw);
	}

	@Override
	@NotNull
	public String getId() {
		return this.id;
	}
	
	@Override
	@NotNull
	public IArenaSpot getSpot() {
		return this.spot;
	}

	@NotNull
	@Override
	public Set<IArenaGameEventTrigger> getTriggers() {
		return triggers;
	}

	@Override
	public void setSchemeRaw(@NotNull List<String> schemeRaw) {
		this.schemeRaw = schemeRaw;
		this.scheme.clear();
		
		AMA plugin = this.getSpot().plugin();
		for (String block : schemeRaw) {
			String[] blockSplit = block.split("~");
			if (blockSplit.length != 2) {
				plugin.error("Invalid block '" + block + "' in '" + id + "' state of '" + spot.getFile().getName() + "' spot in '" + spot.getArenaConfig().getId() + "' arena!");
				continue;
			}
			Location blockLoc = LocUT.deserialize(blockSplit[0]);
			if (blockLoc == null) {
				plugin.error("Invalid block location '" + block + "' in '" + id + "' state of '" + spot.getFile().getName() + "' spot in '" + spot.getArenaConfig().getId() + "' arena!");
				continue;
			}
			if (this.spot.getCuboid().isEmpty() || !this.spot.getCuboid().contains(blockLoc)) {
				plugin.error("Block is outside of the spot region: '" + block + "' in '" + id + "' state of '" + spot.getFile().getName() + "' spot in '" + spot.getArenaConfig().getId() + "' arena!");
				continue;
			}
			
			BlockData blockData = plugin.getServer().createBlockData(blockSplit[1]);
			this.scheme.put(blockLoc, blockData);
		}
	}
	
	@Override
	@NotNull
	public List<String> getSchemeRaw() {
		return this.schemeRaw;
	}

	@NotNull
	@Override
	public Map<Location, BlockData> getScheme() {
		return scheme;
	}
}
