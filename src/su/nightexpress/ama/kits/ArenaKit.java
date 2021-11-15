package su.nightexpress.ama.kits;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.AbstractLoadableItem;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.manager.api.gui.JIcon;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.Perms;
import su.nightexpress.ama.api.arena.IArena;
import su.nightexpress.ama.api.kits.IArenaKit;
import su.nightexpress.ama.arena.ArenaPlayer;
import su.nightexpress.ama.data.ArenaUser;
import su.nightexpress.ama.kits.editor.EditorKitMain;
import su.nightexpress.ama.kits.menu.KitPreviewMenu;

import java.util.*;

public class ArenaKit extends AbstractLoadableItem<AMA> implements IArenaKit {
	
	private boolean isDefault;
    private String name;
    private ItemStack icon;
    
    private int     cost;
    private boolean isPermissionRequired;
    
    private List<String>      commands;
    private Set<PotionEffect> potionEffects;
    private ItemStack[] armor;
    private ItemStack[] items;
    
    private KitPreviewMenu preview;
    private EditorKitMain  editor;
    
    public ArenaKit(@NotNull AMA plugin, @NotNull String path) {
    	super(plugin, path);
    	
		this.setName("&a" + getId() + "Kit");
		this.setDefault(false);
		JIcon item = new JIcon(Material.GOLDEN_CHESTPLATE);
		item.setName("&eSample Kit");
		item.addLore("&7Edit me in &e/ama editor");
		this.setIcon(item.build());
		
		this.setCost(100);
		this.setPermissionRequired(true);
		
		this.setCommands(new ArrayList<>());
		this.setPotionEffects(new HashSet<>());
		this.setArmor(new ItemStack[4]);
		
		ItemStack[] inv = new ItemStack[36];
		inv[0] = new ItemStack(Material.GOLDEN_SWORD);
		inv[1] = new ItemStack(Material.COOKED_BEEF, 16);
		inv[2] = new ItemStack(Material.GOLDEN_APPLE, 4);
		this.setItems(inv);
    }
    
    public ArenaKit(@NotNull AMA plugin, @NotNull JYML cfg) {
    	super(plugin, cfg);

    	this.setDefault(cfg.getBoolean("Default"));
		this.setName(cfg.getString("Name", this.getId()));
		this.setCost(cfg.getInt("Cost"));
		this.setPermissionRequired(cfg.getBoolean("Permission_Required"));

		ItemStack icon = cfg.getItem("Icon");
		if (ItemUT.isAir(icon)) icon = new ItemStack(Material.DIAMOND_SWORD);
		this.setIcon(icon);

		this.setCommands(cfg.getStringList("Content.Commands"));
		this.setPotionEffects(new HashSet<>());
		for (String sId : cfg.getSection("Content.Potion_Effects")) {
			PotionEffectType pet = PotionEffectType.getByName(sId.toUpperCase());
			if (pet == null) {
				plugin.error("Invalid potion effect '" + sId + "' in '" + getId() + "' kit!");
				continue;
			}
			int level = cfg.getInt("Content.Potion_Effects." + sId);
			if (level == 0) continue;
			
			PotionEffect effect = new PotionEffect(pet, Integer.MAX_VALUE, level - 1);
			this.getPotionEffects().add(effect);
		}
		
		this.setArmor(cfg.getItemList64("Content.Armor"));
		this.setItems(cfg.getItemList64("Content.Inventory"));
    }
    
    @Override
	public void onSave() {
    	cfg.set("Name", getName());
    	cfg.set("Default", this.isDefault());
		cfg.set("Cost", this.getCost());
		cfg.set("Permission_Required", this.isPermissionRequired());
    	cfg.setItem("Icon", this.getIcon());
    	cfg.set("Content.Commands", getCommands());
    	cfg.set("Content.Potion_Effects", null);
    	this.getPotionEffects().forEach(potion -> {
    		cfg.set("Content.Potion_Effects." + potion.getType().getName(), potion.getAmplifier() + 1);
    	});
    	cfg.setItemList64("Content.Armor", Arrays.asList(this.getArmor()));
    	cfg.setItemList64("Content.Inventory", Arrays.asList(this.getItems()));
    }
    
    @Override
    public void clear() {
    	if (this.preview != null) {
    		this.preview.clear();
    		this.preview = null;
    	}
    	if (this.editor != null) {
    		this.editor.clear();
    		this.editor = null;
    	}
    }
    
    @Override
	@NotNull
	public EditorKitMain getEditor() {
		if (this.editor == null) {
			this.editor = new EditorKitMain(this);
		}
		return this.editor;
	}

	@NotNull
	@Override
	public KitPreviewMenu getPreview() {
		if (this.preview == null) {
			this.preview = new KitPreviewMenu(this);
		}
		return preview;
	}

