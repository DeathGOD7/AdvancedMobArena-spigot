package su.nightexpress.ama.arena.wave;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.config.api.JYML;
import su.nightexpress.ama.api.arena.game.IArenaGameEventTrigger;
import su.nightexpress.ama.api.arena.wave.IArenaWave;
import su.nightexpress.ama.api.arena.wave.IArenaWaveAmplificator;
import su.nightexpress.ama.api.arena.wave.IArenaWaveManager;
import su.nightexpress.ama.api.arena.wave.IArenaWaveMob;
import su.nightexpress.ama.arena.config.ArenaConfig;
import su.nightexpress.ama.arena.editor.waves.EditorWaveManager;
import su.nightexpress.ama.arena.game.trigger.AbstractArenaGameEventTrigger;

import java.util.*;

public class ArenaWaveManager implements IArenaWaveManager {

	public static final String CONFIG_NAME = "waves.yml";

	private final ArenaConfig arenaConfig;
	private final JYML        config;

	private int finalWave;
    private int delayFirst;
    private int delayDefault;
    
    private boolean gradualSpawnEnabled;
    private double gradualSpawnPercentFirst;
    private int    gradualSpawnNextInterval;
    private double gradualSpawnNextPercent;
    private double gradualSpawnNextKillPercent;

    private Map<String, IArenaWave> waves;
    
    private EditorWaveManager editor;
    
    public ArenaWaveManager(@NotNull ArenaConfig arenaConfig) {
    	this.arenaConfig = arenaConfig;
    	this.config = new JYML(arenaConfig.getFile().getParentFile().getAbsolutePath(), CONFIG_NAME);
    }
    
    @Override
    public void setup() {
	    this.waves = new HashMap<>();
	    
	    this.setFinalWave(config.getInt("Final_Wave", 100));
	    this.setDelayFirst(config.getInt("Delay.First", 5));
	    this.setDelayDefault(config.getInt("Delay.Default", 10));

	    String path = "Gradual_Spawn.";
	    this.setGradualSpawnEnabled(config.getBoolean(path + "Enabled"));
	    this.setGradualSpawnPercentFirst(config.getDouble(path + "First.Amount_Percent", 50));
	    this.setGradualSpawnNextInterval(config.getInt(path + "Next.Time_Interval", 5));
	    this.setGradualSpawnNextPercent(config.getInt(path + "Next.Amount_Percent", 20));
	    this.setGradualSpawnNextKillPercent(config.getInt(path + "Next.For_Killed_Percent", 10));
	    
	    for (String waveId : config.getSection("Waves")) {
	    	String path2 = "Waves." + waveId + ".";

			Map<String, IArenaWaveAmplificator> amplificators = new HashMap<>();
			Map<String, IArenaWaveMob> waveMobs = new HashMap<>();
			IArenaWave wave = new ArenaWave(this.arenaConfig, waveId, waveMobs, amplificators);

			for (String ampId : config.getSection(path2 + "Amplificators")) {
				String path3 = path2 + "Amplificators." + ampId + ".";

				Set<IArenaGameEventTrigger> triggers = AbstractArenaGameEventTrigger.parse(arenaConfig, config, path3 + "Triggers");
				int valueAmount = config.getInt(path3 + "Values.Amount");
				int valueLevel = config.getInt(path3 + "Values.Level");

				IArenaWaveAmplificator amplificator = new ArenaWaveAmplificator(wave, ampId, triggers, valueAmount, valueLevel);
				amplificators.put(amplificator.getId(), amplificator);
			}


	    	for (String mobId : config.getSection(path2 + "Mobs")) {
	    		String path3 = path2 + "Mobs." + mobId + ".";
	    		
	    		int amount = config.getInt(path3 + "Amount");
	    		int level = config.getInt(path3 + "Level");
	    		double chance = config.getDouble(path3 + "Chance");
	    		
	    		IArenaWaveMob mob = new ArenaWaveMob(wave, mobId, amount, level, chance);
	    		waveMobs.put(mob.getMobId(), mob);
	    	}

	    	this.waves.put(wave.getId(), wave);
	    }
    }
    
    @Override
    public void shutdown() {
    	if (this.editor != null) {
    		this.editor.clear();
    		this.editor = null;
    	}
    	if (this.waves != null) {
    		this.waves.values().forEach(IArenaWave::clear);
    		this.waves.clear();
    	}
    }
    
