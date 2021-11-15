package su.nightexpress.ama.nms.v1_15_1;

import java.util.EnumSet;

import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.Entity;
import net.minecraft.server.v1_15_R1.EntityCreature;
import net.minecraft.server.v1_15_R1.EntityLiving;
import net.minecraft.server.v1_15_R1.EnumHand;
import net.minecraft.server.v1_15_R1.PathEntity;
import net.minecraft.server.v1_15_R1.PathfinderGoal;

public class PFAttack extends PathfinderGoal {
	
    protected final EntityCreature a;
    protected int b;
    private final double d;
    private final boolean e;
    private PathEntity f;
    private int g;
    private double h;
    private double i;
    private double j;
    protected final int c = 20;
    private long k;
    
    public PFAttack(final EntityCreature var0, final boolean var3) {
        this.a = var0;
        this.d = 1D;
        this.e = var3;
        this.a(EnumSet.of(PathfinderGoal.Type.MOVE, PathfinderGoal.Type.LOOK));
    }
    
    @Override
	public boolean a() {
        final long var0 = this.a.world.getTime();
        if (var0 - this.k < 20L) {
            return false;
        }
        this.k = var0;
        final EntityLiving var2 = this.a.getGoalTarget();
        if (var2 == null) {
            return false;
        }
        if (!var2.isAlive()) {
            return false;
        }
        this.f = this.a.getNavigation().a((Entity)var2, 0);
        return this.f != null || this.a(var2) >= this.a.g(var2.locX(), var2.locY(), var2.locZ());
    }
    
    @Override
	public boolean b() {
        final EntityLiving var0 = this.a.getGoalTarget();
        if (var0 == null) {
            return false;
        }
        if (!var0.isAlive()) {
            return false;
        }
        if (!this.e) {
            return !this.a.getNavigation().m();
        }
        return this.a.a(new BlockPosition((Entity)var0));
    }
    
    @Override
	public void c() {
        this.a.getNavigation().a(this.f, this.d);
        this.a.q(true);
        this.g = 0;
    }
    
    @Override
	public void d() {
        //final EntityLiving var0 = this.a.getGoalTarget();
        //if (!IEntitySelector.e.test(var0)) {
        //    this.a.setGoalTarget((EntityLiving)null);
        //}
        this.a.q(false);
        this.a.getNavigation().o();
    }
    
    @Override
	public void e() {
        final EntityLiving var0 = this.a.getGoalTarget();
        this.a.getControllerLook().a((Entity)var0, 30.0f, 30.0f);
        final double var2 = this.a.g(var0.locX(), var0.locY(), var0.locZ());
        --this.g;
        if ((this.e || this.a.getEntitySenses().a((Entity)var0)) && this.g <= 0 && ((this.h == 0.0 && this.i == 0.0 && this.j == 0.0) || var0.g(this.h, this.i, this.j) >= 1.0 || this.a.getRandom().nextFloat() < 0.05f)) {
            this.h = var0.locX();
            this.i = var0.locY();
            this.j = var0.locZ();
            this.g = 4 + this.a.getRandom().nextInt(7);
            if (var2 > 1024.0) {
                this.g += 10;
            }
            else if (var2 > 256.0) {
                this.g += 5;
            }
            if (!this.a.getNavigation().a((Entity)var0, this.d)) {
                this.g += 15;
            }
        }
        this.b = Math.max(this.b - 1, 0);
        this.a(var0, var2);
    }
    
    protected void a(final EntityLiving var0, final double var1) {
        final double var2 = this.a(var0);
        if (var1 <= var2 && this.b <= 0) {
            this.b = 20;
            this.a.a(EnumHand.MAIN_HAND);
            this.a.B((Entity)var0);
        }
    }
    
    protected double a(final EntityLiving var0) {
        return this.a.getWidth() * 2.0f * (this.a.getWidth() * 2.0f) + var0.getWidth();
    }
}
