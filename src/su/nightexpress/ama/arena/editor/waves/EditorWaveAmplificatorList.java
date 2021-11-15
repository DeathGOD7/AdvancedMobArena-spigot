package su.nightexpress.ama.arena.editor.waves;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.editor.EditorUtils;
import su.nexmedia.engine.api.menu.AbstractMenuAuto;
import su.nexmedia.engine.api.menu.IMenuClick;
import su.nexmedia.engine.api.menu.IMenuItem;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.arena.config.IArenaConfig;
import su.nightexpress.ama.api.arena.wave.IArenaWave;
import su.nightexpress.ama.api.arena.wave.IArenaWaveAmplificator;
import su.nightexpress.ama.editor.ArenaEditorHandler;
import su.nightexpress.ama.editor.ArenaEditorType;

import java.util.ArrayList;
import java.util.List;

public class EditorWaveAmplificatorList extends AbstractMenuAuto<AMA, IArenaWaveAmplificator> {
	
	private final IArenaWave arenaWave;

	private final String       objectName;
	private final List<String> objectLore;
	
	EditorWaveAmplificatorList(@NotNull IArenaWave arenaWave) {
		super(arenaWave.getArena().plugin(), ArenaEditorHandler.YML_ARENA_WAVE_AMPLIFICATOR_LIST, "");
		this.arenaWave = arenaWave;

		this.objectName = StringUT.color(cfg.getString("Object.Name", "%wave%"));
		this.objectLore = StringUT.color(cfg.getStringList("Object.Lore"));
		
		IMenuClick click = (player, type, e) -> {
			if (type == null) return;

			IArenaConfig config = arenaWave.getArenaConfig();
			if (type instanceof MenuItemType type2) {
				if (type2 == MenuItemType.RETURN) {
					this.arenaWave.getEditor().open(player, 1);
				}
				else super.onItemClickDefault(player, type2);
			}
			else if (type instanceof ArenaEditorType type2) {
				if (type2 == ArenaEditorType.WAVES_CREATE_WAVE_AMPLIFICATOR) {
					plugin.getEditorHandlerNew().startEdit(player, arenaWave, type2);
					EditorUtils.tipCustom(player, plugin.lang().Editor_Arena_Waves_Enter_Amplificator_Create.getMsg());
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
	@NotNull
	protected List<IArenaWaveAmplificator> getObjects(@NotNull Player player) {
		return new ArrayList<>(this.arenaWave.getAmplificators().values());
	}

	@Override
	@NotNull
	protected ItemStack getObjectStack(@NotNull Player player, @NotNull IArenaWaveAmplificator amplificator) {
		ItemStack item = new ItemStack(Material.EXPERIENCE_BOTTLE);
		ItemMeta meta = item.getItemMeta();
		if (meta == null) return item;

		meta.setDisplayName(this.objectName);
		meta.setLore(this.objectLore);
		item.setItemMeta(meta);
		ItemUT.replace(item, amplificator.replacePlaceholders());
		return item;
	}

	@Override
	@NotNull
	protected IMenuClick getObjectClick(@NotNull Player player, @NotNull IArenaWaveAmplificator amplificator) {
		return (p2, type, e) -> {
			if (e.isShiftClick() && e.isRightClick()) {
				amplificator.clear();
				this.arenaWave.getAmplificators().remove(amplificator.getId());
				this.arenaWave.getArenaConfig().getWaveManager().save();
				this.open(p2, 1);
				return;
			}
			amplificator.getEditor().open(p2, 1);
		};
	}

	@Override
	public boolean cancelClick(@NotNull SlotType slotType, int slot) {
		return true;
	}
}
