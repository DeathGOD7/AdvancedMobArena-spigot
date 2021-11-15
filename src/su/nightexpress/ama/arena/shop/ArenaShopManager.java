package su.nightexpress.ama.arena.shop;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.utils.ItemUT;
import su.nightexpress.ama.api.arena.IArena;
import su.nightexpress.ama.api.arena.config.IArenaConfig;
import su.nightexpress.ama.api.arena.game.IArenaGameEventTrigger;
import su.nightexpress.ama.api.arena.shop.IArenaShopManager;
import su.nightexpress.ama.api.arena.shop.IArenaShopProduct;
import su.nightexpress.ama.api.arena.type.ArenaLockState;
import su.nightexpress.ama.api.arena.type.ArenaState;
import su.nightexpress.ama.arena.ArenaPlayer;
import su.nightexpress.ama.arena.config.ArenaConfig;
import su.nightexpress.ama.arena.editor.shop.EditorShopManager;
import su.nightexpress.ama.arena.game.trigger.AbstractArenaGameEventTrigger;

import java.io.File;
import java.util.*;

public class ArenaShopManager implements IArenaShopManager {

	private static final String CONFIG_NAME = "welcome.yml";
	private static final String CONFIG_MENU_NAME = "gui.yml";

	private final ArenaConfig arenaConfig;
	private final JYML        config;

	private       boolean isHideOtherKitProducts;
	private       boolean isLockedWhileMobsAlive;
	
	private Set<IArenaShopProduct> products;
	private ArenaShopMenu shopMenu;
	private EditorShopManager editor;

	public ArenaShopManager(@NotNull ArenaConfig arenaConfig) {
		this.arenaConfig = arenaConfig;
		this.config = new JYML(arenaConfig.getFile().getParentFile().getAbsolutePath() + "/shop/", CONFIG_NAME);
	}

	@Override
	public void setup() {
		this.setHideOtherKitProducts(config.getBoolean("Settings.Hide_Other_Kit_Products"));
		this.setLockedWhileMobsAlive(config.getBoolean("Settings.Lock_While_Mobs_Alive"));
		
		this.products = new LinkedHashSet<>();
		for (String sId : config.getSection("Products")) {
			String path2 = "Products." + sId + ".";
			
			double price = config.getDouble(path2 + "Price");
			Map<ArenaLockState, Set<IArenaGameEventTrigger>> triggers = new HashMap<>();
			for (ArenaLockState lockState : ArenaLockState.values()) {
				triggers.put(lockState, AbstractArenaGameEventTrigger.parse(arenaConfig, config, path2 + "State." + lockState.name() + ".Triggers"));
			}
			
			Set<String> kitRequirements = config.getStringSet(path2 + "Allowed_Kits");
			ItemStack preview = config.getItem(path2 + "Preview");
			if (ItemUT.isAir(preview)) {
				plugin().error("Null preview for '" + sId + "' item in '" + arenaConfig.getId() + "' arena shop!");
				//continue;
			}
			
			List<String> commands = config.getStringList(path2 + "Commands");
			List<ItemStack> items = Arrays.asList(config.getItemList64(path2 + "Items"));
			
			ArenaShopProduct product = new ArenaShopProduct(
					this, sId, price, triggers, kitRequirements, preview, commands, items
					);
			this.products.add(product);
		}
	}
	
	@Override
	public void shutdown() {
		this.products.forEach(IArenaShopProduct::clear);
		this.products.clear();
		if (this.shopMenu != null) {
			this.shopMenu.clear();
			this.shopMenu = null;
		}
		if (this.editor != null) {
			this.editor.clear();
			this.editor = null;
		}
	}
	
	@Override
	public void onSave() {
		config.set("Settings.Hide_Other_Kit_Products", this.isHideOtherKitProducts());
		config.set("Settings.Lock_While_Mobs_Alive", this.isLockedWhileMobsAlive());

		config.set("Products", null);
		this.products.forEach(shopItem -> {
			String path = "Products." + shopItem.getId() + ".";

			shopItem.getStateTriggers().forEach((lockState, triggers) -> {
				String path2 = path + "State." + lockState.name() + ".Triggers.";
				triggers.forEach(trigger -> {
					if (!(trigger instanceof  AbstractArenaGameEventTrigger<?> trigger1)) return;
					trigger1.saveTo(config, path2);
				});
			});

			config.set(path + "Price", shopItem.getPrice());
			config.set(path + "Allowed_Kits", shopItem.getApplicableKits());
			config.setItem(path + "Preview", shopItem.getPreview());
			config.set(path + "Commands", shopItem.getCommands());
			config.setItemList64(path + "Items", shopItem.getItems());
		});
	}

	@Override
	@NotNull
	public IArenaConfig getArenaConfig() {
		return this.arenaConfig;
	}

	@NotNull
	@Override
	public JYML getConfig() {
		return config;
	}

	@Override
	@NotNull
	public List<String> getProblems() {
		return new ArrayList<>();
	}

	@Override
	@NotNull
	public EditorShopManager getEditor() {
		if (this.editor == null) {
			this.editor = new EditorShopManager(this);
		}
		return this.editor;
	}

	@NotNull
	@Override
	public ArenaShopMenu getShopMenu() {
		if (this.shopMenu == null) {
			File file = this.getFile().getParentFile();
			plugin().getConfigManager().extractResource(file.getPath(), "arenas/shop", CONFIG_MENU_NAME, false);

			JYML configMenu = new JYML(this.getFile().getParentFile().getAbsolutePath(), CONFIG_MENU_NAME);
			this.shopMenu = new ArenaShopMenu(this, configMenu);
		}
		return shopMenu;
	}

	@Override
	public boolean isHideOtherKitProducts() {
		return this.isHideOtherKitProducts;
	}
	
	@Override
	public void setHideOtherKitProducts(boolean hideOtherKitProducts) {
		this.isHideOtherKitProducts = hideOtherKitProducts;
	}
	
	@Override
	public boolean isLockedWhileMobsAlive() {
		return this.isLockedWhileMobsAlive;
	}
	
	@Override
	public void setLockedWhileMobsAlive(boolean lockedWhileMobsAlive) {
		this.isLockedWhileMobsAlive = lockedWhileMobsAlive;
	}
	
	@Override
	@NotNull
	public Set<IArenaShopProduct> getProducts() {
		return this.products;
	}

	@Override
	public void setProducts(@NotNull Set<IArenaShopProduct> products) {
		this.products = products;
	}

	public boolean open(@NotNull Player player) {
		ArenaPlayer arenaPlayer = plugin().getArenaManager().getArenaPlayer(player);
		if (arenaPlayer == null) {
			return false;
		}
		
		IArena arena = arenaPlayer.getArena();
		if (arena.getState() != ArenaState.INGAME || !arena.getConfig().getGameplayManager().isShopEnabled()) {
			return false;
		}
		
		if (this.isLockedWhileMobsAlive() && !arenaPlayer.getArena().isNextWaveAllowed()) {
			plugin().lang().Shop_Open_Error_InWave.send(player);
			return false;
		}
		
		this.getShopMenu().open(player, 1);
		return true;
	}
}
