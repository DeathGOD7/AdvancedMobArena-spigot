package su.nightexpress.ama.stats;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.SignChangeEvent;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.AbstractListener;
import su.nexmedia.engine.utils.CollectionsUT;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.Perms;
import su.nightexpress.ama.api.arena.IArena;

public class StatsListener extends AbstractListener<AMA> {

    private final StatsManager statsManager;

    StatsListener(@NotNull StatsManager statsManager) {
        super(statsManager.plugin());
        this.statsManager = statsManager;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onSignChange(SignChangeEvent e) {
        if (!e.getPlayer().hasPermission(Perms.ADMIN)) return;

        Block block = e.getBlock();
        BlockState state = block.getState();
        if (!(state instanceof Sign sign)) return;

        String line1 = e.getLine(0);
        if (line1 == null || !line1.equalsIgnoreCase("AMA")) return;

        String line2 = e.getLine(1);
        if (line2 == null) return;

        StatType type = CollectionsUT.getEnum(line2, StatType.class);
        if (type == null) return;

        String line3 = e.getLine(2);
        int pos = line3 != null ? StringUT.getInteger(line3, -1) : -1;
        if (pos < 1) return;

        String line4 = e.getLine(3);
        IArena arena = line4 != null ? this.plugin.getArenaManager().getArenaById(line4) : null;

        this.statsManager.addSign(sign, type, pos, arena);
    }
}
