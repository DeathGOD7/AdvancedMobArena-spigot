package su.nightexpress.ama.hooks.external;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.hooks.HookState;
import su.nexmedia.engine.hooks.NHook;
import su.nexmedia.sunlight.SunLight;
import su.nexmedia.sunlight.data.SunUser;
import su.nexmedia.sunlight.modules.scoreboard.ScoreboardManager;
import su.nightexpress.ama.AMA;

public class SunLightHK extends NHook<AMA> {
	
	private SunLight sunLight;
	
	public SunLightHK(@NotNull AMA plugin) {
		super(plugin);
	}
	
	@Override
	@NotNull
	public HookState setup() {
		this.sunLight = (SunLight) plugin.getPluginManager().getPlugin(this.getPlugin());
		if (this.sunLight == null) return HookState.ERROR;
		
		return HookState.SUCCESS;
	}

	@Override
	public void shutdown() {
		
	}

	public void disableGod(@NotNull Player player) {
		SunUser user = this.sunLight.getUserManager().getOrLoadUser(player);
		if (user != null) user.setGodMode(false);
	}
	
	public void disableBoard(@NotNull Player player) {
		ScoreboardManager scoreboardManager = this.sunLight.getModuleCache().getScoreboardManager();
		if (scoreboardManager == null) return;
		
		scoreboardManager.removeBoard(player);
	}
}
