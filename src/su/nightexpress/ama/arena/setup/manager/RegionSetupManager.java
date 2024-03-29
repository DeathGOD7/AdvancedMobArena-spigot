package su.nightexpress.ama.arena.setup.manager;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.manager.api.task.ITask;
import su.nexmedia.engine.utils.LocUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.arena.region.IArenaRegion;
import su.nightexpress.ama.api.ArenaCuboid;
import su.nightexpress.ama.arena.region.ArenaRegion;
import su.nightexpress.ama.arena.region.ArenaRegionContainer;
import su.nightexpress.ama.arena.setup.ArenaSetupUtils;
import su.nightexpress.ama.arena.setup.SetupItemType;

import java.util.ArrayList;
import java.util.HashSet;

public class RegionSetupManager extends AbstractSetupManager<ArenaRegion> {

    private Location[] cuboidCache;
    private VisualTask visualTask;

    public RegionSetupManager(@NotNull AMA plugin) {
        super(plugin);
    }

    @Override
    protected void onLoad() {
        super.onLoad();

        this.visualTask = new VisualTask();
        this.visualTask.start();
    }

    @Override
    protected void onShutdown() {
        if (this.visualTask != null) {
            this.visualTask.stop();
            this.visualTask = null;
        }
        super.onShutdown();
    }

    @Override
    protected void onSetupStart(@NotNull Player player, @NotNull ArenaRegion region) {
        this.cuboidCache = new Location[2];
        if (!region.getCuboid().isEmpty()) {
            ArenaCuboid cuboid = region.getCuboid();
            this.cuboidCache[0] = cuboid.getLocationMin().clone();
            this.cuboidCache[1] = cuboid.getLocationMax().clone();
        }

        Inventory inventory = player.getInventory();
        inventory.setItem(0, SetupItemType.REGION_CUBOID.getItem());
        inventory.setItem(2, SetupItemType.REGION_SPAWN.getItem());
        inventory.setItem(3, SetupItemType.REGION_SPAWNER.getItem());
        inventory.setItem(5, SetupItemType.REGION_HOLOGRAM.getItem());
        inventory.setItem(6, SetupItemType.REGION_CONTAINER.getItem());
        inventory.setItem(8, SetupItemType.REGION_SAVE.getItem());
    }

    @Override
    protected void onSetupEnd(@NotNull Player player, @NotNull ArenaRegion region) {
        region.getEditor().open(player, 1);

        this.cuboidCache = null;
    }

    @Override
    protected void updateVisuals() {
        if (cuboidCache[0] != null) {
            ArenaSetupUtils.addVisualText(player, "&a« [Cuboid] 1st Corner »", cuboidCache[0]);
            ArenaSetupUtils.addVisualBlock(player, cuboidCache[0]);
        }
        if (cuboidCache[1] != null) {
            ArenaSetupUtils.addVisualText(player, "&a« [Cuboid] 2nd Corner »", cuboidCache[1]);
            ArenaSetupUtils.addVisualBlock(player, cuboidCache[1]);
        }

        ArenaRegion region = this.getObject();
        if (region.getSpawnLocation() != null) {
            ArenaSetupUtils.addVisualText(player, "&a« Spawn Location »", region.getSpawnLocation());
            ArenaSetupUtils.addVisualBlock(player, region.getSpawnLocation());
        }

        region.getMobSpawners().values().forEach(spawner -> {
            ArenaSetupUtils.addVisualText(player, "&c« Mob Spawner »", spawner);
            ArenaSetupUtils.addVisualBlock(player, spawner);
        });

        region.getContainers().forEach(c -> {
            ArenaSetupUtils.addVisualText(player, "&6« Container »", c.getLocation().clone().add(0,-1,0));
        });
    }

    private void updateVisualParticles(@NotNull Player player) {
        if (cuboidCache[0] != null && cuboidCache[1] != null) {
            ArenaSetupUtils.playCuboid(this.cuboidCache);
        }
    }

    @Override
    protected void removeVisuals() {
        ArenaSetupUtils.removeVisuals(this.player);
    }

