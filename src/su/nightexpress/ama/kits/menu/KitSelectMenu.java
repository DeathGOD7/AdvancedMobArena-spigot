package su.nightexpress.ama.kits.menu;

import org.bukkit.Sound;
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

public class KitSelectMenu extends AbstractKitListMenu {
	
	public KitSelectMenu(@NotNull AMA plugin, @NotNull JYML cfg) {
		super(plugin, cfg, "");
	}

	@Override
	@NotNull
	public Predicate<IArenaKit> getFilter(@Nullable ArenaUser user) {
		return kit -> user != null && user.hasKit(kit.getId());
	}

	@Override
	@NotNull
	public IMenuClick getObjectClick(@NotNull Player player, @NotNull IArenaKit kit) {
		return (player1, type, e) -> {
			ArenaPlayer arenaPlayer = plugin.getArenaManager().getArenaPlayer(player1);
			if (arenaPlayer == null) return;

        	if (e.isLeftClick()) {
        		if (!kit.isAvailable(arenaPlayer, true)) {
        			player1.playSound(player1.getLocation(), Sound.BLOCK_ANVIL_BREAK, 1.0f, 1.0f);
        			return;
        		}
        		
				arenaPlayer.setKit(kit);
				plugin.lang().Kit_Select_Success.replace(kit.replacePlaceholders()).send(player1);
				player1.playSound(player1.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1.0f, 1.0f);
				player1.closeInventory();
				
			}
			else if (e.isRightClick()) {
				kit.getPreview().open(player1, 1);
			}
        };
	}
}
