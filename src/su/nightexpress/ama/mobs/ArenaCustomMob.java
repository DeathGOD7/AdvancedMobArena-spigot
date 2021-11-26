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
import su.nexmedia.engine.api.manager.AbstractLoadableItem;
import su.nexmedia.engine.api.manager.ICleanable;
import su.nexmedia.engine.api.manager.IEditable;
import su.nexmedia.engine.api.manager.IPlaceholder;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.mobs.editor.EditorMobMain;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

public class ArenaCustomMob extends AbstractLoadableItem<AMA> implements IPlaceholder, IEditable, ICleanable {

    private String     name;
    private boolean    nameVisible;
    private EntityType entityType;
    
    private int levelMin;
    private int levelMax;
    
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
    
    private final ArenaMobHealthBar        bossBar;
    private       ItemStack[]              equipment;
    private final Map<Attribute, double[]> attributes; // [0] Base, [1] Per Level
    
    private EditorMobMain editor;

	public static final String PLACEHOLDER_ID = "%mob_id%";
	public static final String PLACEHOLDER_NAME = "%mob_name%";
	public static final String PLACEHOLDER_HEALTH = "%mob_health%";
	public static final String PLACEHOLDER_HEALTH_MAX = "%mob_health_max%";
	public static final String PLACEHOLDER_NAME_VISIBLE = "%mob_name_visible%";
	public static final String PLACEHOLDER_ENTITY_TYPE = "%mob_entity_type%";
	public static final String PLACEHOLDER_LEVEL = "%mob_level%";
	public static final String PLACEHOLDER_LEVEL_MIN = "%mob_level_min%";
	public static final String PLACEHOLDER_LEVEL_MAX = "%mob_level_max%";
	public static final String PLACEHOLDER_BOSSBAR_ENABLED = "%mob_bossbar_enabled%";
	public static final String PLACEHOLDER_BOSSBAR_TITLE = "%mob_bossbar_title%";
	public static final String PLACEHOLDER_BOSSBAR_COLOR = "%mob_bossbar_color%";
	public static final String PLACEHOLDER_BOSSBAR_STYLE = "%mob_bossbar_style%";
	public static final String PLACEHOLDER_ATTRIBUTE_BASE_NAME = "%mob_attribute_base_name%";
	public static final String PLACEHOLDER_ATTRIBUTE_BASE_VALUE = "%mob_attribute_base_value%";
	public static final String PLACEHOLDER_ATTRIBUTE_LEVEL_NAME = "%mob_attribute_level_name%";
	public static final String PLACEHOLDER_ATTRIBUTE_LEVEL_VALUE = "%mob_attribute_level_value%";
	public static final String PLACEHOLDER_SETTINGS_BABY = "%mob_settings_baby%";
	public static final String PLACEHOLDER_SETTINGS_HORSE_COLOR = "%mob_settings_horse_color%";
	public static final String PLACEHOLDER_SETTINGS_HORSE_STYLE = "%mob_settings_horse_style%";
	public static final String PLACEHOLDER_SETTINGS_CREEPER_CHARGED = "%mob_settings_creeper_charged%";
	public static final String PLACEHOLDER_SETTINGS_SLIME_SIZE = "%mob_settings_slime_size%";
	public static final String PLACEHOLDER_SETTINGS_PARROT_VARIANT = "%mob_settings_parrot_variant%";
	public static final String PLACEHOLDER_SETTINGS_LLAMA_COLOR = "%mob_settings_llama_color%";
	public static final String PLACEHOLDER_SETTINGS_SHEEP_COLOR = "%mob_settings_sheep_color%";
	public static final String PLACEHOLDER_SETTINGS_RABBIT_TYPE = "%mob_settings_rabbit_type%";
	public static final String PLACEHOLDER_SETTINGS_CAT_TYPE = "%mob_settings_cat_type%";
	public static final String PLACEHOLDER_SETTINGS_MUSHROOM_COW_VARIANT = "%mob_settings_mushroom_cow_variant%";
	public static final String PLACEHOLDER_SETTINGS_WOLF_ANGRY = "%mob_settings_wolf_angry%";
	public static final String PLACEHOLDER_SETTINGS_VILLAGER_PROFESSION = "%mob_settings_villager_profession%";

