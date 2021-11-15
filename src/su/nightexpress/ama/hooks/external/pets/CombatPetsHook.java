package su.nightexpress.ama.hooks.external.pets;

import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.hooks.HookState;
import su.nexmedia.engine.hooks.NHook;
import su.nightexpress.ama.AMA;

@Deprecated
public class CombatPetsHook extends NHook<AMA> {
	
	public CombatPetsHook(@NotNull AMA plugin) {
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
	
	// TODO
	/*@EventHandler
	public void onJoin(ArenaJoinEvent e) {
		Arena arena = e.getArena();
		if (!arena.getConfig().isRestricted(ArenaRestrict.USER_CUSTOM_PETS)) {
			return;
		}
		Player p = e.getPlayer();
		
		if (PetAPI.hasActivePet(p)) {
			PetAPI.despawnPet(p, DespawnReason.BAD_REGION);
            plugin.lang().Game_Restrict_Pets.send(p, true);
		}	
	}*/

    /*@EventHandler(ignoreCancelled = true)
    public void onPetDamageInArena(EntityDamageByEntityEvent e) {
        Entity dd = e.getDamager();
        Player p;
        
        if (dd instanceof LivingEntity && PetAPI.isPet(dd)) {
            p = PetAPI.getPetData((LivingEntity) dd).getOwner();
        } 
        else if (dd instanceof Projectile && PetAPI.isPet((LivingEntity) ((Projectile) dd).getShooter())) {
        	p = PetAPI.getPetData((LivingEntity) ((Projectile) dd).getShooter()).getOwner();
        } 
        else {
            return;
        }
        
        if (isInArena(p)) {
            e.setCancelled(false);
        }
    }*/
}
