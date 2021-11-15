package su.nightexpress.ama.nms.v1_15_1;

import net.minecraft.server.v1_15_R1.*;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftTNTPrimed;
import org.bukkit.entity.Entity;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityTargetEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.utils.Reflex;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.ama.nms.PMS;

import java.util.Map;
import java.util.Set;

public class V1_15_R1 implements PMS {
    
	@Override
	public void load() {
	    EntityInjector.setup();
	}

	@Override
    public LivingEntity spawnMob(EntityType type, Location loc) {
		net.minecraft.server.v1_15_R1.EntityInsentient eIns = (EntityInsentient) EntityInjector.spawnEntity(type, loc);
		if (eIns == null) return null;
		
        Entity bukkitEntity = eIns.getBukkitEntity();
        LivingEntity eLiving = (LivingEntity) bukkitEntity;	
        
        boolean isAnimal = eLiving instanceof Animals || eLiving instanceof Fish;
        if (isAnimal) {
        	Set<?> goalSelectorsD = (Set<?>) Reflex.getFieldValue(eIns.goalSelector, "d");
        	Map<?, ?> goalSelectorsC = (Map<?, ?>) Reflex.getFieldValue(eIns.goalSelector, "c");
        	if (goalSelectorsD != null) goalSelectorsD.clear();
        	if (goalSelectorsC != null) goalSelectorsC.clear();
        }
        Set<?> targetSelectorsD = (Set<?>) Reflex.getFieldValue(eIns.targetSelector, "d");
        Map<?, ?> targetSelectorsC = (Map<?, ?>) Reflex.getFieldValue(eIns.targetSelector, "c");
        if (targetSelectorsD != null) targetSelectorsD.clear();
        if (targetSelectorsC != null) targetSelectorsC.clear();
        
        if (eIns.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE) == null) {
            eIns.getAttributeMap().b(GenericAttributes.ATTACK_DAMAGE);
        }
        
        if (!(eIns instanceof EntityCreature)) return eLiving;
        
        if (isAnimal) {
	        eIns.goalSelector.a(0, new PathfinderGoalFloat(eIns));
	        eIns.goalSelector.a(2, new PFAttack((EntityCreature) eIns, true));
        }
        eIns.targetSelector.a(1, new PathfinderGoalHurtByTarget((EntityCreature)eIns, EntityHuman.class));
        eIns.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget<EntityHuman>(eIns, EntityHuman.class, true));
        
        return eLiving;
    }
    
    @Override
    public void setTarget(LivingEntity entity, @Nullable LivingEntity li2) {
		CraftLivingEntity craftLiving = (CraftLivingEntity) entity;
		if (!(craftLiving.getHandle() instanceof EntityInsentient insentient)) return;

        if (li2 == null) {
            insentient.setGoalTarget(null);
            return;
        }
        insentient.setGoalTarget(((CraftLivingEntity)li2).getHandle(), EntityTargetEvent.TargetReason.CUSTOM, true);
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
	    EntityArmorStand entity = new EntityArmorStand(EntityTypes.ARMOR_STAND, world);
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
	    ((CraftPlayer)p).getHandle().playerConnection.sendPacket(spawnEntityLiving);
	    PacketPlayOutEntityMetadata entityMetadata = new PacketPlayOutEntityMetadata(entity.getId(),entity.getDataWatcher(),false);
	    ((CraftPlayer)p).getHandle().playerConnection.sendPacket(entityMetadata);
	    
		return entity.getId();
	}
	
	@Override
	public int visualGlowBlockAdd(@NotNull Player p, @NotNull Location loc) {
		org.bukkit.World w = loc.getWorld();
		if (w == null) return -1;
		
		WorldServer world = ((CraftWorld) w).getHandle();
	    EntityShulker entity = new EntityShulker(EntityTypes.SHULKER, world);
	    entity.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), 0, 0);
	    entity.setHeadRotation(0);
	    entity.setInvisible(true);
	    entity.setInvulnerable(true);
	    entity.setNoGravity(true);
	    entity.setCustomNameVisible(true);
	    entity.setSilent(true);
	    entity.setFlag(6, true); //Glow
	    PacketPlayOutSpawnEntityLiving spawnEntityLiving = new PacketPlayOutSpawnEntityLiving(entity);
	    ((CraftPlayer)p).getHandle().playerConnection.sendPacket(spawnEntityLiving);
	    PacketPlayOutEntityMetadata entityMetadata = new PacketPlayOutEntityMetadata(entity.getId(),entity.getDataWatcher(),false);
	    ((CraftPlayer)p).getHandle().playerConnection.sendPacket(entityMetadata);
	    
		return entity.getId();
	}

	@Override
	public void visualEntityRemove(@NotNull Player p, int... id) {
	    PacketPlayOutEntityDestroy packetPlayOutEntityDestroy = new PacketPlayOutEntityDestroy(id);
	    ((CraftPlayer)p).getHandle().playerConnection.sendPacket(packetPlayOutEntityDestroy);
	    
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
		
		packet.block = nms;
		return packet;
	}
	
	@Override
	public void setTNTSource(@NotNull TNTPrimed tnt, @NotNull Player p) {
		EntityLiving nmsEntityLiving = (EntityLiving)(((CraftLivingEntity) p).getHandle());
		EntityTNTPrimed nmsTNT = (EntityTNTPrimed) (((CraftTNTPrimed) tnt).getHandle());
		Reflex.setFieldValue(nmsTNT, "source", nmsEntityLiving);
		
		/*try {
		    Field sourceField = EntityTNTPrimed.class.getDeclaredField("source");
		    sourceField.setAccessible(true);
		    sourceField.set(nmsTNT, nmsEntityLiving);
		} catch (Exception ex) {
		    ex.printStackTrace();
		}*/
	}
}
