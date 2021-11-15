package su.nightexpress.ama.api.arena.type;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.config.api.ILangMsg;
import su.nightexpress.ama.AMA;

@Deprecated
public enum LeaveReason {

	OUTSIDE,
	SELF,
	FINISH,
	DEATH,
	KICK,
	NO_KIT,
	FORCE,
	TIMELEFT,
	NO_REGION,
	;

	@Deprecated
	public void sendMessage(@NotNull Player p) {
		AMA plugin = AMA.getInstance();
		ILangMsg title = null;
		
		switch(this) {
			case DEATH: {
				title = plugin.lang().Titles_Leave_Death;
				break;
			}
			case FINISH: {
				title = plugin.lang().Titles_Leave_Finish;
				break;
			}
			case FORCE: {
				title = plugin.lang().Titles_Leave_ForceEnd;
				break;
			}
			case KICK: {
				title = plugin.lang().Titles_Leave_Kick;
				break;
			}
			case NO_KIT: {
				title = plugin.lang().Titles_Leave_NoKit;
				break;
			}
			case OUTSIDE: {
				title = plugin.lang().Titles_Leave_Outside;
				break;
			}
			case SELF: {
				title = plugin.lang().Titles_Leave_Self;
				break;
			}
			case TIMELEFT: {
				title = plugin.lang().Titles_Leave_Timeleft;
				break;
			}
			case NO_REGION: {
				title = plugin.lang().Titles_Leave_NoRegion;
				break;
			}
			default: {
				title = plugin.lang().Titles_Leave_Self;
				break;
			}
		}
		
		title.send(p);
	}
}
