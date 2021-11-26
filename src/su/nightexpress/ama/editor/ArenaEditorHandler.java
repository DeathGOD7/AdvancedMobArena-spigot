package su.nightexpress.ama.editor;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.editor.AbstractEditorHandler;
import su.nexmedia.engine.api.editor.EditorUtils;
import su.nexmedia.engine.config.api.JYML;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.arena.config.IArenaConfig;
import su.nightexpress.ama.api.arena.game.IArenaGameCommand;
import su.nightexpress.ama.api.arena.region.IArenaRegion;
import su.nightexpress.ama.api.arena.region.IArenaRegionContainer;
import su.nightexpress.ama.api.arena.region.IArenaRegionWave;
import su.nightexpress.ama.api.arena.reward.IArenaReward;
import su.nightexpress.ama.api.arena.shop.IArenaShopProduct;
import su.nightexpress.ama.api.arena.spot.IArenaSpot;
import su.nightexpress.ama.api.arena.spot.IArenaSpotState;
import su.nightexpress.ama.api.arena.wave.IArenaWave;
import su.nightexpress.ama.api.arena.wave.IArenaWaveAmplificator;
import su.nightexpress.ama.api.arena.wave.IArenaWaveMob;
import su.nightexpress.ama.api.kits.IArenaKit;
import su.nightexpress.ama.arena.config.ArenaConfig;
import su.nightexpress.ama.arena.game.ArenaGameplayManager;
import su.nightexpress.ama.arena.region.ArenaRegionManager;
import su.nightexpress.ama.arena.shop.ArenaShopManager;
import su.nightexpress.ama.arena.spot.ArenaSpotManager;
import su.nightexpress.ama.arena.wave.ArenaWaveManager;
import su.nightexpress.ama.editor.handler.kit.EditorHandlerKit;
import su.nightexpress.ama.editor.handler.kit.EditorHandlerKitManager;
import su.nightexpress.ama.editor.handler.arena.config.HandlerArenaConfig;
import su.nightexpress.ama.editor.handler.arena.game.HandlerGameplay;
import su.nightexpress.ama.editor.handler.arena.game.HandlerGameplayCommand;
import su.nightexpress.ama.editor.handler.arena.region.HandlerRegion;
import su.nightexpress.ama.editor.handler.arena.region.HandlerRegionContainer;
import su.nightexpress.ama.editor.handler.arena.region.HandlerRegionManager;
import su.nightexpress.ama.editor.handler.arena.region.HandlerRegionWave;
import su.nightexpress.ama.editor.handler.arena.reward.HandlerReward;
import su.nightexpress.ama.editor.handler.arena.shop.HandlerShopManager;
import su.nightexpress.ama.editor.handler.arena.shop.HandlerShopProduct;
import su.nightexpress.ama.editor.handler.arena.spot.HandlerSpot;
import su.nightexpress.ama.editor.handler.arena.spot.HandlerSpotManager;
import su.nightexpress.ama.editor.handler.arena.spot.HandlerSpotState;
import su.nightexpress.ama.editor.handler.arena.wave.HandlerWave;
import su.nightexpress.ama.editor.handler.arena.wave.HandlerWaveAmplificator;
import su.nightexpress.ama.editor.handler.arena.wave.HandlerWaveManager;
import su.nightexpress.ama.editor.handler.arena.wave.HandlerWaveMob;
import su.nightexpress.ama.editor.handler.mob.EditorHandlerMob;
import su.nightexpress.ama.editor.handler.mob.EditorHandlerMobManager;
import su.nightexpress.ama.kits.KitManager;
import su.nightexpress.ama.mobs.ArenaCustomMob;
import su.nightexpress.ama.mobs.MobManager;

public class ArenaEditorHandler extends AbstractEditorHandler<AMA, ArenaEditorType> {

    public static JYML YML_HUB;
    public static JYML YML_ARENA_LIST;
    public static JYML YML_ARENA_MAIN;

    public static JYML YML_ARENA_GAMEPLAY;
    public static JYML YML_ARENA_GAME_COMMAND_LIST;
    public static JYML YML_ARENA_GAME_COMMAND_SETTINGS;