	@Override
	@NotNull
	public UnaryOperator<String> replacePlaceholders() {
		return str -> str
			.replace(PLACEHOLDER_ID, this.getId())
			.replace(PLACEHOLDER_NAME, this.getName())
			.replace(PLACEHOLDER_NAME_VISIBLE, plugin.lang().getBool(this.isNameVisible()))
			.replace(PLACEHOLDER_ENTITY_TYPE, plugin.lang().getEnum(this.getEntityType()))
			.replace(PLACEHOLDER_LEVEL_MIN, String.valueOf(this.getLevelMin()))
			.replace(PLACEHOLDER_LEVEL_MAX, String.valueOf(this.getLevelMax()))
			.replace(PLACEHOLDER_BOSSBAR_ENABLED, plugin.lang().getBool(this.getHealthBar().isEnabled()))
			.replace(PLACEHOLDER_BOSSBAR_TITLE, this.getHealthBar().getTitle())
			.replace(PLACEHOLDER_BOSSBAR_COLOR, this.getHealthBar().getColor().name())
			.replace(PLACEHOLDER_BOSSBAR_STYLE, this.getHealthBar().getStyle().name())
			.replace(PLACEHOLDER_SETTINGS_BABY, plugin.lang().getBool(this.isBaby))
			.replace(PLACEHOLDER_SETTINGS_HORSE_COLOR, this.horseColor.name())
			.replace(PLACEHOLDER_SETTINGS_HORSE_STYLE, this.horseStyle.name())
			.replace(PLACEHOLDER_SETTINGS_CREEPER_CHARGED, plugin.lang().getBool(this.creeperCharged))
			.replace(PLACEHOLDER_SETTINGS_SLIME_SIZE, String.valueOf(this.slimeSize))
			.replace(PLACEHOLDER_SETTINGS_PARROT_VARIANT, this.parrotVariant.name())
			.replace(PLACEHOLDER_SETTINGS_LLAMA_COLOR, this.llamaColor.name())
			.replace(PLACEHOLDER_SETTINGS_SHEEP_COLOR, this.sheepColor.name())
			.replace(PLACEHOLDER_SETTINGS_RABBIT_TYPE, this.rabbitType.name())
			.replace(PLACEHOLDER_SETTINGS_CAT_TYPE, this.catType.name())
			.replace(PLACEHOLDER_SETTINGS_MUSHROOM_COW_VARIANT, this.mushroomVariant.name())
			.replace(PLACEHOLDER_SETTINGS_WOLF_ANGRY, plugin.lang().getBool(this.wolfAngry))
			.replace(PLACEHOLDER_SETTINGS_VILLAGER_PROFESSION, this.villagerProfession.name())
			;
	}

	// Creating new config
    public ArenaCustomMob(@NotNull AMA plugin, @NotNull String path) {
    	super(plugin, path);

		this.setEntityType(EntityType.ZOMBIE);
    	this.setName("&f" + StringUT.capitalizeFully(getEntityType().name().toLowerCase().replace("_", " ")) + " &cLv. &6" + PLACEHOLDER_LEVEL);
		this.setNameVisible(true);
		this.setEquipment(new ItemStack[4]);

		this.setLevelMin(1);
		this.setLevelMax(10);

		this.setBaby(false);
		this.setHorseColor(Horse.Color.BLACK);
		this.setHorseStyle(Horse.Style.NONE);
		this.setCreeperCharged(false);
		this.setSlimeSize(4);
		this.setParrotVariant(Parrot.Variant.RED);
		this.setLlamaColor(Llama.Color.GRAY);
		this.setSheepColor(DyeColor.WHITE);
		this.setRabbitType(Rabbit.Type.GOLD);
		this.setCatType(Cat.Type.BLACK);
		this.setMushroomVariant(MushroomCow.Variant.RED);
		this.setVillagerProfession(Villager.Profession.NONE);
		this.setWolfAngry(true);
		
		String barTitle = "&c&l" + PLACEHOLDER_NAME + " &7&l- &f&l" + PLACEHOLDER_HEALTH + "&7/&f&l" + PLACEHOLDER_HEALTH_MAX;
		this.bossBar = new ArenaMobHealthBar(false, barTitle, BarStyle.SOLID, BarColor.RED);
		
		this.attributes = new HashMap<>();
		this.attributes.put(Attribute.GENERIC_MAX_HEALTH, new double[] {20D, 1D});
    }
    
