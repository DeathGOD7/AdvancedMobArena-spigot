package su.nightexpress.ama.arena;

import java.util.List;
import java.util.function.DoubleUnaryOperator;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.config.api.ILangMsg;
import su.nexmedia.engine.utils.PlayerUT;

public class ArenaKillStreak {

	private int streak;
	private ILangMsg msg;
	private DoubleUnaryOperator extraMoney;
	private DoubleUnaryOperator extraScore;
	private List<String> commands;
	
	public ArenaKillStreak(
			int streak,
			@NotNull ILangMsg msg,
			@NotNull DoubleUnaryOperator extraMoney,
			@NotNull DoubleUnaryOperator extraScore,
			@NotNull List<String> commands
			) {
		this.streak = streak;
		this.msg = msg.replace("%streak%", this.getStreak());
		this.extraMoney = extraMoney;
		this.extraScore = extraScore;
		this.commands = commands;
	}
	
	public int getStreak() {
		return streak;
	}
	
	@NotNull
	public ILangMsg getMessage() {
		return this.msg;
	}
	
	@NotNull
	public DoubleUnaryOperator getExtraMoney() {
		return this.extraMoney;
	}
	
	@NotNull
	public DoubleUnaryOperator getExtraScore() {
		return this.extraScore;
	}
	
	@NotNull
	public List<String> getCommands() {
		return this.commands;
	}
	
	public void executeCommands(@NotNull Player player) {
		this.getCommands().forEach(cmd -> {
			PlayerUT.execCmd(player, cmd);
		});
	}
}
