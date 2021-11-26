package su.nightexpress.ama.editor.handler.mob;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.CollectionsUT;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.editor.ArenaEditorType;
import su.nightexpress.ama.editor.ArenaInputHandler;
import su.nightexpress.ama.mobs.ArenaCustomMob;

public class EditorHandlerMob extends ArenaInputHandler<ArenaCustomMob> {

    public EditorHandlerMob(@NotNull AMA plugin) {
        super(plugin);
    }

    @Override
    public boolean onType(@NotNull Player player, @NotNull ArenaCustomMob mob,
                          @NotNull ArenaEditorType type, @NotNull String msg) {

        switch (type) {
            case MOB_CHANGE_BOSSBAR_TITLE -> mob.getHealthBar().setTitle(msg);
            case MOB_CHANGE_ATTRIBUTES_BASE, MOB_CHANGE_ATTRIBUTES_LEVEL -> {
                String[] split = msg.split(" ");
                if (split.length != 2) {
                    return false;
                }

                Attribute attribute = CollectionsUT.getEnum(split[0], Attribute.class);
                if (attribute == null) {
                    return false;
                }

                double value = StringUT.getDouble(split[1], 0D);
                double[] valuesHas = mob.getAttributes().computeIfAbsent(attribute, k -> new double[2]);
                int index = type == ArenaEditorType.MOB_CHANGE_ATTRIBUTES_BASE ? 0 : 1;
                valuesHas[index] = value;
                mob.getAttributes().put(attribute, valuesHas);
            }
            case MOB_CHANGE_ENTITY_TYPE -> {
                EntityType entityType = CollectionsUT.getEnum(msg, EntityType.class);
                if (entityType == null || !entityType.isSpawnable() || !entityType.isAlive()) {
                    return false;
                }
                mob.setEntityType(entityType);
            }
            case MOB_CHANGE_SETTINGS_SLIME -> mob.setSlimeSize(StringUT.getInteger(msg, 1));
            case MOB_CHANGE_NAME -> mob.setName(msg);
            case MOB_CHANGE_LEVEL_MIN -> mob.setLevelMin(StringUT.getInteger(msg, 1));
            case MOB_CHANGE_LEVEL_MAX -> mob.setLevelMax(StringUT.getInteger(msg, 1));
        }

        mob.save();
        return true;
    }
}
