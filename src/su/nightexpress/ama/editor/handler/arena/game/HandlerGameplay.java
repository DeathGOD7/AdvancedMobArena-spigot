package su.nightexpress.ama.editor.handler.arena.game;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.manager.editor.EditorManager;
import su.nexmedia.engine.utils.CollectionsUT;
import su.nexmedia.engine.utils.StringUT;
import su.nexmedia.engine.utils.constants.JStrings;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.arena.game.IArenaGameplayManager;
import su.nightexpress.ama.api.kits.IArenaKit;
import su.nightexpress.ama.editor.ArenaEditorType;
import su.nightexpress.ama.editor.ArenaInputHandler;

public class HandlerGameplay extends ArenaInputHandler<IArenaGameplayManager> {

	public HandlerGameplay(@NotNull AMA plugin) {
		super(plugin);
	}

	@Override
	public boolean onType(
			@NotNull Player player, @NotNull IArenaGameplayManager game,
			@NotNull ArenaEditorType type, @NotNull String msg) {

		switch (type) {
			case GAMEPLAY_CHANGE_TIMELEFT -> {
				int time = StringUT.getInteger(msg, 0);
				game.setTimeleft(time);
			}
			case GAMEPLAY_CHANGE_LOBBY_TIME -> {
				int time = StringUT.getInteger(msg, 1);
				game.setLobbyTime(time);
			}

			/*case GAMEPLAY_CHANGE_COINS_MULTIPLIER: {
				double mult = StringUT.getDouble(msg, 1);
				game.setCoinsMultiplier(mult);
				break;
			}*/
			case GAMEPLAY_CHANGE_PLAYERS_MIN -> {
				int amount = StringUT.getInteger(msg, 1);
				game.setPlayerMinAmount(amount);
			}
			case GAMEPLAY_CHANGE_PLAYERS_MAX -> {
				int amount = StringUT.getInteger(msg, 1);
				game.setPlayerMaxAmount(amount);
			}
			case GAMEPLAY_CHANGE_PLAYERS_LIVES -> {
				int amount = StringUT.getInteger(msg, 1);
				game.setPlayerLivesAmount(amount);
			}
			case GAMEPLAY_CHANGE_COMMANDS_ADD_WHITE -> game.getPlayerCommandsAllowed().add(StringUT.colorOff(msg));
			case GAMEPLAY_CHANGE_BANNED_ITEMS -> {
				Material material = Material.getMaterial(msg.toUpperCase());
				if (material == null) {
					EditorManager.errorCustom(player, plugin.lang().Editor_Arena_Gameplay_Error_BannedItems.getMsg());
					return false;
				}
				game.getBannedItems().add(material);
			}
			case GAMEPLAY_CHANGE_ALLOWED_SPAWN_REASONS -> {
				CreatureSpawnEvent.SpawnReason spawnReason = CollectionsUT.getEnum(msg, CreatureSpawnEvent.SpawnReason.class);
				if (spawnReason == null) {
					EditorManager.errorEnum(player, CreatureSpawnEvent.SpawnReason.class);
					return false;
				}
				game.getAllowedSpawnReasons().add(spawnReason);
			}
			case GAMEPLAY_CHANGE_KITS_ADD_ALLOWED -> {
				String id = StringUT.colorOff(msg);
				IArenaKit kit = plugin.getKitManager().getKitById(id);
				if (kit == null && !id.equals(JStrings.MASK_ANY)) {
					EditorManager.errorCustom(player, plugin.lang().Editor_Arena_Gameplay_Error_Kits_InvalidKit.getMsg());
					return false;
				}
				game.getKitsAllowed().add(id.toLowerCase());
			}
			case GAMEPLAY_CHANGE_KITS_ADD_LIMIT -> {
				String[] split = msg.split(" ");
				int limit = split.length >= 2 ? StringUT.getInteger(split[0], -1) : -1;
				if (limit < 0) {
					EditorManager.errorCustom(player, plugin.lang().Editor_Arena_Gameplay_Error_Kits_InvalidLimit.getMsg());
					return false;
				}

				String id = split[1];
				IArenaKit kit = plugin.getKitManager().getKitById(id);
				if (kit == null) {
					EditorManager.errorCustom(player, plugin.lang().Editor_Arena_Gameplay_Error_Kits_InvalidKit.getMsg());
					return false;
				}

				game.getKitsLimits().put(kit.getId(), limit);
			}
			default -> {
				return true;
			}
		}

		game.save();
		return true;
	}
}
