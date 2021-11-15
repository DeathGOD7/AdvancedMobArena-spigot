package su.nightexpress.ama.kits;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.manager.IListener;
import su.nexmedia.engine.utils.DataUT;
import su.nexmedia.engine.utils.LocUT;
import su.nexmedia.engine.utils.StringUT;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.Perms;
import su.nightexpress.ama.api.arena.event.ArenaPlayerJoinEvent;
import su.nightexpress.ama.api.arena.type.ArenaState;
import su.nightexpress.ama.api.kits.IArenaKit;
import su.nightexpress.ama.arena.ArenaPlayer;
import su.nightexpress.ama.data.ArenaUser;
import su.nightexpress.ama.kits.menu.KitSelectMenu;
import su.nightexpress.ama.kits.menu.KitShopMenu;

import java.util.*;

public class KitManager extends IListener<AMA> {
	
	private JYML cfg;
	private JYML configPreview;

	private boolean isSavePurchasedKits;
	private List<String> signFormat;

	private Map<String, IArenaKit> kits;
	@Deprecated
	private Set<Sign> kitSigns;
	
	private KitSelectMenu selectMenu;
	private KitShopMenu   shopMenu;
	
	public static final NamespacedKey KEY_SIGN_KIT = new NamespacedKey(AMA.getInstance(), "kits_sign_kit");
	
	public KitManager(@NotNull AMA plugin) {
		super(plugin);
	}
	
	public void setup() {
		this.plugin.getConfigManager().extract("/kits/kits/");
		this.cfg = JYML.loadOrExtract(plugin, "/kits/settings.yml");
		this.configPreview = JYML.loadOrExtract(plugin, "/kits/gui.preview.yml");
		
		this.isSavePurchasedKits = cfg.getBoolean("General.Save_Purchased_Kits");
		this.signFormat = StringUT.color(cfg.getStringList("Sign_Format"));
		
		this.kits = new HashMap<>();
		for (JYML cfg2 : JYML.loadAll(plugin.getDataFolder() + "/kits/kits/", false)) {
			ArenaKit kit = new ArenaKit(plugin, cfg2);
			this.kits.put(kit.getId(), kit);
		}
		this.plugin.info("Kits Loaded: " + kits.size());
		
		this.selectMenu = new KitSelectMenu(plugin, JYML.loadOrExtract(plugin, "/kits/gui.selector.yml"));
		this.shopMenu = new KitShopMenu(this.plugin, JYML.loadOrExtract(plugin, "/kits/gui.shop.yml"));
		
		this.kitSigns = new HashSet<>();
		for (String sLoc : this.cfg.getStringList("Signs")) {
			Location loc = LocUT.deserialize(sLoc);
			if (loc == null) continue;
			
			Block block = loc.getBlock();
			BlockState state = block.getState();
			if (!(state instanceof Sign)) continue;
			
			this.kitSigns.add((Sign) state);
		}
		this.updateSigns();
		this.registerListeners();
	}

	public void shutdown() {
		this.unregisterListeners();
		
		if (this.kits != null) {
			this.getKits().forEach(IArenaKit::clear);
			this.kits.clear();
			this.kits = null;
		}
		
		if (this.kitSigns != null) {
			this.cfg.set("Signs", null);
			List<String> list = new ArrayList<>();
			this.kitSigns.forEach(sign -> list.add(LocUT.serialize(sign.getLocation())));
			this.cfg.set("Signs", list);
			
			this.kitSigns.clear();
			this.kitSigns = null;
			this.cfg.saveChanges();
		}
		
		if (this.selectMenu != null) {
			this.selectMenu.clear();
			this.selectMenu = null;
		}
		if (this.shopMenu != null) {
			this.shopMenu.clear();
			this.shopMenu = null;
		}
	}

	@NotNull
	public JYML getConfigPreview() {
		return configPreview;
	}

	@NotNull
	public KitSelectMenu getSelectMenu() {
		return selectMenu;
	}

	@NotNull
	public KitShopMenu getShopMenu() {
		return shopMenu;
	}
    
    public boolean isSavePurchasedKits() {
    	return this.isSavePurchasedKits;
    }
	
