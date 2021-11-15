package su.nightexpress.ama.economy;

import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.hooks.external.VaultHK;
import su.nexmedia.engine.manager.api.Loadable;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.config.Config;
import su.nightexpress.ama.economy.object.InternalEco;
import su.nightexpress.ama.economy.object.VaultEco;

public class EconomyManager implements Loadable {

	private AMA plugin;
	private IEconomy economy;
	
	public EconomyManager(@NotNull AMA plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public void setup() {
		VaultHK vault = plugin.getVault();
		
		if (Config.GEN_VAULT_CURRENCY && vault != null && vault.getEconomy() != null) {
			this.economy = new VaultEco(this.plugin, vault);
		}
		else {
			this.economy = new InternalEco(this.plugin);
		}
		this.plugin.info("Economy plugin: " + this.economy.getName());
	}
	
	@Override
	public void shutdown() {
		this.economy = null;
	}
	
	@NotNull
	public IEconomy getEconomy() {
		return this.economy;
	}
}