    @Override
    protected void handleItem(@NotNull PlayerInteractEvent e, @NotNull Player player, @NotNull ArenaRegion region,
                              @NotNull ItemStack item, @NotNull SetupItemType itemType) {

        switch (itemType) {
            case REGION_CUBOID -> {
                Block block = e.getClickedBlock();
                if (block == null || block.isEmpty()) return;

                Location location = block.getLocation();
                IArenaRegion overlap = region.getArenaConfig().getRegionManager().getRegion(location);
                if (overlap != null && !region.getId().equals(overlap.getId())) {
                    plugin.lang().Setup_Region_Cuboid_Error_Overlap.replace(overlap.replacePlaceholders()).send(player);
                    return;
                }

                Action action = e.getAction();
                int pos = action == Action.LEFT_CLICK_BLOCK ? 0 : 1;
                this.cuboidCache[pos] = location;

                plugin.lang().Setup_Region_Cuboid_Set
                        .replace(region.replacePlaceholders())
                        .replace("%corner%", String.valueOf(pos + 1))
                        .send(player);

                if (this.cuboidCache[0] == null || this.cuboidCache[1] == null) return;
                ArenaCuboid cuboidNew = new ArenaCuboid(this.cuboidCache[0], this.cuboidCache[1]);
                region.setCuboid(cuboidNew);

                boolean spawnLost = region.getSpawnLocation() != null && !cuboidNew.contains(region.getSpawnLocation());
                int spawnersLost = (int) region.getMobSpawners().values().stream().filter(loc -> !cuboidNew.contains(loc)).count();
                int contLost = (int) region.getContainers().stream().filter(con -> !cuboidNew.contains(con.getLocation())).count();

                plugin.lang().Setup_Reigon_Cuboid_Preview
                        .replace("%spawn-lost%", plugin.lang().getBool(spawnLost))
                        .replace("%spawners-lost%", String.valueOf(spawnersLost))
                        .replace("%containers-lost%", String.valueOf(contLost))
                        .send(player);
            }
            case REGION_SPAWN -> {
                Location location = player.getLocation();
                if (!region.getCuboid().contains(location)) {
                    plugin.lang().Setup_Region_Error_Outside.send(player);
                    return;
                }
                region.setSpawnLocation(location);
                plugin.lang().Setup_Region_Spawn_Set.replace(region.replacePlaceholders()).send(player);
            }
            case REGION_SPAWNER -> {
                Block block = e.getClickedBlock();
                if (block == null) return;

                Location location = block.getLocation();
                if (!region.getCuboid().contains(location)) {
                    plugin.lang().Setup_Region_Error_Outside.send(player);
                    return;
                }

                Action action = e.getAction();
                if (action == Action.RIGHT_CLICK_BLOCK && region.getMobSpawners().values().remove(location)) {
                    plugin.lang().Setup_Region_Spawner_Remove.replace(region.replacePlaceholders()).send(player);
                    return;
                }
                if (action == Action.LEFT_CLICK_BLOCK && region.addMobSpawner(location)) {
                    plugin.lang().Setup_Region_Spawner_Add.replace(region.replacePlaceholders()).send(player);
                }
            }
            case REGION_HOLOGRAM -> {
                Action action = e.getAction();
                if (action == Action.RIGHT_CLICK_BLOCK) {
                    Block block = e.getClickedBlock();
                    if (block == null) return;

                    Location location = LocUT.getCenter(block.getLocation().clone().add(0,3D,0), false);
                    region.setHologramStateLocation(location);
                    region.updateHologramState();
                    plugin.lang().Setup_Region_Hologram_Changed.replace(region.replacePlaceholders()).send(player);
                }
                else if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
                    region.setHologramStateEnabled(!region.isHologramStateEnabled());
                    plugin.lang().Setup_Region_Hologram_Toggled.replace(region.replacePlaceholders()).send(player);
                }
            }
            case REGION_SAVE -> {
                if (cuboidCache[0] != null && cuboidCache[1] != null) {
                    region.setCuboid(new ArenaCuboid(cuboidCache[0], cuboidCache[1]));
                }
                else {
                    region.setCuboid(ArenaCuboid.empty());
                }
                region.save();
                this.endSetup(player);
            }
            case REGION_CONTAINER -> {
                Block block = e.getClickedBlock();
                if (block == null) return;

                Location location = block.getLocation();
                if (!region.getCuboid().contains(location)) {
                    plugin.lang().Setup_Region_Error_Outside.send(player);
                    return;
                }

                Action action = e.getAction();
                if (action == Action.RIGHT_CLICK_BLOCK) {
                    if (region.getContainers().stream().noneMatch(container -> container.getLocation().equals(location))) {
                        if (!(block.getState() instanceof Chest chest)) {
                            e.setUseItemInHand(Event.Result.ALLOW);
                            e.setUseInteractedBlock(Event.Result.ALLOW);
                            return;
                        }

                        ArenaRegionContainer container = new ArenaRegionContainer(region, chest, new HashSet<>(), 1, 27, new ArrayList<>());
                        region.getContainers().add(container);
                        plugin.lang().Setup_Region_Container_Add.replace(region.replacePlaceholders()).send(player);
                    }
                }
                else if (action == Action.LEFT_CLICK_BLOCK) {
                    if (!region.getContainers().removeIf(container -> container.getLocation().equals(location))) {
                        if (!(block.getState() instanceof Chest)) return;
                        e.setUseInteractedBlock(Event.Result.ALLOW);
                        return;
                    }
                    plugin.lang().Setup_Region_Container_Remove.replace(region.replacePlaceholders()).send(player);
                }
            }
        }
    }

    class VisualTask extends ITask<AMA> {

        public VisualTask() {
            super(plugin(), 10L, true);
        }

        @Override
        public void action() {
            if (player == null) return;
            updateVisualParticles(player);
        }
    }
}
