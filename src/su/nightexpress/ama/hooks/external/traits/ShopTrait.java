package su.nightexpress.ama.hooks.external.traits;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import su.nightexpress.ama.api.ArenaAPI;
import su.nightexpress.ama.arena.ArenaPlayer;

@TraitName("ama-shop")
public class ShopTrait extends Trait {

	public ShopTrait() {
		super("ama-shop");
	}

    @EventHandler
    public void click(NPCRightClickEvent e) {
        if (e.getNPC() == this.getNPC()) {
        	Player p = e.getClicker();
        	ArenaPlayer arenaPlayer = ArenaAPI.getArenaManager().getArenaPlayer(p);
        	if (arenaPlayer == null) return;
        	
        	arenaPlayer.getArena().getConfig().getShopManager().open(p);
        }
    }
}
