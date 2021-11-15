package su.nightexpress.ama.mobs;

import org.bukkit.DyeColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.manager.LoadableItem;
import su.nexmedia.engine.utils.CollectionsUT;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.ama.AMA;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ArenaCustomMob extends LoadableItem {

    private String name;
    private boolean visible;
    private EntityType type;
    
    private int lvlMin;
    private int lvlMax;
    
    private boolean     isBaby;
    private Horse.Color horseColor;
    private Horse.Style horseStyle;
    private boolean creeperCharged;
    private int slimeSize;
    private Parrot.Variant parrotVariant;
    private Llama.Color llamaColor;
    private DyeColor sheepColor;
    private Rabbit.Type rabbitType;
    private Cat.Type catType;
    private MushroomCow.Variant mushroomVariant;
    private boolean wolfAngry;
    private Villager.Profession villagerProfession;
    
    private ArenaMobHealthBar bossBar;
    private       ItemStack[]              equip;
    private final Map<Attribute, double[]> attributes; // [0] Base, [1] Per Level
    
    //protected EditorMobMob editor;
    
    // Creating new config
    public ArenaCustomMob(@NotNull AMA plugin, @NotNull String path) {
    	super(plugin, path);
    	
    	this.setName("&aNew Custom Mob");
		this.setNameVisible(true);
		this.setEntityType(EntityType.ZOMBIE);
		
		this.lvlMin = 1;
		this.lvlMax = 10;
		
		this.isBaby = false;
		this.creeperCharged = false;
		this.wolfAngry = false;
		this.slimeSize = 4;
		
		this.setEquipment(new ItemStack[4]);
		
		String barTitle = "&c&l%name% &7&l- &f&l%hp%&7/&f&l%maxhp%";
		this.bossBar = new ArenaMobHealthBar(barTitle, BarStyle.SOLID, BarColor.RED);
		
		this.attributes = new HashMap<>();
		this.attributes.put(Attribute.GENERIC_MAX_HEALTH, new double[] {20D, 1D});
    }
    
    public ArenaCustomMob(@NotNull AMA plugin, @NotNull JYML cfg) {
    	super(plugin, cfg);

		this.setName(cfg.getString("display-name", this.getId()));
		this.setNameVisible(cfg.getBoolean("name-visible"));
		EntityType type = CollectionsUT.getEnum(cfg.getString("entity-type", ""), EntityType.class);
		if (type == null) {
			throw new IllegalStateException("Invalid entity type for '" + getId() + "' mob!");
		}
		this.setEntityType(type);
		
		this.lvlMin = cfg.getInt("level.minimum", 1);
		this.lvlMax = cfg.getInt("level.maximum", 1);
		
		this.isBaby = cfg.getBoolean(path + "settings.baby");
		
		String path = "settings." + type.name().toLowerCase() + ".";
		switch (type) {
			case HORSE -> {
				this.horseColor = cfg.getEnum(path + "color", Horse.Color.class);
				this.horseStyle = cfg.getEnum(path + "style", Horse.Style.class);
			}
			case CREEPER -> this.creeperCharged = cfg.getBoolean(path + "charged");
			case SLIME -> this.slimeSize = cfg.getInt(path + "size", 4);
			case PARROT -> this.parrotVariant = cfg.getEnum(path + "variant", Parrot.Variant.class);
			case LLAMA -> this.llamaColor = cfg.getEnum(path + "color", Llama.Color.class);
			case SHEEP -> this.sheepColor = cfg.getEnum(path + "color", DyeColor.class);
			case RABBIT -> this.rabbitType = cfg.getEnum(path + "type", Rabbit.Type.class);
			case CAT -> this.catType = cfg.getEnum(path + "type", Cat.Type.class);
			case MUSHROOM_COW -> this.mushroomVariant = cfg.getEnum(path + "variant", MushroomCow.Variant.class);
			case VILLAGER -> this.villagerProfession = cfg.getEnum(path + "profession", Villager.Profession.class);
			case WOLF -> this.wolfAngry = cfg.getBoolean(path + "angry");
			default -> {}
		}
		
		this.setEquipment(cfg.getItemList64("equipment"));
		
		// Boss Bar
		if (cfg.getBoolean("boss-bar.enabled")) {
			String barTitle = cfg.getString("boss-bar.title", "&c&l%name% &7&l- &f&l%hp%&7&l/&f&l%maxhp%");
			BarColor barColor = cfg.getEnum("boss-bar.color", BarColor.class, BarColor.RED);
			BarStyle barStyle = cfg.getEnum("boss-bar.style", BarStyle.class, BarStyle.SOLID);
			this.bossBar = new ArenaMobHealthBar(barTitle, barStyle, barColor);
		}
		
		// Attributes
		this.attributes = new HashMap<>();
		for (Attribute att : Attribute.values()) {
			double value = cfg.getDouble("attributes.start." + att.name());
			double perLevel = cfg.getDouble("attributes.per-level." + att.name());
			if (value > 0 || perLevel > 0) {
				this.attributes.put(att, new double[] {value, perLevel});
			}
		}
    }
    
    @Override
    protected void save(@NotNull JYML cfg) {
		cfg.set("display-name", this.getName());
		cfg.set("name-visible", this.isNameVisible());
		cfg.set("entity-type", this.getEntityType().name());
		
		cfg.set("level.minimum", this.getLevelMin());
		cfg.set("level.maximum", this.getLevelMax());
		
		cfg.set("settings.baby", this.isBaby);
		cfg.set("settings.creeper.charged", this.creeperCharged);
		cfg.set("settings.wolf.angry", this.wolfAngry);
		cfg.set("settings.slime.size", this.slimeSize);
		
		String path = "settings." + this.getEntityType().name().toLowerCase() + ".";
		if (this.horseColor != null) cfg.set(path + "color", this.horseColor.name());
		if (this.horseStyle != null) cfg.set(path + "style", this.horseStyle.name());
		if (this.parrotVariant != null) cfg.set(path + "variant", this.parrotVariant.name());
		if (this.llamaColor != null) cfg.set(path + "color", this.llamaColor.name());
		if (this.sheepColor != null) cfg.set(path + "color", this.sheepColor.name());
		if (this.rabbitType != null) cfg.set(path + "type", this.rabbitType.name());
		if (this.catType != null) cfg.set(path + "type", this.catType.name());
		if (this.mushroomVariant != null) cfg.set(path + "variant", this.mushroomVariant.name());
		if (this.villagerProfession != null) cfg.set(path + "profession", this.villagerProfession.name());
		
		cfg.set("equipment", null);
		cfg.setItemList64("equipment", Arrays.asList(this.getEquipment()));
		
    	cfg.set("boss-bar.enabled", this.hasHealthBar());
    	ArenaMobHealthBar bar = this.getHealthBar();
    	if (bar != null) {
			cfg.set("boss-bar.title", bar.getTitle());
			cfg.set("boss-bar.style", bar.getStyle().name());
			cfg.set("boss-bar.color", bar.getColor().name());
    	}
		
		cfg.set("attributes", null);
		this.getAttributes().forEach((att, values) -> {
			String name = att.name();
			cfg.set("attributes.start." + name, values[0]);
			cfg.set("attributes.per-level", values[1]);
		});
    }
    
    @NotNull
    public String getName() {
    	return this.name;
    }
    
    public void setName(@NotNull String name) {
    	this.name = StringUT.color(name);
    }
    
    public boolean isNameVisible() {
    	return this.visible;
    }
    
    public void setNameVisible(boolean visible) {
    	this.visible = visible;
    }
    
    @NotNull
    public EntityType getEntityType() {
    	return this.type;
    }
    
    public void setEntityType(@NotNull EntityType type) {
    	this.type = type;
    }
    
    public int getLevelMin() {
    	return this.lvlMin;
    }
    
    public int getLevelMax() {
    	return this.lvlMax;
    }
    
    @NotNull
    public ItemStack[] getEquipment() {
    	return this.equip;
    }
    
    public void setEquipment(@NotNull ItemStack[] equip) {
    	this.equip = equip;
    }
    
    /**
     * @return Mob attribute values array, where [0] is base value, [1] is per level increase.
     */
    @NotNull
    public Map<Attribute, double[]> getAttributes() {
    	return this.attributes;
    }
    
    /*@NotNull
    public EditorMobMob getEditor() {
    	if (this.editor == null) {
    		this.editor = new EditorMobMob((AMA) plugin, this);
    	}
    	return this.editor;
    }*/
    
    public void clear() {
    	/*if (this.editor != null) {
    		this.editor.shutdown();
    		this.editor = null;
    	}*/
    }
    
	public boolean hasHealthBar() {
    	return this.bossBar != null;
    }
    
	@Nullable
    public ArenaMobHealthBar getHealthBar() {
    	return this.bossBar;
    }
	
	public void applySettings(@NotNull LivingEntity entity, int level) {
		entity.setCustomName(this.getName().replace("%level%", String.valueOf(level)));
		entity.setCustomNameVisible(this.isNameVisible());

		EntityEquipment armor = entity.getEquipment();
		if (armor != null) armor.setArmorContents(this.getEquipment());

		if (entity instanceof Ageable age) {
			if (this.isBaby) age.setBaby();
			else age.setAdult();
		}

		if (entity instanceof Horse horse) {
			if (this.horseColor != null && this.horseStyle != null) {
				horse.setStyle(this.horseStyle);
				horse.setColor(this.horseColor);
			}
		}
		else if (entity instanceof Creeper creeper) {
			creeper.setPowered(this.creeperCharged);
		}
		else if (entity instanceof Slime slime) {
			slime.setSize(this.slimeSize);
		}
		else if (entity instanceof Parrot parrot) {
			if (this.parrotVariant != null) {
				parrot.setVariant(this.parrotVariant);
			}
		}
		else if (entity instanceof Llama llama) {
			if (this.llamaColor != null) {
				llama.setColor(this.llamaColor);
			}
		}
		else if (entity instanceof Sheep sheep) {
			if (this.sheepColor != null) {
				sheep.setColor(this.sheepColor);
			}
		}
		else if (entity instanceof Rabbit rabbit) {
			if (this.rabbitType != null) {
				rabbit.setRabbitType(this.rabbitType);
			}
		}
		else if (entity instanceof Cat cat) {
			if (this.catType != null) {
				cat.setCatType(this.catType);
			}
		}
		else if (entity instanceof MushroomCow mushroomCow) {
			if (this.mushroomVariant != null) {
				mushroomCow.setVariant(this.mushroomVariant);
			}
		}
		else if (entity instanceof Villager villager) {
			if (this.villagerProfession != null) {
				villager.setProfession(this.villagerProfession);
			}
		}
		else if (entity instanceof Wolf wolf) {
			wolf.setAngry(this.wolfAngry);
		}
		else if (entity instanceof Zombie zombie) {
			if (this.isBaby) zombie.setBaby();
			
			if (zombie instanceof ZombieVillager) {
				if (this.villagerProfession != null) {
					ZombieVillager zombieVillager = (ZombieVillager) zombie;
					zombieVillager.setVillagerProfession(this.villagerProfession);
				}
			}
			else if (zombie instanceof PigZombie pigZombie) {
				pigZombie.setAngry(true);
			}
		}
	}

	public void applyAttributes(@NotNull LivingEntity entity, int lvl) {
		final int lvl2 = Math.min(this.lvlMax, Math.max(this.lvlMin, lvl)) - 1; // -1 to fine value
		
		this.getAttributes().forEach((att, values) -> {
			AttributeInstance aInstance = entity.getAttribute(att);
			if (aInstance == null) return;
			
			// Fix for cases where default value is not present
			// so it will use the vanilla one.
			if (values[0] <= 0) values[0] = aInstance.getBaseValue();
			
			double value = values[0] + (values[1] * lvl2);
			aInstance.setBaseValue(value);
			
			if (att == Attribute.GENERIC_MAX_HEALTH) {
				entity.setHealth(value);
			}
		});
	}
}
