package su.nightexpress.ama.data;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.data.users.IUserManager;
import su.nightexpress.ama.AMA;

public class UserManager extends IUserManager<AMA, ArenaUser> {
	
	public UserManager(@NotNull AMA plugin) {
		super(plugin);
	}

	@Override
	@NotNull
	protected ArenaUser createData(@NotNull Player player) {
		return new ArenaUser(this.plugin, player);
	}
}