    public ArenaCustomMob(@NotNull AMA plugin, @NotNull JYML cfg) {
    	super(plugin, cfg);

		this.setName(cfg.getString("Name", this.getId()));
		this.setNameVisible(cfg.getBoolean("Name_Visible"));
		EntityType type = cfg.getEnum("Entity_Type", EntityType.class);
		if (type == null) {
			throw new IllegalStateException("Invalid entity type for '" + getId() + "' mob!");
		}
		this.setEntityType(type);
		this.setEquipment(cfg.getItemList64("Equipment"));

		this.setLevelMin(cfg.getInt("Level.Minimum", 1));
		this.setLevelMax(cfg.getInt("Level.Maximum", 1));

		String path = "Settings.";
		this.setBaby(cfg.getBoolean(path + "Is_Baby"));
		this.setHorseColor(cfg.getEnum(path + "Horse.Color", Horse.Color.class, Horse.Color.BLACK));
		this.setHorseStyle(cfg.getEnum(path + "Horse.Style", Horse.Style.class, Horse.Style.NONE));
		this.setCreeperCharged(cfg.getBoolean(path + "Creeper.Charged"));
		this.setSlimeSize(cfg.getInt(path + "Slime.Size", 4));
		this.setParrotVariant(cfg.getEnum(path + "Parrot.Variant", Parrot.Variant.class, Parrot.Variant.RED));
		this.setLlamaColor(cfg.getEnum(path + "Llama.Color", Llama.Color.class, Llama.Color.GRAY));
		this.setSheepColor(cfg.getEnum(path + "Sheep.Color", DyeColor.class, DyeColor.WHITE));
		this.setRabbitType(cfg.getEnum(path + "Rabbit.Type", Rabbit.Type.class, Rabbit.Type.GOLD));
		this.setCatType(cfg.getEnum(path + "Cat.Type", Cat.Type.class, Cat.Type.BLACK));
		this.setMushroomVariant(cfg.getEnum(path + "Mushroom_Cow.Variant", MushroomCow.Variant.class, MushroomCow.Variant.RED));
		this.setVillagerProfession(cfg.getEnum(path + "Villager.Profession", Villager.Profession.class, Villager.Profession.NONE));
		this.setWolfAngry(cfg.getBoolean(path + "Wolf.Angry"));
		
		// Boss Bar
		path = "Boss_Bar.";
		boolean barEnabled = cfg.getBoolean(path + "Enabled");
		String barTitle = cfg.getString(path + "Title", PLACEHOLDER_NAME);
		BarColor barColor = cfg.getEnum(path + "Color", BarColor.class, BarColor.RED);
		BarStyle barStyle = cfg.getEnum(path + "Style", BarStyle.class, BarStyle.SOLID);
		this.bossBar = new ArenaMobHealthBar(barEnabled, barTitle, barStyle, barColor);
		
		// Attributes
		path = "Attributes.";
		this.attributes = new HashMap<>();
		for (Attribute attribute : Attribute.values()) {
			double valueBase = cfg.getDouble(path + "Base." + attribute.name());
			double valueLevel = cfg.getDouble(path + "Per_Level." + attribute.name());
			if (valueBase > 0 || valueLevel > 0) {
				this.attributes.put(attribute, new double[] {valueBase, valueLevel});
			}
		}
    }
    
