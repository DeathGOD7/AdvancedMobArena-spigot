package su.nightexpress.ama.mobs;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.NumberUT;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.ama.arena.ArenaPlayer;

import java.util.Set;

public class ArenaMobHealthBar {

	private final String   title;
	private final BarStyle style;
	private final BarColor color;
	
	public ArenaMobHealthBar(
			@NotNull String title,
			@NotNull BarStyle style,
			@NotNull BarColor color
			) {
		this.title = StringUT.color(title);
		this.style = style;
		this.color = color;
	}
	
	@NotNull
	public String getTitle() {
		return this.title;
	}
	
	@NotNull
	public BarStyle getStyle() {
		return this.style;
	}
	
	@NotNull
	public BarColor getColor() {
		return this.color;
	}
	
	@NotNull
	private String replaceTitle(@NotNull String str, @NotNull LivingEntity boss) {
		return str
				.replace("%hp%", NumberUT.format(boss.getHealth()))
				.replace("%maxhp%", NumberUT.format(getMaxHealth(boss)))
				.replace("%name%", boss.getCustomName())
				;
	}
	
	public void create(@NotNull Set<ArenaPlayer> players, @NotNull LivingEntity boss) {
		String title = this.replaceTitle(this.getTitle(), boss);
		
		BossBar bar = Bukkit.getServer().createBossBar(title, this.color, this.style, BarFlag.DARKEN_SKY);
		bar.setProgress(1D);
		bar.setVisible(true);
		
		players.forEach(arenaPlayer -> arenaPlayer.addMobHealthBar(boss, bar));
	}
	
	public void update(@NotNull Set<ArenaPlayer> players, @NotNull LivingEntity boss) {
		String title = this.replaceTitle(this.getTitle(), boss);
        double percent = Math.max(0D, Math.min(1D, boss.getHealth() / this.getMaxHealth(boss)));
        
        for (ArenaPlayer arenaPlayer : players) {
        	BossBar bar = arenaPlayer.getMobHealthBar(boss.getUniqueId());
        	if (bar == null) continue;
        	
        	bar.setTitle(title);
        	bar.setProgress(percent);
        }
	}
	
	public void remove(@NotNull Set<ArenaPlayer> players, @NotNull LivingEntity boss) {
		players.forEach(arenaPlayer -> arenaPlayer.removeMobHealthBar(boss));
	}
	
	private double getMaxHealth(@NotNull LivingEntity entity) {
		AttributeInstance aInstance = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
		if (aInstance == null) return entity.getHealth();
		
		return aInstance.getValue();
	}
}
