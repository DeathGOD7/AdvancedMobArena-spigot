package su.nightexpress.ama.stats;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.manager.AbstractManager;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.utils.LocUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.arena.IArena;

import java.util.*;

public class HologramHandler extends AbstractManager<AMA> {

    private final StatsManager                         statsManager;
    private       Map<StatType, Map<Hologram, String>> holograms;

    public HologramHandler(@NotNull StatsManager statsManager) {
        super(statsManager.plugin());
        this.statsManager = statsManager;
    }

    public void onLoad() {
        JYML cfg = this.statsManager.getConfig();
        this.holograms = new HashMap<>();

        for (StatType statType : StatType.values()) {
            for (String raw : cfg.getStringList("Holograms." + statType.name())) {
                String[] rawSplit = raw.split(":");
                String rawLoc = rawSplit[0];
                String rawArena = null;

                if (rawSplit.length >= 2) {
                    rawArena = rawSplit[1];
                    IArena arena = plugin.getArenaManager().getArenaById(rawArena);
                    if (arena == null) {
                        plugin.error("Invalid arena '" + rawArena + "' in stats hologram: " + raw);
                        continue;
                    }
                }

                Location location = LocUT.deserialize(rawLoc);
                if (location == null) continue;

                this.add(location, statType, rawArena);
            }
        }
    }

    public void onShutdown() {
        JYML cfg = this.statsManager.getConfig();
        cfg.set("Holograms", null);

        this.holograms.forEach((statType, map) -> {
            List<String> list = new ArrayList<>();
            map.forEach((holo, arena) -> {
                String locRaw = LocUT.serialize(holo.getLocation());
                if (arena != null) locRaw += (":" + arena);
                list.add(locRaw);
            });
            cfg.set("Holograms." + statType.name(), list);
        });
        cfg.saveChanges();

        this.holograms.values().forEach(map -> {
            map.keySet().forEach(Hologram::delete);
        });
        this.holograms.clear();
    }

    public void update() {
        this.holograms.forEach((statType, map) -> {
            map.entrySet().removeIf(entry -> !this.update(entry.getKey(), statType, entry.getValue()));
        });
    }

    private boolean update(@NotNull Hologram hologram, @NotNull StatType statType, @Nullable String arenaId) {
        String header = arenaId == null ?
                plugin.lang().Holograms_Stats_Header_Orphan.getMsg() :
                plugin.lang().Holograms_Stats_Header_Arena.getMsg();

        if (arenaId != null) {
            IArena arena = plugin.getArenaManager().getArenaById(arenaId);
            if (arena == null) {
                hologram.delete();
                return false;
            }

            header = arena.getConfig().replacePlaceholders().apply(header);
        }

        hologram.clearLines();
        hologram.appendTextLine(header.replace(StatsManager.PLACEHOLDER_TOP_TYPE, plugin.lang().getEnum(statType)));

        int pos = 1;
        String line = plugin.lang().Holograms_Stats_Line.getMsg();
        String statName = plugin.lang().getEnum(statType);
        for (Map.Entry<String, Integer> e : statsManager.getTopScores(arenaId, statType, 10)) {
            hologram.appendTextLine(line
                    .replace(StatsManager.PLACEHOLDER_TOP_POSITION, String.valueOf(pos++))
                    .replace(StatsManager.PLACEHOLDER_TOP_NAME, e.getKey())
                    .replace(StatsManager.PLACEHOLDER_TOP_SCORE, String.valueOf(e.getValue()))
                    .replace(StatsManager.PLACEHOLDER_TOP_TYPE, statName)
            );
        }

        return true;
    }

    public boolean removeNear(@NotNull Location location) {
        for (Map<Hologram, String> map : this.holograms.values()) {
            for (Hologram holo : map.keySet()) {
                if (holo.getLocation().distance(location) < 5) {
                    holo.delete();
                    map.remove(holo);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean add(@NotNull Location location, @NotNull StatType statType) {
        return this.add(location, statType, null);
    }

    public boolean add(@NotNull Location location, @NotNull StatType statType, @Nullable String arena) {
        Hologram hologram = HologramsAPI.createHologram(plugin, LocUT.getCenter(location));
        if (!this.update(hologram, statType, arena)) return false;

        Map<Hologram, String> mapHolo = this.holograms.computeIfAbsent(statType, map -> new HashMap<>());
        mapHolo.put(hologram, arena);

        return true;
    }
}
