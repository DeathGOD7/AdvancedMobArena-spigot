package su.nightexpress.ama.hooks.external;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import com.earth2me.essentials.Essentials;

import su.nexmedia.engine.hooks.HookState;
import su.nexmedia.engine.hooks.NHook;
import su.nightexpress.ama.AMA;

public class EssentialsHK extends NHook<AMA> {
	
	private Essentials ess;
	
	public EssentialsHK(@NotNull AMA plugin) {
		super(plugin);
	}
	
	@Override
	@NotNull
	public HookState setup() {
		this.ess = (Essentials) this.plugin.getPluginManager().getPlugin(this.getPlugin());
		if (this.ess != null) {
			return HookState.SUCCESS;
		}
		return HookState.ERROR;
	}

	@Override
	public void shutdown() {
		
	}

	public void disableGod(@NotNull Player player) {
		this.ess.getUser(player).setGodModeEnabled(false);
	}
}