	public boolean isKitExists(@NotNull String id) {
		return this.getKitById(id) != null;
	}

	@NotNull
	public Map<String, IArenaKit> getKitsMap() {
		return this.kits;
	}
	
	@NotNull
    public Collection<IArenaKit> getKits() {
    	return this.getKitsMap().values();
    }
	
	@NotNull
	public List<String> getKitIds() {
		return new ArrayList<>(this.kits.keySet());
	}
    
	@Nullable
    public IArenaKit getKitById(@NotNull String id) {
    	return this.kits.get(id.toLowerCase());
    }
    
    @Nullable
    public IArenaKit getDefaultKit() {
    	return Rnd.get(this.getKits().stream().filter(IArenaKit::isDefault).toList());
    }

	private void addSign(@NotNull Sign sign, @NotNull IArenaKit kit) {
		DataUT.setData(sign, KEY_SIGN_KIT, kit.getId());
		
		this.kitSigns.add(sign);
		this.plugin.getServer().getScheduler().runTask(plugin, this::updateSigns);
	}

	@Deprecated
	private void updateSigns() {
		this.kitSigns.forEach(sign -> {
			String kitId = DataUT.getStringData(sign, KEY_SIGN_KIT);
			IArenaKit kit = kitId != null ? this.getKitById(kitId) : null;
			if (kit == null) return;
			
	    	List<String> text = new ArrayList<>(this.signFormat);
	    	text.replaceAll(kit.replacePlaceholders());
	    	
	    	for (int line = 0; line < 4; line++) {
	    		sign.setLine(line, line >= text.size() ? "" : text.get(line));
	    	}
	    	sign.update(true);
		});
    }
	
    // ---------------------------------------------------------------------
    // E V E N T S
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onArenaJoin(ArenaPlayerJoinEvent e) {
		if (!this.isSavePurchasedKits()) return;
		
		Player player = e.getArenaPlayer().getPlayer();
		
		ArenaUser user = plugin.getUserManager().getOrLoadUser(player);
		if (user == null) return;
		
		this.plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
			this.getKits().stream().filter(kit -> {
				if (user.hasKit(kit) || !kit.hasPermission(player)) return false;
				return kit.getCost() <= 0;
			}).forEach(user::addKit);
			
			if (plugin.cfg().dataSaveInstant) {
				plugin.getUserManager().save(user);
			}
		});
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onSignChange(SignChangeEvent e) {
		if (!e.getPlayer().hasPermission(Perms.ADMIN)) return;
		
		Block block = e.getBlock();
		BlockState state = block.getState();
		if (!(state instanceof Sign sign)) return;
		
		String line1 = e.getLine(0);
		if (line1 == null || !line1.equalsIgnoreCase("AMA")) return;
		
		String line2 = e.getLine(1);
		if (line2 == null || !line2.equalsIgnoreCase("KIT")) return;
		
		String line3 = e.getLine(2);
		IArenaKit kit = line3 == null ? null : this.getKitById(line3);
		if (kit == null) return;

		this.addSign(sign, kit);
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onSignClickKit(PlayerInteractEvent e) {
		Block block = e.getClickedBlock();
		if (block == null || !(block.getState() instanceof Sign sign)) return;
		if (e.getHand() == EquipmentSlot.OFF_HAND) return;
		
		Player player = e.getPlayer();
		ArenaPlayer arenaPlayer = plugin.getArenaManager().getArenaPlayer(player);
		if (arenaPlayer == null) return;
		if (arenaPlayer.getArena().getState() == ArenaState.INGAME) return;

		String kitId = DataUT.getStringData(sign, KEY_SIGN_KIT);

		IArenaKit kit = kitId != null ? this.getKitById(kitId) : null;
		if (kit == null) return;

		e.setUseInteractedBlock(Result.DENY);
		
		if (kit.isAvailable(arenaPlayer, true)) {
			if (kit.buy(arenaPlayer)) {
				plugin.lang().Kit_Select_Success.replace(kit.replacePlaceholders()).send(player);
				player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 1.0f, 1.0f);
			}
		}
	}
}
