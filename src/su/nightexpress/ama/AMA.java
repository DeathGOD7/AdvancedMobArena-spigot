package su.nightexpress.ama;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.NexDataPlugin;
import su.nexmedia.engine.api.editor.EditorHolder;
import su.nexmedia.engine.commands.api.IGeneralCommand;
import su.nexmedia.engine.core.Version;
import su.nexmedia.engine.data.IDataHandler;
import su.nexmedia.engine.hooks.Hooks;
import su.nightexpress.ama.arena.ArenaManager;
import su.nightexpress.ama.arena.setup.ArenaSetupManager;
import su.nightexpress.ama.commands.*;
import su.nightexpress.ama.config.Config;
import su.nightexpress.ama.config.Lang;
import su.nightexpress.ama.data.ArenaUser;
import su.nightexpress.ama.data.ArenaUserData;
import su.nightexpress.ama.data.UserManager;
import su.nightexpress.ama.editor.ArenaEditorHandler;
import su.nightexpress.ama.editor.ArenaEditorHub;
import su.nightexpress.ama.editor.ArenaEditorType;
import su.nightexpress.ama.hooks.EHook;
import su.nightexpress.ama.hooks.external.*;
import su.nightexpress.ama.hooks.external.pets.MyPetHook;
import su.nightexpress.ama.economy.EconomyManager;
import su.nightexpress.ama.economy.IEconomy;
import su.nightexpress.ama.kits.KitManager;
import su.nightexpress.ama.stats.StatsManager;
import su.nightexpress.ama.mobs.MobManager;
import su.nightexpress.ama.nms.PMS;
import su.nightexpress.ama.nms.v1_15_1.V1_15_R1;
import su.nightexpress.ama.nms.v1_16_3.V1_16_R3;
import su.nightexpress.ama.nms.v1_17_R1.V1_17_R1;

import java.sql.SQLException;

public class AMA extends NexDataPlugin<AMA, ArenaUser> implements EditorHolder<AMA, ArenaEditorType> {
	
	private static AMA                instance;
	private        ArenaUserData      userData;
	private        ArenaEditorHub     editorHub;
	private        ArenaEditorHandler arenaEditorHandler;
	
	private Config config;
	private Lang lang;
	
	private EconomyManager economyManager;
	private ArenaManager arenaManager;
	private ArenaSetupManager arenaSetupManager;
	private MobManager mobManager;
	private KitManager kitManager;
	private StatsManager statsManager;

	private HologramsHK holograms;
	private PMS pms;
	
	public AMA() {
		instance = this;
	}
	
	@NotNull
	public static AMA getInstance() {
		return instance;
	}
	
	@Override
	public void enable() {
		switch (Version.CURRENT) { // TODO
			case V1_15_R1: {
				this.pms = new V1_15_R1();
				break;
			}
			case V1_16_R3: {
				this.pms = new V1_16_R3();
				break;
			}
			case V1_17_R1: {
			    this.pms = new V1_17_R1();
			    break;
			}
			default: {
				break;
			}
		}
		if (this.pms == null) {
			this.error("Could not setup NMS interface! (Unsupported version)");
			this.getPluginManager().disablePlugin(this);
			return;
		}
		this.pms.load();
		
		this.economyManager = new EconomyManager(this);
		this.economyManager.setup();
		
		this.mobManager = new MobManager(this);
		this.mobManager.setup();
		
		this.kitManager = new KitManager(this);
		this.kitManager.setup();
		
		this.arenaManager = new ArenaManager(this);
		this.arenaManager.setup();

		this.arenaSetupManager = new ArenaSetupManager(this);
		this.arenaSetupManager.setup();
		
		this.statsManager = new StatsManager(this);
		this.statsManager.setup();

		this.arenaEditorHandler = new ArenaEditorHandler(this);
		this.arenaEditorHandler.setup();
	}

	@Override
	public void disable() {
		if (this.editorHub != null) {
			this.editorHub.clear();
			this.editorHub = null;
		}
		if (this.arenaEditorHandler != null) {
			this.arenaEditorHandler.shutdown();
			this.arenaEditorHandler = null;
		}
		if (this.arenaSetupManager != null) {
			this.arenaSetupManager.shutdown();
			this.arenaSetupManager = null;
		}
		if (this.arenaManager != null) {
			this.arenaManager.shutdown();
			this.arenaManager = null;
		}
		if (this.mobManager != null) {
			this.mobManager.shutdown();
			this.mobManager = null;
		}
		if (this.kitManager != null) {
			this.kitManager.shutdown();
			this.kitManager = null;
		}
		if (this.statsManager != null) {
			this.statsManager.shutdown();
			this.statsManager = null;
		}
		if (this.economyManager != null) {
			this.economyManager.shutdown();
			this.economyManager = null;
		}
	}