	@Override
	public boolean isDefault() {
    	return this.isDefault;
    }
    
    @Override
	public void setDefault(boolean isDefault) {
    	this.isDefault = isDefault;
    }
    
    @Override
	@NotNull
    public String getName() {
        return this.name;
    }
    
    @Override
	public void setName(@NotNull String name) {
        this.name = StringUT.color(name);
    }
    
    @Override
	@NotNull
    public ItemStack getIcon() {
        return new ItemStack(this.icon);
    }
    
    @Override
	public void setIcon(@NotNull ItemStack icon) {
        this.icon = new ItemStack(icon);
    }

	@Override
	public int getCost() {
		return cost;
	}

	@Override
	public void setCost(int cost) {
		this.cost = cost;
	}

	@Override
	public boolean isPermissionRequired() {
		return isPermissionRequired;
	}

	@Override
	public void setPermissionRequired(boolean permissionRequired) {
		isPermissionRequired = permissionRequired;
	}
	
	@Override
	@NotNull
    public List<String> getCommands() {
    	return this.commands;
    }
    
    @Override
	public void setCommands(@NotNull List<String> commands) {
    	this.commands = commands;
    }
    
    @Override
	@NotNull
    public Set<PotionEffect> getPotionEffects() {
    	return this.potionEffects;
    }
    
    @Override
	public void setPotionEffects(@NotNull Set<PotionEffect> potionEffects) {
    	this.potionEffects = potionEffects;
    }
    
    @Override
	@NotNull
	public ItemStack[] getArmor() {
    	return this.armor;
    }
    
    @Override
	public void setArmor(@NotNull ItemStack[] armor) {
    	this.armor = Arrays.copyOf(armor, 4);
    }

    @Override
	@NotNull
    public ItemStack[] getItems() {
    	return this.items;
    }
    
    @Override
	public void setItems(@NotNull ItemStack[] items) {
    	this.items = Arrays.copyOf(items, 27);
    }
	
	public boolean buy(@NotNull ArenaPlayer arenaPlayer) {
		Player player = arenaPlayer.getPlayer();
		if (!this.hasPermission(player)) {
			plugin.lang().Kit_Buy_Error_NoPermission.send(player);
			return false;
		}

		ArenaUser user = plugin.getUserManager().getOrLoadUser(player);
		
		// Check if player already have this kit
		// Only for permanent kits
		boolean accountKits = plugin.getKitManager().isSavePurchasedKits();
		if (accountKits && user != null && user.hasKit(this)) {
			arenaPlayer.setKit(this);
			return true;
		}

		double balance = plugin.getEconomy().getBalance(player);
		int cost = player.hasPermission(Perms.BYPASS_KIT_COST) ? 0 : this.getCost();
		
		if (cost > 0) {
			if (balance < cost) {
				plugin.lang().Kit_Buy_Error_NoMoney.replace(this.replacePlaceholders()).send(player);
				player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_HURT, 1.0f, 1.0f);
				return false;
			}
			plugin.getEconomy().take(player, cost);
		}

        plugin.lang().Kit_Buy_Success.replace(this.replacePlaceholders()).send(player);
		player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
		
		if (accountKits && user != null) {
			user.addKit(this);
		}
		else {
			arenaPlayer.setKit(this);
		}
		return true;
	}
	
	public boolean isAvailable(@NotNull ArenaPlayer arenaPlayer, boolean isMsg) {
        IArena arena = arenaPlayer.getArena();
        Player player = arenaPlayer.getPlayer();
        
        // Check if kits are disabled on the arena
        if (!arena.getConfig().getGameplayManager().isKitsEnabled()) {
            if (isMsg) plugin.lang().Arena_Game_Restrict_Kits.send(player);
            return false;
        }
        
        // Check if kit is banned on the arena
        if (!arena.getConfig().getGameplayManager().isKitAllowed(this.getId())) {
        	if (isMsg) plugin.lang().Kit_Select_Error_Disabled.send(player);
        	return false;
        }
        
        // Check if player has permission.
        if (!this.hasPermission(player)) {
        	if (isMsg) plugin.lang().Kit_Select_Error_NoPermission.send(player);
        	return false;
        }
        
        // Check for limit
        int limitMax = arena.getConfig().getGameplayManager().getKitLimit(this.getId());
        if (limitMax >= 0) {
			int limitHas = (int) arena.getPlayers().stream()
					.filter(arenaPlayer1 -> arenaPlayer1.getKit() != null && arenaPlayer1.getKit().equals(this)).count();
			if (limitHas >= limitMax) {
				if (isMsg) plugin.lang().Kit_Select_Error_Limit.send(player);
				return false;
			}
		}
        
        return true;
	}
}
