package su.nightexpress.ama.editor.handler.arena.region;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.manager.editor.EditorManager;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.arena.game.IArenaGameEventTrigger;
import su.nightexpress.ama.api.arena.region.IArenaRegion;
import su.nightexpress.ama.api.arena.type.ArenaLockState;
import su.nightexpress.ama.arena.game.trigger.AbstractArenaGameEventTrigger;
import su.nightexpress.ama.arena.region.ArenaRegionWave;
import su.nightexpress.ama.editor.ArenaEditorType;
import su.nightexpress.ama.editor.ArenaInputHandler;

import java.util.Collections;
import java.util.HashSet;

public class HandlerRegion extends ArenaInputHandler<IArenaRegion> {

    public HandlerRegion(@NotNull AMA plugin) {
        super(plugin);
    }

    @Override
    public boolean onType(
            @NotNull Player player, @NotNull IArenaRegion region,
            @NotNull ArenaEditorType type, @NotNull String msg) {

        switch (type) {
            case REGION_WAVE_CREATE -> {
                String id = EditorManager.fineId(msg);
                boolean has = region.getWaves().stream().anyMatch(wave -> wave.getId().equalsIgnoreCase(id));
                if (has) {
                    EditorManager.errorCustom(player, plugin.lang().Editor_Region_Wave_Error_Create.getMsg());
                    return false;
                }

                ArenaRegionWave wave = new ArenaRegionWave(region, id, new HashSet<>(Collections.singletonList(id)), new HashSet<>(), new HashSet<>());
                region.getWaves().add(wave);
            }
            case REGION_CHANGE_TRIGGERS_LOCKED, REGION_CHANGE_TRIGGERS_UNLOCKED -> {
                String[] split = StringUT.colorOff(msg).split(" ");
                if (split.length < 2) {
                    EditorManager.errorCustom(player, plugin.lang().Editor_Error_Triggers.getMsg());
                    return false;
                }

                IArenaGameEventTrigger trigger = AbstractArenaGameEventTrigger.parse(region.getArenaConfig(), split[0], split[1]);
                if (trigger == null) {
                    EditorManager.errorCustom(player, plugin.lang().Editor_Error_Triggers.getMsg());
                    return false;
                }

                ArenaLockState state = type == ArenaEditorType.REGION_CHANGE_TRIGGERS_LOCKED ? ArenaLockState.LOCKED : ArenaLockState.UNLOCKED;
                region.getStateTriggers(state).add(trigger);
            }
            case REGION_CHANGE_NAME -> region.setName(msg);
            default -> {return true;}
        }

        region.save();
        return true;
    }
}
