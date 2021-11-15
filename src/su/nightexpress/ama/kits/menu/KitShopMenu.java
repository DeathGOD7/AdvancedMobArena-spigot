package su.nightexpress.ama.kits.menu;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.menu.IMenuClick;
import su.nexmedia.engine.config.api.JYML;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.kits.IArenaKit;
import su.nightexpress.ama.arena.ArenaPlayer;
import su.nightexpress.ama.data.ArenaUser;

import java.util.function.Predicate;

public class KitShopMenu extends AbstractKitListMenu {
	
	public KitShopMenu(@NotNull AMA plugin, @NotNull JYML cfg) {
		super(plugin, cfg, "");
	}

	@Override
	@NotNull
	public Predicate<IArenaKit> getFilter(@Nullable ArenaUser user) {
		return kit -> user != null && !user.hasKit(kit.getId());
	}

	@Override
	@NotNull
	public IMenuClick getObjectClick(@NotNull Player player, @NotNull IArenaKit kit) {
		return (player1, type, e) -> {
			ArenaPlayer arenaPlayer = plugin.getArenaManager().getArenaPlayer(player1);
			if (arenaPlayer == null) return;

        	if (e.isLeftClick()) {
				if (kit.buy(arenaPlayer)) {
					this.open(player1, this.getPage(player1));
				}
			}
			else if (e.isRightClick()) {
				kit.getPreview().open(player1, 1);
			}
        };
	}
}
