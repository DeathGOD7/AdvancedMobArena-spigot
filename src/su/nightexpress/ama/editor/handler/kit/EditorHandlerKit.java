package su.nightexpress.ama.editor.handler.kit;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.editor.EditorUtils;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.kits.IArenaKit;
import su.nightexpress.ama.editor.ArenaEditorType;
import su.nightexpress.ama.editor.ArenaInputHandler;

public class EditorHandlerKit extends ArenaInputHandler<IArenaKit> {

	public EditorHandlerKit(@NotNull AMA plugin) {
		super(plugin);
	}

	@Override
	public boolean onType(
			@NotNull Player player, @NotNull IArenaKit kit,
			@NotNull ArenaEditorType type, @NotNull String msg) {

		switch (type) {
			case KIT_CHANGE_NAME -> kit.setName(msg);
			case KIT_CHANGE_COMMANDS -> kit.getCommands().add(StringUT.colorRaw(msg));
			case KIT_CHANGE_POTIONS -> {
				String[] split = msg.split(":");
				PotionEffectType effectType = PotionEffectType.getByName(split[0].toUpperCase());
				if (effectType == null) {
					EditorUtils.errorCustom(player, "&cInvalid Effect!");
					return false;
				}
				int amp = split.length >= 2 ? StringUT.getInteger(split[1], 1) : 1;
				PotionEffect potionEffect = new PotionEffect(effectType, Integer.MAX_VALUE, Math.max(0, amp - 1));
				kit.getPotionEffects().add(potionEffect);
			}
			case KIT_CHANGE_COST -> {
				int cost = StringUT.getInteger(msg, -999);
				if (cost == -999) {
					EditorUtils.errorNumber(player, false);
					return false;
				}
				kit.setCost(cost);
			}
			default -> {return true;}
		}
		
		kit.save();
		return true;
	}
}
