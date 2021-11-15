package su.nightexpress.ama.nms.v1_17_R1;

import net.minecraft.core.BlockPosition;
import net.minecraft.network.protocol.game.PacketPlayOutBlockChange;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.EntityCreature;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.AttributeBase;
import net.minecraft.world.entity.ai.attributes.AttributeModifiable;
import net.minecraft.world.entity.ai.attributes.AttributeProvider;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.entity.ai.goal.PathfinderGoalFloat;
import net.minecraft.world.entity.ai.goal.target.PathfinderGoalHurtByTarget;
import net.minecraft.world.entity.ai.goal.target.PathfinderGoalNearestAttackableTarget;
import net.minecraft.world.entity.decoration.EntityArmorStand;
import net.minecraft.world.entity.item.EntityTNTPrimed;
import net.minecraft.world.entity.monster.EntityShulker;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.block.state.IBlockData;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftTNTPrimed;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityTargetEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.utils.Reflex;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.ama.nms.PMS;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class V1_17_R1 implements PMS {
    
    @Override
    public void load() {
        EntityInjector.setup();
    }

    @Override
    public LivingEntity spawnMob(@NotNull EntityType type, @NotNull Location loc) {
        EntityInsentient eIns = (EntityInsentient) EntityInjector.spawnEntity(type, loc);
        if (eIns == null) return null;
        
        Entity bukkitEntity = eIns.getBukkitEntity();
        LivingEntity eLiving = (LivingEntity) bukkitEntity; 
        
        boolean isAnimal = eLiving instanceof Animals || eLiving instanceof Fish;
        if (isAnimal) {
            Set<?> goalSelectorsD = (Set<?>) Reflex.getFieldValue(eIns.bP, "d");
            Map<?, ?> goalSelectorsC = (Map<?, ?>) Reflex.getFieldValue(eIns.bP, "c");
            if (goalSelectorsD != null) goalSelectorsD.clear();
            if (goalSelectorsC != null) goalSelectorsC.clear();
        }
        Set<?> targetSelectorsD = (Set<?>) Reflex.getFieldValue(eIns.bQ, "d");
        Map<?, ?> targetSelectorsC = (Map<?, ?>) Reflex.getFieldValue(eIns.bQ, "c");
        if (targetSelectorsD != null) targetSelectorsD.clear();
        if (targetSelectorsC != null) targetSelectorsC.clear();
        
        this.setAttribute(eIns, GenericAttributes.f, 1D);
        
        if (!(eIns instanceof EntityCreature)) return eLiving;
        
        if (isAnimal) {
            eIns.bP.a(0, new PathfinderGoalFloat(eIns));
            eIns.bP.a(2, new PathfinderAttack(eIns));
        }
        eIns.bQ.a(1, new PathfinderGoalHurtByTarget((EntityCreature)eIns, EntityHuman.class));
        eIns.bQ.a(2, new PathfinderGoalNearestAttackableTarget<EntityHuman>(eIns, EntityHuman.class, true));
        
        return eLiving;
    }
    
    private void setAttribute(@NotNull EntityLiving handle, @NotNull AttributeBase att, double value) {
        if (handle.getAttributeInstance(att) == null) {
            handle.getAttributeMap().a(att);
            
            // Hardcode to register missing entity's attributes.
            AttributeProvider provider = (AttributeProvider) Reflex.getFieldValue(handle.getAttributeMap(), "d");
            if (provider == null) return;
            
            @SuppressWarnings("unchecked")
            Map<AttributeBase, AttributeModifiable> aMap = (Map<AttributeBase, AttributeModifiable>) Reflex.getFieldValue(provider, "a");
            if (aMap == null) return;
            
            Map<AttributeBase, AttributeModifiable> aMap2 = new HashMap<>();
            aMap2.putAll(aMap);
            aMap2.put(att, new AttributeModifiable(att, var1 -> {
                
            }));
            Reflex.setFieldValue(provider, "a", aMap2);
            
            // Update attributes in provider.
            //handle.getAttributeMap().c(att);
            //handle.getAttributeMap().d(att);
        }

        handle.getAttributeInstance(att).setValue(value);
    }
    
    @Override
    public void setTarget(@NotNull LivingEntity entity, @Nullable LivingEntity target) {
        CraftLivingEntity craftLiving = (CraftLivingEntity) entity;
        if (!(craftLiving.getHandle() instanceof EntityInsentient insentient)) return;

        if (target == null) {
            insentient.setGoalTarget(null);
            return;
        }
        insentient.setGoalTarget(((CraftLivingEntity)target).getHandle(), EntityTargetEvent.TargetReason.CUSTOM, true);
    }
    
    @Override
    public LivingEntity getTarget(LivingEntity entity) {
        CraftLivingEntity craftLiving = (CraftLivingEntity) entity;
        if (!(craftLiving.getHandle() instanceof EntityInsentient insentient)) return null;

        EntityLiving target = insentient.getGoalTarget();
        return target == null ? null : (LivingEntity) target.getBukkitEntity();
    }

    // 
    
    @Override
    public int visualEntityAdd(@NotNull Player p, @NotNull String name, @NotNull Location loc) {
        org.bukkit.World w = loc.getWorld();
        if (w == null) return -1;
        
        WorldServer world = ((CraftWorld) w).getHandle();
        EntityArmorStand entity = new EntityArmorStand(EntityTypes.c, world);
        entity.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), 0, 0);
        entity.setHeadRotation(0);
        entity.setInvisible(true);
        entity.setInvulnerable(true);
        entity.getBukkitEntity().setCustomName(StringUT.color(name));
        entity.setSmall(true);
        entity.setNoGravity(true);
        entity.setCustomNameVisible(true);
        entity.setSilent(true);
        PacketPlayOutSpawnEntityLiving spawnEntityLiving = new PacketPlayOutSpawnEntityLiving(entity);
        ((CraftPlayer)p).getHandle().b.sendPacket(spawnEntityLiving);
        PacketPlayOutEntityMetadata entityMetadata = new PacketPlayOutEntityMetadata(entity.getId(),entity.getDataWatcher(),false);
        ((CraftPlayer)p).getHandle().b.sendPacket(entityMetadata);
        
        return entity.getId();
    }
    
    @Override
    public int visualGlowBlockAdd(@NotNull Player p, @NotNull Location loc) {
        org.bukkit.World w = loc.getWorld();
        if (w == null) return -1;
        
        WorldServer world = ((CraftWorld) w).getHandle();
        EntityShulker entity = new EntityShulker(EntityTypes.ay, world);
        entity.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), 0, 0);
        entity.setHeadRotation(0);
        entity.setInvisible(true);
        entity.setInvulnerable(true);
        entity.setNoGravity(true);
        entity.setCustomNameVisible(true);
        entity.setSilent(true);
        entity.setFlag(6, true); //Glow
        PacketPlayOutSpawnEntityLiving spawnEntityLiving = new PacketPlayOutSpawnEntityLiving(entity);
        ((CraftPlayer)p).getHandle().b.sendPacket(spawnEntityLiving);
        PacketPlayOutEntityMetadata entityMetadata = new PacketPlayOutEntityMetadata(entity.getId(),entity.getDataWatcher(),false);
        ((CraftPlayer)p).getHandle().b.sendPacket(entityMetadata);
        
        return entity.getId();
    }

    @Override
    public void visualEntityRemove(@NotNull Player p, int... ids) {
        for (int id : ids) {
            PacketPlayOutEntityDestroy packetPlayOutEntityDestroy = new PacketPlayOutEntityDestroy(id);
            ((CraftPlayer)p).getHandle().b.sendPacket(packetPlayOutEntityDestroy);
        }
        //PacketPlayOutEntityTeleport packet = new PacketPlayOutEntityTeleport(((CraftWorld) p.getWorld()).getHandle().getEntity(id[0]));
        //((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
    }
    
    //
    
    @Override
    @NotNull
    public Object getChangeBlockPacket(@NotNull BlockData hitBlock, @NotNull Location loc) {
        org.bukkit.World bukkitWorld = loc.getWorld();
        if (bukkitWorld == null) throw new IllegalStateException("World can not be null!");
        
        BlockPosition bPos = new BlockPosition(loc.getX(), loc.getY(), loc.getZ());
        WorldServer nmsWorld = ((CraftWorld) bukkitWorld).getHandle();
        
        PacketPlayOutBlockChange packet = new PacketPlayOutBlockChange(nmsWorld, bPos);
        //IBlockData data = Rnd.nextBoolean() ? Blocks.SNOW_BLOCK.getBlockData() : Blocks.ICE.getBlockData();//net.minecraft.server.v1_15_R1.Block.REGISTRY_ID.fromId(80);

        CraftBlockData cb = (CraftBlockData) hitBlock;
        IBlockData nms = cb.getState();
        
        Reflex.setFieldValue(packet, "b", nms);
        return packet;
    }
    
    @Override
    public void setTNTSource(@NotNull TNTPrimed tnt, @NotNull Player p) {
        EntityLiving nmsEntityLiving = (EntityLiving)(((CraftLivingEntity) p).getHandle());
        EntityTNTPrimed nmsTNT = (EntityTNTPrimed) (((CraftTNTPrimed) tnt).getHandle());
        Reflex.setFieldValue(nmsTNT, "source", nmsEntityLiving);
    }
}
