package su.nightexpress.ama.editor.handler.arena.config;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.arena.config.IArenaConfig;
import su.nightexpress.ama.editor.ArenaEditorType;
import su.nightexpress.ama.editor.ArenaInputHandler;

public class HandlerArenaConfig extends ArenaInputHandler<IArenaConfig> {

	public HandlerArenaConfig(@NotNull AMA plugin) {
		super(plugin);
	}

	@Override
	public boolean onType(
			@NotNull Player player, @NotNull IArenaConfig arenaConfig,
			@NotNull ArenaEditorType type, @NotNull String msg) {

		switch (type) {
			case ARENA_CHANGE_REQUIREMENT_MONEY -> arenaConfig.setJoinMoneyRequired(StringUT.getDouble(msg, 0D));
		}
		return true;
	}
}
