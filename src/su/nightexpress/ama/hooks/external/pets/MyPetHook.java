package su.nightexpress.ama.hooks.external.pets;

import de.Keyle.MyPet.MyPetApi;
import de.Keyle.MyPet.api.entity.MyPet.PetState;
import de.Keyle.MyPet.api.event.MyPetCallEvent;
import de.Keyle.MyPet.api.player.MyPetPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.hooks.HookState;
import su.nexmedia.engine.hooks.NHook;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.arena.IArena;
import su.nightexpress.ama.api.arena.event.ArenaPlayerJoinEvent;
import su.nightexpress.ama.arena.ArenaPlayer;

public class MyPetHook extends NHook<AMA> {
	
	public MyPetHook(@NotNull AMA plugin) {
		super(plugin);
	}

	@Override
	@NotNull
	public HookState setup() {
		this.registerListeners();
		
		return HookState.SUCCESS;
	}
	
	@Override
	public void shutdown() {
		this.unregisterListeners();
	}
	
	@EventHandler
	public void onArenaJoin(ArenaPlayerJoinEvent e) {
		IArena arena = e.getArena();
		if (arena.getConfig().getGameplayManager().isExternalPetsEnabled()) {
			return;
		}
		
		Player p = e.getArenaPlayer().getPlayer();
		if (MyPetApi.getPlayerManager().isMyPetPlayer(p)) {
            MyPetPlayer player = MyPetApi.getPlayerManager().getMyPetPlayer(p);
            if (player.hasMyPet() && player.getMyPet().getStatus() == PetState.Here) {
                player.getMyPet().removePet();
                plugin.lang().Arena_Game_Restrict_NoPets.send(player.getPlayer());
            }
		}	
	}
    
    /*@EventHandler(priority = EventPriority.HIGHEST)
    public void onMyPetDamageInArena(EntityDamageByEntityEvent event) {
        MyPetBukkitEntity damager;
        
        if (event.getDamager() instanceof MyPetBukkitEntity) {
            damager = (MyPetBukkitEntity) event.getDamager();
        } 
        else if (event.getDamager() instanceof Projectile && ((Projectile) event.getDamager()).getShooter() instanceof MyPetBukkitEntity) {
            damager = (MyPetBukkitEntity) ((Projectile) event.getDamager()).getShooter();
        } 
        else {
            return;
        }
        
        if (isInArena(damager.getOwner())) {
            event.setCancelled(false);
        }
    }*/

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onMyPetCall(MyPetCallEvent e) {
    	Player p = e.getOwner().getPlayer();
    	ArenaPlayer arenaPlayer = plugin.getArenaManager().getArenaPlayer(p);
    	if (arenaPlayer == null) return;
    	
    	if (!arenaPlayer.getArena().getConfig().getGameplayManager().isExternalPetsEnabled()) {
    		e.setCancelled(true);
    	}
    }
}
