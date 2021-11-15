package su.nightexpress.ama.editor.handler.arena.region;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.editor.EditorUtils;
import su.nexmedia.engine.manager.editor.EditorManager;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.arena.game.IArenaGameEventTrigger;
import su.nightexpress.ama.api.arena.region.IArenaRegionWave;
import su.nightexpress.ama.arena.game.trigger.AbstractArenaGameEventTrigger;
import su.nightexpress.ama.editor.ArenaEditorType;
import su.nightexpress.ama.editor.ArenaInputHandler;

public class HandlerRegionWave extends ArenaInputHandler<IArenaRegionWave> {

    public HandlerRegionWave(@NotNull AMA plugin) {
        super(plugin);
    }

    @Override
    public boolean onType(
            @NotNull Player player, @NotNull IArenaRegionWave regionWave,
            @NotNull ArenaEditorType type, @NotNull String msg) {

        switch (type) {
            case REGION_WAVE_CHANGE_ID -> regionWave.getArenaWaveIds().add(EditorUtils.fineId(msg));
            case REGION_WAVE_CHANGE_SPAWNERS_ADD -> regionWave.getSpawnerIds().add(EditorManager.fineId(msg));
            case REGION_WAVE_CHANGE_TRIGGERS_ADD -> {
                String[] split = StringUT.colorOff(msg).split(" ");
                if (split.length < 2) {
                    EditorManager.errorCustom(player, plugin.lang().Editor_Error_Triggers.getMsg());
                    return false;
                }

                IArenaGameEventTrigger trigger = AbstractArenaGameEventTrigger.parse(regionWave.getArenaConfig(), split[0], split[1]);
                if (trigger == null) {
                    EditorManager.errorCustom(player, plugin.lang().Editor_Error_Triggers.getMsg());
                    return false;
                }

                regionWave.getTriggers().add(trigger);
            }
            default -> {return true;}
        }

        regionWave.getRegion().save();
        return true;
    }
}
