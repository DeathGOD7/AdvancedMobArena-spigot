package su.nightexpress.ama.hooks.external;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.economy.Currency;
import com.elmakers.mine.bukkit.api.event.PreLoadEvent;
import com.elmakers.mine.bukkit.api.magic.CasterProperties;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.Messages;

import su.nexmedia.engine.hooks.HookState;
import su.nexmedia.engine.hooks.NHook;
import su.nexmedia.engine.utils.NumberUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.arena.ArenaPlayer;

public class MagicHK extends NHook<AMA> {
	
	public MagicHK(@NotNull AMA plugin) {
		super(plugin);
	}

	@Override
	@NotNull
	protected HookState setup() {
		this.registerListeners();
		
		return HookState.SUCCESS;
	}

	@Override
	protected void shutdown() {
		this.unregisterListeners();
	}

	@EventHandler
	public void onMagicPreLoad(PreLoadEvent e) {
		e.registerCurrency(new ArenaCurrency());
		e.registerCurrency(new ArenaCurrencyScore());
	}
	
	class ArenaCurrency implements Currency {

		public ArenaCurrency() {
			
		}
		
		@Override
		public void deduct(Mage m, CasterProperties caster, double amount) {
			plugin.getEconomy().take(m.getPlayer(), amount);
		}

		@Override
		public String formatAmount(double amount, Messages messages) {
			return NumberUT.format(amount) + " " + plugin.lang().Coins_Format.getMsg();
		}

		@Override
		public double getBalance(Mage mage, CasterProperties caster) {
			Player p = mage.getPlayer();
			return plugin.getEconomy().getBalance(p);
		}

		@Override
		public double getDefaultValue() {
			return 0;
		}

		@Override
		public MaterialAndData getIcon() {
			return null;
		}

		@Override
		public String getKey() {
			return "ama_coins";
		}

		@Override
		public double getMaxValue() {
			return 0;
		}

		@Override
		public String getName(Messages messages) {
			return plugin.lang().Coins_Format.getMsg();
		}

		@Override
		public double getWorth() {
			return 1;
		}

		@Override
		public boolean give(Mage mage, CasterProperties caster, double amount) {
			plugin.getEconomy().add(mage.getPlayer(), amount);
			return true;
		}

		@Override
		public boolean has(Mage mage, CasterProperties caster, double amount) {
			if (!plugin.getArenaManager().isPlaying(mage.getPlayer())) {
				return false;
			}
			return plugin.getEconomy().getBalance(mage.getPlayer()) >= amount;
		}

		@Override
		public boolean hasMaxValue() {
			return false;
		}

		@Override
		public boolean isValid() {
			return true;
		}
	}
	
	class ArenaCurrencyScore implements Currency {

		public ArenaCurrencyScore() {
			
		}
		
		@Override
		public void deduct(Mage m, CasterProperties caster, double amount) {
			ArenaPlayer arenaPlayer = plugin.getArenaManager().getArenaPlayer(m.getPlayer());
			if (arenaPlayer == null) return;
			
			arenaPlayer.addScore((int) -amount);
		}

		@Override
		public String formatAmount(double amount, Messages messages) {
			return NumberUT.format(amount);
		}

		@Override
		public double getBalance(Mage mage, CasterProperties caster) {
			ArenaPlayer arenaPlayer = plugin.getArenaManager().getArenaPlayer(mage.getPlayer());
			if (arenaPlayer == null) return 0;
			
			return arenaPlayer.getScore();
		}

		@Override
		public double getDefaultValue() {
			return 0;
		}

		@Override
		public MaterialAndData getIcon() {
			return null;
		}

		@Override
		public String getKey() {
			return "ama_score";
		}

		@Override
		public double getMaxValue() {
			return 0;
		}

		@Override
		public String getName(Messages messages) {
			return "Score";
		}

		@Override
		public double getWorth() {
			return 1;
		}

		@Override
		public boolean give(Mage mage, CasterProperties caster, double amount) {
			ArenaPlayer arenaPlayer = plugin.getArenaManager().getArenaPlayer(mage.getPlayer());
			if (arenaPlayer == null) return false;
			
			arenaPlayer.addScore((int) amount);
			return true;
		}

		@Override
		public boolean has(Mage mage, CasterProperties caster, double amount) {
			ArenaPlayer arenaPlayer = plugin.getArenaManager().getArenaPlayer(mage.getPlayer());
			if (arenaPlayer == null) return false;
			
			return arenaPlayer.getScore() >= (int) amount;
		}

		@Override
		public boolean hasMaxValue() {
			return false;
		}

		@Override
		public boolean isValid() {
			return true;
		}
	}
}
