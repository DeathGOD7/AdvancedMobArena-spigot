package su.nightexpress.ama.hooks.external;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.trait.TraitInfo;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.hooks.HookState;
import su.nexmedia.engine.hooks.NHook;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.hooks.external.traits.ArenasTrait;
import su.nightexpress.ama.hooks.external.traits.KitSelectorTrait;
import su.nightexpress.ama.hooks.external.traits.KitShopTrait;

import java.util.HashSet;
import java.util.Set;

@Deprecated
public class CitizensHK extends NHook<AMA> {
	
	private Set<TraitInfo> traits;
	
	public CitizensHK(@NotNull AMA plugin) {
		super(plugin);
	}
	
	@Override
	@NotNull
	public HookState setup() {
		this.registerTraits();
		
		return HookState.SUCCESS;
	}
	
	@Override
	public void shutdown() {
		this.unregisterTraits();
	}
	
    private void registerTraits() {
    	this.traits = new HashSet<>();
    	
    	this.traits.add(TraitInfo.create(ArenasTrait.class));
	    //this.traits.add(TraitInfo.create(StatsTrait.class));
    	this.traits.add(TraitInfo.create(KitShopTrait.class));
    	this.traits.add(TraitInfo.create(KitSelectorTrait.class));
    	
    	this.traits.forEach(trait -> CitizensAPI.getTraitFactory().registerTrait(trait));
    }

    private void unregisterTraits() {
    	this.traits.forEach(trait -> CitizensAPI.getTraitFactory().deregisterTrait(trait));
    	this.traits.clear();
    	this.traits = null;
    }
}