    @Override
	public void onSave() {
		cfg.set("Name", this.getName());
		cfg.set("Name_Visible", this.isNameVisible());
		cfg.set("Entity_Type", this.getEntityType().name());
		
		cfg.set("Level.Minimum", this.getLevelMin());
		cfg.set("Level.Maximum", this.getLevelMax());

		String path = "Settings.";
		cfg.set(path + "Is_Baby", this.isBaby());
		cfg.set(path + "Creeper.Charged", this.isCreeperCharged());
		cfg.set(path + "Wolf.Angry", this.isWolfAngry());
		cfg.set(path + "Slime.Size", this.getSlimeSize());
		cfg.set(path + "Horse.Color", this.getHorseColor().name());
		cfg.set(path + "Horse.Style", this.getHorseStyle().name());
		cfg.set(path + "Parrot.Variant", this.getParrotVariant().name());
		cfg.set(path + "Llama.Color", this.getLlamaColor().name());
		cfg.set(path + "Sheep.Color", this.getSheepColor().name());
		cfg.set(path + "Rabbit.Type", this.getRabbitType().name());
		cfg.set(path + "Cat.Type", this.getCatType().name());
		cfg.set(path + "Mushroom_Cow.Variant", this.getMushroomVariant().name());
		cfg.set(path + "Villager.Profession", this.getVillagerProfession().name());
		
		cfg.set("Equipment", null);
		cfg.setItemList64("Equipment", Arrays.asList(this.getEquipment()));

		path = "Boss_Bar.";
    	ArenaMobHealthBar bar = this.getHealthBar();
		cfg.set(path + "Enabled", bar.isEnabled());
    	cfg.set(path + "Title", bar.getTitle());
    	cfg.set(path + "Style", bar.getStyle().name());
    	cfg.set(path + "Color", bar.getColor().name());
		
		cfg.set("Attributes", null);
		this.getAttributes().forEach((att, values) -> {
			String name = att.name();
			cfg.set("Attributes.Base." + name, values[0]);
			cfg.set("Attributes.Per_Level." + name, values[1]);
		});
    }

	@NotNull
	@Override
	public EditorMobMain getEditor() {
		if (this.editor == null) {
			this.editor = new EditorMobMain(this);
		}
		return editor;
	}

	@Override
	public void clear() {
		if (this.editor != null) {
			this.editor.clear();
			this.editor = null;
		}
	}

	@NotNull
    public String getName() {
    	return this.name;
    }
    
    public void setName(@NotNull String name) {
    	this.name = StringUT.color(name);
    }
    
    public boolean isNameVisible() {
    	return this.nameVisible;
    }
    
    public void setNameVisible(boolean visible) {
    	this.nameVisible = visible;
    }
    
    @NotNull
    public EntityType getEntityType() {
    	return this.entityType;
    }
    
    public void setEntityType(@NotNull EntityType type) {
    	this.entityType = type;
    }
    
    public int getLevelMin() {
    	return this.levelMin;
    }

	public void setLevelMin(int levelMin) {
		this.levelMin = levelMin;
	}

	public int getLevelMax() {
    	return this.levelMax;
    }

	public void setLevelMax(int levelMax) {
		this.levelMax = levelMax;
	}

	@NotNull
    public ItemStack[] getEquipment() {
    	return this.equipment;
    }
    
    public void setEquipment(@NotNull ItemStack[] equip) {
    	this.equipment = equip;
    }
    
    /**
     * @return Mob attribute values array, where [0] is base value, [1] is per level increase.
     */
    @NotNull
    public Map<Attribute, double[]> getAttributes() {
    	return this.attributes;
    }

	public boolean isBaby() {
		return isBaby;
	}

	public void setBaby(boolean baby) {
		isBaby = baby;
	}

	@NotNull
	public Horse.Color getHorseColor() {
		return horseColor;
	}

	public void setHorseColor(@NotNull Horse.Color horseColor) {
		this.horseColor = horseColor;
	}

	@NotNull
	public Horse.Style getHorseStyle() {
		return horseStyle;
	}

	public void setHorseStyle(@NotNull Horse.Style horseStyle) {
		this.horseStyle = horseStyle;
	}

	public boolean isCreeperCharged() {
		return creeperCharged;
	}

	public void setCreeperCharged(boolean creeperCharged) {
		this.creeperCharged = creeperCharged;
	}

	public int getSlimeSize() {
		return slimeSize;
	}

	public void setSlimeSize(int slimeSize) {
		this.slimeSize = slimeSize;
	}

	@NotNull
	public Parrot.Variant getParrotVariant() {
		return parrotVariant;
	}

	public void setParrotVariant(@NotNull Parrot.Variant parrotVariant) {
		this.parrotVariant = parrotVariant;
	}

	@NotNull
	public Llama.Color getLlamaColor() {
		return llamaColor;
	}

	public void setLlamaColor(@NotNull Llama.Color llamaColor) {
		this.llamaColor = llamaColor;
	}