	@Override
	protected boolean setupDataHandlers() {
		try {
			this.userData = ArenaUserData.getInstance();
			this.userData.setup();
		} 
		catch (SQLException e) {
			this.error("Could not setup database handler!");
			e.printStackTrace();
			return false;
		}
		
		this.userManager = new UserManager(this);
		this.userManager.setup();
		
		return true;
	}

	@Override
	public void setConfig() {
		this.config = new Config(this);
		this.config.setup();
		
		this.lang = new Lang(this);
		this.lang.setup();
		
		// FIXME Quick fix for null lang argument for ILangMsg for streaks.
		this.config.setupStreaks();
	}
	
	@Override
	@NotNull
	public Config cfg() {
		return this.config;
	}

	@Override
	@NotNull
	public Lang lang() {
		return this.lang;
	}

	@Override
	@NotNull
	public IDataHandler<AMA, ArenaUser> getData() {
		return this.userData;
	}

	@Override
	public void registerHooks() {
		this.registerHook(Hooks.CITIZENS, CitizensHK.class);
		this.registerHook(EHook.ESSENTIALS, EssentialsHK.class);
		this.registerHook(EHook.MCMMO, McmmoHK.class);
		this.registerHook(Hooks.PLACEHOLDER_API, PlaceholderHook.class);
		this.registerHook(EHook.SUNLIGHT, SunLightHK.class);
		this.holograms = this.registerHook(EHook.HOLOGRAPHIC_DISPLAYS, HologramsHK.class);
		this.registerHook(EHook.MAGIC, MagicHK.class);
		
		// TODO this.registerHook(CombatPetsHook.class);
		this.registerHook(EHook.MYPET, MyPetHook.class);
	}

	@Override
	public void registerCmds(@NotNull IGeneralCommand<AMA> mainCommand) {
		if (!Config.GEN_VAULT_CURRENCY) {
			mainCommand.addSubCommand(new BalanceCmd(this));
			mainCommand.addSubCommand(new CoinsCmd(this));
		}
		mainCommand.addSubCommand(new ForceEndCmd(this));
		mainCommand.addSubCommand(new ForceStartCmd(this));
		mainCommand.addSubCommand(new JoinCmd(this));
		mainCommand.addSubCommand(new LeaveCmd(this));
		mainCommand.addSubCommand(new ListCmd(this));
		mainCommand.addSubCommand(new RegionCommand(this));
		mainCommand.addSubCommand(new ScoreCmd(this));
		mainCommand.addSubCommand(new ShopCmd(this));
		mainCommand.addSubCommand(new SkipwaveCmd(this));
		mainCommand.addSubCommand(new SpectateCmd(this));
		mainCommand.addSubCommand(new SpotCmd(this));
	}

	@Override
	@NotNull
	public ArenaEditorHandler getEditorHandlerNew() {
		return this.arenaEditorHandler;
	}

	@Override
	@NotNull
	public ArenaEditorHub getEditor() {
		if (this.editorHub == null) {
			this.editorHub = new ArenaEditorHub(this);
		}
		return this.editorHub;
	}

	@NotNull
	public ArenaEditorHub getEditorHub() {
		return editorHub;
	}

	@NotNull
	public EconomyManager getEconomyManager() {
		return this.economyManager;
	}
	
	@NotNull
	public IEconomy getEconomy() {
		return this.getEconomyManager().getEconomy();
	}
	
	@NotNull
	public ArenaManager getArenaManager() {
		return this.arenaManager;
	}

	@NotNull
	public ArenaSetupManager getArenaSetupManager() {
		return arenaSetupManager;
	}

	@NotNull
	public MobManager getMobManager() {
		return this.mobManager;
	}
	
	@NotNull
	public KitManager getKitManager() {
		return this.kitManager;
	}
	
	@NotNull
	public StatsManager getStatsManager() {
		return this.statsManager;
	}

	@Nullable
	public HologramsHK getHolograms() {
		return this.holograms;
	}

	@NotNull
	public PMS getPMS() {
		return this.pms;
	}
}
