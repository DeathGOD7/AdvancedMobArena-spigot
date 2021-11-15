package su.nightexpress.ama.nms.v1_17_R1;

import java.util.EnumSet;

import org.jetbrains.annotations.NotNull;

import net.minecraft.core.BlockPosition;
import net.minecraft.world.EnumHand;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.ai.goal.PathfinderGoal;
import net.minecraft.world.level.pathfinder.PathEntity;


public class PathfinderAttack extends PathfinderGoal {
    
    protected EntityInsentient entity;
    protected int atkCooldown;
    private double speed;
    private PathEntity path;
    private int ticksNextPathCalc;
    private double pathX;
    private double pathY;
    private double pathZ;
    
    public PathfinderAttack(@NotNull EntityInsentient entity) {
        this.entity = entity;
        this.speed = 1D;
        this.a(EnumSet.of(PathfinderGoal.Type.a, PathfinderGoal.Type.b));
    }
    
    // .start
    @Override
    public void c() {
        this.entity.getNavigation().a(this.path, this.speed);
        this.entity.setAggressive(true);
        this.ticksNextPathCalc = 0;
    }
    
    // .stop
    @Override
    public void d() {
        this.entity.setAggressive(false);
        this.entity.getNavigation().o();
    }
    
    // .canUse
    @Override
    public boolean a() {
        EntityLiving target = this.entity.getGoalTarget();
        if (target == null) {
            return false;
        }
        if (!target.isAlive()) {
            return false;
        }
        this.path = this.entity.getNavigation().a(target, 0);
        return this.path != null || this.getMinAttackRange(target) >= this.entity.h(target.locX(), target.locY(), target.locZ());
    }

    // .canContinueToUse
    @Override
    public boolean b() {
        EntityLiving target = this.entity.getGoalTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        return this.entity.a(new BlockPosition(target.locX(), target.locY(), target.locZ()));
    }

    // .tick
    @Override
    public void e() {
        EntityLiving target = this.entity.getGoalTarget();
        this.entity.getControllerLook().a(target, 30.0f, 30.0f);
        double distance = this.entity.h(target.locX(), target.locY(), target.locZ());
        --this.ticksNextPathCalc;
        
        boolean isNoPath = (this.pathX == 0 && this.pathY == 0 && this.pathZ == 0);
        boolean isDistOne = target.h(this.pathX, this.pathY, this.pathZ) >= 1;
        boolean isRandom = this.entity.getRandom().nextFloat() < 0.05f;
        
        if (this.ticksNextPathCalc <= 0 && (isNoPath || isDistOne || isRandom)) {
            this.pathX = target.locX();
            this.pathY = target.locY();
            this.pathZ = target.locZ();
            this.ticksNextPathCalc = 4 + this.entity.getRandom().nextInt(7);
            if (distance > 1024.0) {
                this.ticksNextPathCalc += 10;
            }
            else if (distance > 256.0) {
                this.ticksNextPathCalc += 5;
            }
            if (!this.entity.getNavigation().a(target, this.speed)) {
                this.ticksNextPathCalc += 15;
            }
        }
        this.atkCooldown = Math.max(this.atkCooldown - 1, 0);
        this.attack(target, distance);
    }
    
    protected void attack(EntityLiving target, double distance) {
        double atkRange = this.getMinAttackRange(target);
        if (distance <= atkRange && this.atkCooldown <= 0) {
            this.atkCooldown = 20;
            this.entity.b(EnumHand.a);
            this.entity.attackEntity(target);
        }
    }
    
    protected double getMinAttackRange(EntityLiving target) {
        return this.entity.getWidth() * 2.0f * (this.entity.getWidth() * 2.0f) + target.getWidth();
    }
}