    public static JYML YML_ARENA_WAVE_MANAGER;
    public static JYML YML_ARENA_WAVE_GRADUAL;
    public static JYML YML_ARENA_WAVE_LIST;
    public static JYML YML_ARENA_WAVE_MAIN;
    public static JYML YML_ARENA_WAVE_MOBS;
    public static JYML YML_ARENA_WAVE_AMPLIFICATOR_LIST;
    public static JYML YML_ARENA_WAVE_AMPLIFICATOR_SETTINGS;

    public static JYML YML_ARENA_REGION_LIST;
    public static JYML YML_ARENA_REGION_MAIN;
    public static JYML YML_ARENA_REGION_WAVE_LIST;
    public static JYML YML_ARENA_REGION_WAVE_SETTINGS;
    public static JYML YML_ARENA_REGION_CONTAINER_LIST;
    public static JYML YML_ARENA_REGION_CONTAINER_SETTINGS;

    public static JYML YML_ARENA_REWARD_LIST;
    public static JYML YML_ARENA_REWARD_SETTINGS;

    public static JYML YML_ARENA_SHOP_MANAGER;
    public static JYML YML_ARENA_SHOP_PRODUCT_LIST;
    public static JYML YML_ARENA_SHOP_PRODUCT_SETTINGS;

    public static JYML ARENA_SPOT_LIST;
    public static JYML ARENA_SPOT_MAIN;
    public static JYML ARENA_SPOT_STATE_LIST;

    public static JYML KIT_LIST;
    public static JYML KIT_MAIN;

    public static JYML SETUP_ITEMS;

