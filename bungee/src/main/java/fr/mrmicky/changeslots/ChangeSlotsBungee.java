package fr.mrmicky.changeslots;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;

import com.google.common.io.ByteStreams;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;

public class ChangeSlotsBungee extends Plugin {

	private Configuration config;

	@Override
	public void onEnable() {
		loadConfig();
		getProxy().getPluginManager().registerCommand(this, new CommandSetslots());
		if (config.getBoolean("UpdateSeverPing")) {
			getProxy().getPluginManager().registerListener(this, new ProxyListener());
		}
	}

	private void loadConfig() {
		try {
			if (!getDataFolder().exists()) {
				getDataFolder().mkdir();
			}
			File config = new File(getDataFolder().getPath(), "config.yml");
			if (!config.exists()) {
				try {
					config.createNewFile();
					try (InputStream is = getResourceAsStream("config.yml");
							OutputStream os = new FileOutputStream(config)) {
						ByteStreams.copy(is, os);
					}
				} catch (IOException e) {
					throw new RuntimeException("Unable to create configuration file", e);
				}
			}
			this.config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(config);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void changeSlots(int slots) throws ReflectiveOperationException {
		Field playerLimit = getProxy().getConfig().getClass().getDeclaredField("playerLimit");
		playerLimit.setAccessible(true);
		playerLimit.set(getProxy().getConfig(), slots);
	}

	private BaseComponent[] getConfigString(String key) {
		return TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', config.getString(key)));
	}

	class CommandSetslots extends Command {

		public CommandSetslots() {
			super("setslots", "changeslots.admin", new String[] { "setslot", "changeslots" });
		}

		@Override
		public void execute(CommandSender sender, String[] args) {
			if (args.length == 1) {
				try {
					changeSlots(Integer.valueOf(args[0]));
					sender.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&',
							config.getString("Success").replace("%n", args[0]))));
				} catch (NumberFormatException e1) {
					sender.sendMessage(getConfigString("NoNumber"));
				} catch (Exception e2) {
					sender.sendMessage(getConfigString("Error"));
					e2.printStackTrace();
				}
			} else {
				sender.sendMessage(getConfigString("NoArgument"));
			}
		}
	}

	public class ProxyListener implements Listener {

		@SuppressWarnings("deprecation")
		@EventHandler(priority = 55)
		public void onPing(ProxyPingEvent e) {
			e.getResponse().getPlayers().setMax(getProxy().getConfig().getPlayerLimit());
		}
	}
}