	@NotNull
	public DyeColor getSheepColor() {
		return sheepColor;
	}

	public void setSheepColor(@NotNull DyeColor sheepColor) {
		this.sheepColor = sheepColor;
	}

	@NotNull
	public Rabbit.Type getRabbitType() {
		return rabbitType;
	}

	public void setRabbitType(@NotNull Rabbit.Type rabbitType) {
		this.rabbitType = rabbitType;
	}

	@NotNull
	public Cat.Type getCatType() {
		return catType;
	}

	public void setCatType(@NotNull Cat.Type catType) {
		this.catType = catType;
	}

	@NotNull
	public MushroomCow.Variant getMushroomVariant() {
		return mushroomVariant;
	}

	public void setMushroomVariant(@NotNull MushroomCow.Variant mushroomVariant) {
		this.mushroomVariant = mushroomVariant;
	}

	public boolean isWolfAngry() {
		return wolfAngry;
	}

	public void setWolfAngry(boolean wolfAngry) {
		this.wolfAngry = wolfAngry;
	}

	@NotNull
	public Villager.Profession getVillagerProfession() {
		return villagerProfession;
	}

	public void setVillagerProfession(@NotNull Villager.Profession villagerProfession) {
		this.villagerProfession = villagerProfession;
	}

	@NotNull
    public ArenaMobHealthBar getHealthBar() {
    	return this.bossBar;
    }
	
	public void applySettings(@NotNull LivingEntity entity, int level) {
		entity.setCustomName(this.getName().replace(PLACEHOLDER_LEVEL, String.valueOf(level)));
		entity.setCustomNameVisible(this.isNameVisible());

		EntityEquipment armor = entity.getEquipment();
		if (armor != null) armor.setArmorContents(this.getEquipment());

		if (entity instanceof Ageable age) {
			if (this.isBaby()) age.setBaby();
			else age.setAdult();
		}

		if (entity instanceof Horse horse) {
			horse.setStyle(this.getHorseStyle());
			horse.setColor(this.getHorseColor());
		}
		else if (entity instanceof Creeper creeper) {
			creeper.setPowered(this.isCreeperCharged());
		}
		else if (entity instanceof Slime slime) {
			slime.setSize(this.getSlimeSize());
		}
		else if (entity instanceof Parrot parrot) {
			parrot.setVariant(this.getParrotVariant());
		}
		else if (entity instanceof Llama llama) {
			llama.setColor(this.getLlamaColor());
		}
		else if (entity instanceof Sheep sheep) {
			sheep.setColor(this.getSheepColor());
		}
		else if (entity instanceof Rabbit rabbit) {
			rabbit.setRabbitType(this.getRabbitType());
		}
		else if (entity instanceof Cat cat) {
			cat.setCatType(this.getCatType());
		}
		else if (entity instanceof MushroomCow mushroomCow) {
			mushroomCow.setVariant(this.getMushroomVariant());
		}
		else if (entity instanceof Villager villager) {
			villager.setProfession(this.getVillagerProfession());
		}
		else if (entity instanceof Wolf wolf) {
			wolf.setAngry(this.isWolfAngry());
		}
		else if (entity instanceof Zombie zombie) {
			if (this.isBaby()) zombie.setBaby();
			
			if (zombie instanceof ZombieVillager zombieVillager) {
				zombieVillager.setVillagerProfession(this.getVillagerProfession());
			}
			else if (zombie instanceof PigZombie pigZombie) {
				pigZombie.setAngry(true);
			}
		}
	}

	public void applyAttributes(@NotNull LivingEntity entity, int level) {
		final int lvl2 = Math.min(this.getLevelMax(), Math.max(this.getLevelMin(), level)) - 1; // -1 to fine value
		
		this.getAttributes().forEach((attribute, values) -> {
			AttributeInstance aInstance = entity.getAttribute(attribute);
			if (aInstance == null) return;
			
			// Fix for cases where default value is not present
			// so it will use the vanilla one.
			if (values[0] <= 0) values[0] = aInstance.getBaseValue();
			
			double value = values[0] + (values[1] * lvl2);
			aInstance.setBaseValue(value);
			
			if (attribute == Attribute.GENERIC_MAX_HEALTH) {
				entity.setHealth(value);
			}
		});
	}
}