    public ArenaEditorHandler(@NotNull AMA plugin) {
        super(plugin);
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        YML_HUB = JYML.loadOrExtract(plugin, "/editor/hub.yml");

        YML_ARENA_LIST = JYML.loadOrExtract(plugin, "/editor/arena/list.yml");
        YML_ARENA_MAIN = JYML.loadOrExtract(plugin, "/editor/arena/main.yml");

        YML_ARENA_GAMEPLAY = JYML.loadOrExtract(plugin, "/editor/arena/game/gameplay.yml");
        YML_ARENA_GAME_COMMAND_LIST = JYML.loadOrExtract(plugin, "/editor/arena/game/command_list.yml");
        YML_ARENA_GAME_COMMAND_SETTINGS = JYML.loadOrExtract(plugin, "/editor/arena/game/command_settings.yml");

        YML_ARENA_WAVE_MANAGER = JYML.loadOrExtract(plugin, "/editor/arena/waves/manager_main.yml");
        YML_ARENA_WAVE_GRADUAL = JYML.loadOrExtract(plugin, "/editor/arena/waves/manager_gradual.yml");
        YML_ARENA_WAVE_LIST = JYML.loadOrExtract(plugin, "/editor/arena/waves/wave_list.yml");
        YML_ARENA_WAVE_MAIN = JYML.loadOrExtract(plugin, "/editor/arena/waves/wave_settings.yml");
        YML_ARENA_WAVE_MOBS = JYML.loadOrExtract(plugin, "/editor/arena/waves/wave_mob_list.yml");
        YML_ARENA_WAVE_AMPLIFICATOR_LIST = JYML.loadOrExtract(plugin, "/editor/arena/waves/wave_amplificator_list.yml");
        YML_ARENA_WAVE_AMPLIFICATOR_SETTINGS = JYML.loadOrExtract(plugin, "/editor/arena/waves/wave_amplificator_settings.yml");

        YML_ARENA_REGION_LIST = JYML.loadOrExtract(plugin, "/editor/arena/region/region_list.yml");
        YML_ARENA_REGION_MAIN = JYML.loadOrExtract(plugin, "/editor/arena/region/region_main.yml");
        YML_ARENA_REGION_WAVE_LIST = JYML.loadOrExtract(plugin, "/editor/arena/region/wave_list.yml");
        YML_ARENA_REGION_WAVE_SETTINGS = JYML.loadOrExtract(plugin, "/editor/arena/region/wave_settings.yml");
        YML_ARENA_REGION_CONTAINER_LIST = JYML.loadOrExtract(plugin, "/editor/arena/region/container_list.yml");
        YML_ARENA_REGION_CONTAINER_SETTINGS = JYML.loadOrExtract(plugin, "/editor/arena/region/container_settings.yml");

        YML_ARENA_REWARD_LIST = JYML.loadOrExtract(plugin, "/editor/arena/reward/list.yml");
        YML_ARENA_REWARD_SETTINGS = JYML.loadOrExtract(plugin, "/editor/arena/reward/settings.yml");

        ARENA_SPOT_LIST = JYML.loadOrExtract(plugin, "/editor/arena/spot/list.yml");
        ARENA_SPOT_MAIN = JYML.loadOrExtract(plugin, "/editor/arena/spot/main.yml");
        ARENA_SPOT_STATE_LIST = JYML.loadOrExtract(plugin, "/editor/arena/spot/state_list.yml");

        YML_ARENA_SHOP_MANAGER = JYML.loadOrExtract(plugin, "/editor/arena/shop/shop_settings.yml");
        YML_ARENA_SHOP_PRODUCT_LIST = JYML.loadOrExtract(plugin, "/editor/arena/shop/product_list.yml");
        YML_ARENA_SHOP_PRODUCT_SETTINGS = JYML.loadOrExtract(plugin, "/editor/arena/shop/product_settings.yml");

        KIT_LIST = JYML.loadOrExtract(plugin, "/editor/kit/list.yml");
        KIT_MAIN = JYML.loadOrExtract(plugin, "/editor/kit/main.yml");

        SETUP_ITEMS = JYML.loadOrExtract(plugin, "/editor/setup_items.yml");

        this.handlers.put(IArenaConfig.class, new HandlerArenaConfig(plugin));
        this.handlers.put(ArenaGameplayManager.class, new HandlerGameplay(plugin));
        this.handlers.put(IArenaGameCommand.class, new HandlerGameplayCommand(plugin));
        this.handlers.put(IArenaRegion.class, new HandlerRegion(plugin));
        this.handlers.put(IArenaRegionContainer.class, new HandlerRegionContainer(plugin));
        this.handlers.put(ArenaRegionManager.class, new HandlerRegionManager(plugin));
        this.handlers.put(IArenaRegionWave.class, new HandlerRegionWave(plugin));
        this.handlers.put(IArenaReward.class, new HandlerReward(plugin));
        this.handlers.put(ArenaShopManager.class, new HandlerShopManager(plugin));
        this.handlers.put(IArenaShopProduct.class, new HandlerShopProduct(plugin));
        this.handlers.put(IArenaSpot.class, new HandlerSpot(plugin));
        this.handlers.put(ArenaSpotManager.class, new HandlerSpotManager(plugin));
        this.handlers.put(IArenaSpotState.class, new HandlerSpotState(plugin));
        this.handlers.put(IArenaWave.class, new HandlerWave(plugin));
        this.handlers.put(IArenaWaveAmplificator.class, new HandlerWaveAmplificator(plugin));
        this.handlers.put(ArenaWaveManager.class, new HandlerWaveManager(plugin));
        this.handlers.put(IArenaWaveMob.class, new HandlerWaveMob(plugin));
        this.handlers.put(IArenaKit.class, new EditorHandlerKit(plugin));
        this.handlers.put(KitManager.class, new EditorHandlerKitManager(plugin));
        this.handlers.put(MobManager.class, new EditorHandlerMobManager(plugin));
        this.handlers.put(ArenaCustomMob.class, new EditorHandlerMob(plugin));
    }

    @Override
    protected void onShutdown() {

    }

    @Override
    protected boolean onType(@NotNull Player player, @NotNull Object object, @NotNull ArenaEditorType type, @NotNull String input) {
        // Arena Main Handlers.
        if (type == ArenaEditorType.ARENA_CREATE) {
            String id = EditorUtils.fineId(input);
            if (plugin.getArenaManager().getArenaById(id) != null) {
                EditorUtils.errorCustom(player, plugin.lang().Editor_Arena_Error_Exist.getMsg());
                return false;
            }

            ArenaConfig arenaConfig = new ArenaConfig(plugin, plugin.getDataFolder() + "/arenas/" + id + "/" + id + ".yml");
            arenaConfig.save();
            plugin.getArenaManager().getArenasMap().put(arenaConfig.getId(), arenaConfig.getArena());
            return true;
        }
        return super.onType(player, object, type, input);
    }
}