    @Override
	public void onSave() {
		config.set("Final_Wave", this.getFinalWave());

		config.set("Delay.First", this.getDelayFirst());
		config.set("Delay.Default", this.getDelayDefault());
		
		config.set("Gradual_Spawn.Enabled", this.isGradualSpawnEnabled());
		config.set("Gradual_Spawn.First.Amount_Percent", this.getGradualSpawnPercentFirst());
		config.set("Gradual_Spawn.Next.Amount_Percent", this.getGradualSpawnNextPercent());
		config.set("Gradual_Spawn.Next.For_Killed_Percent", this.getGradualSpawnNextKillPercent());
		config.set("Gradual_Spawn.Next.Time_Interval", this.getGradualSpawnNextInterval());

		config.set("Waves", null);
		this.getWaves().forEach((id, wave) -> {
			String path2 = "Waves." + id + ".";
			wave.getMobs().forEach((mobId, mob) -> {
				String path3 = path2 + "Mobs." + mobId + ".";
				config.set(path3 + "Amount", mob.getAmount());
				config.set(path3 + "Level", mob.getLevel());
				config.set(path3 + "Chance", mob.getChance());
			});

			wave.getAmplificators().forEach((ampId, amp) -> {
				String path3 = path2 + "Amplificators." + ampId + ".";
				amp.getTriggers().forEach(trigger -> {
					if (!(trigger instanceof AbstractArenaGameEventTrigger<?> trigger1)) return;
					trigger1.saveTo(config, path3 + "Triggers.");
				});

				config.set(path3 + "Values.Amount", amp.getValueAmount());
				config.set(path3 + "Values.Level", amp.getValueLevel());
			});
		});
	}

    @Override
	@NotNull
	public EditorWaveManager getEditor() {
		if (this.editor == null) {
			this.editor = new EditorWaveManager(this);
		}
		return this.editor;
	}

	@Override
	@NotNull
    public ArenaConfig getArenaConfig() {
		return this.arenaConfig;
	}

	@Override
	@NotNull
	public JYML getConfig() {
		return this.config;
	}

	@Override
	@NotNull
	public List<String> getProblems() {
		List<String> list = new ArrayList<>();
		if (this.getWaves().isEmpty()) {
			list.add("No waves defined!");
		}
		
		return list;
	}

	@Override
	@NotNull
	public Map<String, IArenaWave> getWaves() {
		return this.waves;
	}

	@Override
	public int getFinalWave() {
		return this.finalWave;
	}

	@Override
	public void setFinalWave(int finalWave) {
		this.finalWave = finalWave;
	}


	@Override
	public int getDelayDefault() {
		return delayDefault;
	}

	@Override
	public void setDelayDefault(int delayDefault) {
		this.delayDefault = delayDefault;
	}

	@Override
	public int getDelayFirst() {
		return delayFirst;
	}

	@Override
	public void setDelayFirst(int delayFirst) {
		this.delayFirst = delayFirst;
	}


	@Override
	public boolean isGradualSpawnEnabled() {
		return this.gradualSpawnEnabled;
	}
    
    @Override
	public void setGradualSpawnEnabled(boolean gradualSpawnEnabled) {
		this.gradualSpawnEnabled = gradualSpawnEnabled;
	}
    
    @Override
	public double getGradualSpawnPercentFirst() {
		return this.gradualSpawnPercentFirst;
	}
    
    @Override
	public void setGradualSpawnPercentFirst(double gradualSpawnPercentFirst) {
		this.gradualSpawnPercentFirst = gradualSpawnPercentFirst;
	}

	@Override
	public int getGradualSpawnNextInterval() {
		return gradualSpawnNextInterval;
	}

	@Override
	public void setGradualSpawnNextInterval(int gradualSpawnNextInterval) {
		this.gradualSpawnNextInterval = gradualSpawnNextInterval;
	}

	@Override
	public double getGradualSpawnNextPercent() {
		return this.gradualSpawnNextPercent;
	}
    
    @Override
	public void setGradualSpawnNextPercent(double gradualSpawnNextPercent) {
		this.gradualSpawnNextPercent = Math.min(gradualSpawnNextPercent, this.getGradualSpawnPercentFirst());
	}

	@Override
	public double getGradualSpawnNextKillPercent() {
		return gradualSpawnNextKillPercent;
	}

	@Override
	public void setGradualSpawnNextKillPercent(double gradualSpawnNextKillPercent) {
		this.gradualSpawnNextKillPercent = Math.min(gradualSpawnNextKillPercent, this.getGradualSpawnNextPercent());
	}
}
