package su.nightexpress.ama.arena.game.trigger;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.hooks.external.MythicMobsHK;
import su.nexmedia.engine.utils.Constants;
import su.nexmedia.engine.utils.ItemUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.arena.IArena;
import su.nightexpress.ama.api.arena.config.IArenaConfig;
import su.nightexpress.ama.api.arena.event.ArenaMobDeathEvent;
import su.nightexpress.ama.api.arena.event.ArenaPlayerGameEvent;
import su.nightexpress.ama.api.arena.game.ArenaGameEventType;
import su.nightexpress.ama.api.arena.game.event.ArenaGameEventEvent;
import su.nightexpress.ama.api.arena.region.IArenaRegion;
import su.nightexpress.ama.api.arena.region.event.ArenaRegionEvent;
import su.nightexpress.ama.api.arena.shop.IArenaShopProduct;
import su.nightexpress.ama.api.arena.shop.event.ArenaShopProductEvent;
import su.nightexpress.ama.api.arena.spot.IArenaSpot;
import su.nightexpress.ama.api.arena.spot.event.ArenaSpotStateChangeEvent;
import su.nightexpress.ama.arena.game.trigger.value.AbstractArenaGameTriggerValue;
import su.nightexpress.ama.arena.game.trigger.value.ArenaGameTriggerValueString;
import su.nightexpress.ama.mobs.ArenaCustomMob;

public class ArenaGameEventTriggerString extends AbstractArenaGameEventTrigger<String> {

    public ArenaGameEventTriggerString(@NotNull IArenaConfig arenaConfig, @NotNull ArenaGameEventType eventType, @NotNull String inputs) {
        super(arenaConfig, eventType, inputs);
    }

    @Override
    protected void loadValues(@NotNull String[] values) {
        for (String value : values) {
            this.values.add(new ArenaGameTriggerValueString(value));
        }
    }

    @Override
    @NotNull
    protected String formatValue(@NotNull AbstractArenaGameTriggerValue<String> triggerValue) {
        AMA plugin = this.arenaConfig.plugin();
        String value = triggerValue.getValue();
        if (value.equalsIgnoreCase(Constants.MASK_ANY)) {
            return plugin.lang().Other_Any.getMsg();
        }

        return switch (this.getType()) {
            case MOB_KILLED -> {
                ArenaCustomMob customMob = plugin.getMobManager().getMobById(value);
                if (customMob != null) yield customMob.getName();

                MythicMobsHK mythicMobs = plugin.getMythicMobs();
                if (mythicMobs != null) yield mythicMobs.getName(value);

                yield value;
            }
            case SPOT_CHANGED -> {
                IArenaSpot spot = arenaConfig.getSpotManager().getSpot(value);
                /*plugin.getArenaManager().getArenas().stream()
                        .map(arena -> arena.getConfig().getSpotManager())
                        .map(IArenaSpotManager::getSpotsMap)
                        .filter(map -> map.containsKey(triggerValue.getValue()))
                        .findFirst().orElse(Collections.emptyMap()).get(value);*/
                yield spot != null ? spot.getName() : value;
            }
            case REGION_LOCKED, REGION_UNLOCKED -> {
                IArenaRegion region = arenaConfig.getRegionManager().getRegion(value);
                        /*plugin.getArenaManager().getArenas().stream()
                        .map(arena -> arena.getConfig().getRegionManager())
                        .map(IArenaRegionManager::getRegionsMap).filter(map -> map.containsKey(triggerValue.getValue()))
                        .findFirst().orElse(Collections.emptyMap()).get(value);*/
                yield region != null ? region.getName() : value;
            }
            case SHOP_ITEM_LOCKED, SHOP_ITEM_UNLOCKED -> {
                IArenaShopProduct shopItem = arenaConfig.getShopManager().getProducts().stream()
                        .filter(product -> product.getId().equalsIgnoreCase(value)).findFirst().orElse(null);
                        /*plugin.getArenaManager().getArenas().stream()
                        .map(arena -> arena.getConfig().getShopManager())
                        .map(IArenaShopManager::getProducts)
                        .filter(products -> products.stream().anyMatch(product -> product.getId().equalsIgnoreCase(triggerValue.getValue())))
                        .findFirst().orElse(Collections.emptySet()).stream().filter(product -> product.getId().equalsIgnoreCase(triggerValue.getValue()))
                        .findFirst().orElse(null);*/
                yield shopItem != null ? ItemUT.getItemName(shopItem.getPreview()) : value;
            }
            default -> value;
        };
    }

    @Override
    public boolean isReady(@NotNull ArenaGameEventEvent event) {
        if (event.getEventType() != this.getType()) return false;

        IArena arena = event.getArena();
        String arenaValue = switch (this.getType()) {
            case MOB_KILLED -> {
                ArenaMobDeathEvent deathEvent = (ArenaMobDeathEvent) event;
                yield deathEvent.getMobId();
            }
            case PLAYER_DEATH, PLAYER_JOIN, PLAYER_LEAVE -> {
                ArenaPlayerGameEvent playerGameEvent = (ArenaPlayerGameEvent) event;
                yield playerGameEvent.getArenaPlayer().getPlayer().getName();
            }
            case SPOT_CHANGED -> {
                ArenaSpotStateChangeEvent spotEvent = (ArenaSpotStateChangeEvent) event;
                yield spotEvent.getSpot().getId();
            }
            case REGION_LOCKED, REGION_UNLOCKED -> {
                ArenaRegionEvent regionEvent = (ArenaRegionEvent) event;
                yield regionEvent.getArenaRegion().getId();
            }
            case SHOP_ITEM_LOCKED, SHOP_ITEM_UNLOCKED -> {
                ArenaShopProductEvent shopEvent = (ArenaShopProductEvent) event;
                yield shopEvent.getShopProduct().getId();
            }
            default -> "";
        };

        return this.test(arenaValue);
    }
}
