package su.nightexpress.ama.arena.wave;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.ama.api.arena.wave.IArenaWave;
import su.nightexpress.ama.api.arena.wave.IArenaWaveMob;

public class ArenaWaveMob implements IArenaWaveMob {

	private final IArenaWave arenaWave;

	private String mobId;
	private int    amount;
	private int level;
	private double chance;
	
	public ArenaWaveMob(@NotNull IArenaWave arenaWave, @NotNull String mobId, int amount, int level, double chance) {
		this.arenaWave = arenaWave;
		this.setMobId(mobId);
		this.setAmount(amount);
		this.setLevel(level);
		this.setChance(chance);
	}

	public ArenaWaveMob(@NotNull IArenaWaveMob waveMob) {
		this(waveMob.getArenaWave(), waveMob.getMobId(), waveMob.getAmount(), waveMob.getLevel(), waveMob.getChance());
	}

	@NotNull
	@Override
	public IArenaWave getArenaWave() {
		return arenaWave;
	}

	@NotNull
	public String getMobId() {
		return this.mobId;
	}
	
	public void setMobId(@NotNull String mobId) {
		this.mobId = mobId;
	}
	
	public int getAmount() {
		return this.amount;
	}
	
	public void setAmount(int amount) {
		this.amount = Math.max(0, amount);
	}

	@Override
	public int getLevel() {
		return level;
	}

	@Override
	public void setLevel(int level) {
		this.level = Math.max(1, level);
	}

	public double getChance() {
		return this.chance;
	}
	
	public void setChance(double chance) {
		this.chance = chance;
	}
}
