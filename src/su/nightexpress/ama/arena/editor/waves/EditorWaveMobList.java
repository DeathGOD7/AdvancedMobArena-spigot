package su.nightexpress.ama.arena.editor.waves;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.editor.EditorUtils;
import su.nexmedia.engine.api.menu.AbstractMenuAuto;
import su.nexmedia.engine.api.menu.IMenuClick;
import su.nexmedia.engine.api.menu.IMenuItem;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.arena.wave.IArenaWave;
import su.nightexpress.ama.api.arena.wave.IArenaWaveMob;
import su.nightexpress.ama.editor.ArenaEditorHandler;
import su.nightexpress.ama.editor.ArenaEditorType;
import su.nightexpress.ama.mobs.ArenaCustomMob;

import java.util.ArrayList;
import java.util.List;

public class EditorWaveMobList extends AbstractMenuAuto<AMA, IArenaWaveMob> {
	
	private final IArenaWave arenaWave;

	private final String       objectName;
	private final List<String> objectLore;
	
	EditorWaveMobList(@NotNull IArenaWave arenaWave) {
		super(arenaWave.getArena().plugin(), ArenaEditorHandler.YML_ARENA_WAVE_MOBS, "");
		this.arenaWave = arenaWave;

		this.objectName = StringUT.color(cfg.getString("Object.Name", IArenaWaveMob.PLACEHOLDER_ID));
		this.objectLore = StringUT.color(cfg.getStringList("Object.Lore"));
		
		IMenuClick click = (player, type, e) -> {
			if (type == null) return;

			if (type instanceof MenuItemType type2) {
				if (type2 == MenuItemType.RETURN) {
					this.arenaWave.getEditor().open(player, 1);
				}
				else super.onItemClickDefault(player, type2);
			}
			else if (type instanceof ArenaEditorType type2) {
				if (type2 == ArenaEditorType.WAVES_CREATE_WAVE_MOB) {
					plugin.getEditorHandlerNew().startEdit(player, arenaWave, type2);
					EditorUtils.tipCustom(player, plugin.lang().Editor_Arena_Waves_Enter_Mob_Create.getMsg());
					EditorUtils.sendClickableTips(player, plugin.getMobManager().getSupportedMobIds());
					player.closeInventory();
				}
			}
		};
		
		
		for (String sId : cfg.getSection("Content")) {
			IMenuItem menuItem = cfg.getMenuItem("Content." + sId, MenuItemType.class);
			
			if (menuItem.getType() != null) {
				menuItem.setClick(click);
			}
			this.addItem(menuItem);
		}
		
		for (String sId : cfg.getSection("Editor")) {
			IMenuItem menuItem = cfg.getMenuItem("Editor." + sId, ArenaEditorType.class);
			
			if (menuItem.getType() != null) {
				menuItem.setClick(click);
			}
			this.addItem(menuItem);
		}
	}

	@Override
	public void onReady(@NotNull Player player, @NotNull Inventory inventory) {

	}

	@Override
	public boolean cancelClick(@NotNull SlotType slotType, int slot) {
		return true;
	}

	@Override
	@NotNull
	protected List<IArenaWaveMob> getObjects(@NotNull Player player) {
		return new ArrayList<>(this.arenaWave.getMobs().values());
	}

	@Override
	@NotNull
	protected ItemStack getObjectStack(@NotNull Player player, @NotNull IArenaWaveMob waveMob) {
		Material material = Material.BAT_SPAWN_EGG;
		ArenaCustomMob customMob = plugin.getMobManager().getMobById(waveMob.getMobId());
		if (customMob != null) {
			material = Material.getMaterial(customMob.getEntityType().name() + "_SPAWN_EGG");
			if (customMob.getEntityType() == EntityType.MUSHROOM_COW) material = Material.MOOSHROOM_SPAWN_EGG;
			if (material == null) material = Material.BAT_SPAWN_EGG;
		}

		ItemStack item = new ItemStack(material);
		ItemMeta meta = item.getItemMeta();
		if (meta == null) return item;

		List<String> lore = new ArrayList<>(this.objectLore);
		lore.replaceAll(waveMob.replacePlaceholders());

		meta.setDisplayName(waveMob.replacePlaceholders().apply(this.objectName));
		meta.setLore(lore);

		item.setItemMeta(meta);
		return item;
	}

	@Override
	@NotNull
	protected IMenuClick getObjectClick(@NotNull Player player, @NotNull IArenaWaveMob waveMob) {
		return (p2, type, e) -> {
			if (e.isShiftClick() && e.isRightClick()) {
				this.arenaWave.getMobs().remove(waveMob.getMobId());
				this.arenaWave.getArenaConfig().getWaveManager().save();
				this.open(p2, 1);
				return;
			}

			if (e.isLeftClick()) {
				plugin.getEditorHandlerNew().startEdit(p2, waveMob, ArenaEditorType.WAVES_CHANGE_WAVE_MOB_AMOUNT);
				EditorUtils.tipCustom(p2, plugin.lang().Editor_Arena_Waves_Enter_Mob_Amount.getMsg());
			}
			else if (e.isRightClick()) {
				plugin.getEditorHandlerNew().startEdit(p2, waveMob, ArenaEditorType.WAVES_CHANGE_WAVE_MOB_LEVEL);
				EditorUtils.tipCustom(p2, plugin.lang().Editor_Arena_Waves_Enter_Mob_Level.getMsg());
			}
			else if (e.getClick() == ClickType.MIDDLE) {
				plugin.getEditorHandlerNew().startEdit(p2, waveMob, ArenaEditorType.WAVES_CHANGE_WAVE_MOB_CHANCE);
				EditorUtils.tipCustom(p2, plugin.lang().Editor_Arena_Waves_Enter_Mob_Chance.getMsg());
			}
			p2.closeInventory();
		};
	}
}
