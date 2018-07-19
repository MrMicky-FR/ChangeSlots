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
import net.md_5.bungee.event.EventPriority;

public class ChangeSlotsBungee extends Plugin implements Listener {

	private Configuration config;

	@Override
	public void onEnable() {
		loadConfig();

		getProxy().getPluginManager().registerCommand(this, new CommandSetSlots());

		if (config.getBoolean("UpdateServerPing")) {
			getProxy().getPluginManager().registerListener(this, this);
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
				} catch (IOException exception) {
					throw new RuntimeException("Unable to create configuration file", exception);
				}
			}

			this.config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(config);
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}

	public void changeSlots(int slots) throws ReflectiveOperationException {
		Field playerLimitField = getProxy().getConfig().getClass().getDeclaredField("playerLimit");
		playerLimitField.setAccessible(true);
		playerLimitField.set(getProxy().getConfig(), slots);
	}

	private BaseComponent[] getConfigString(String key) {
		return TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', config.getString(key)));
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPing(ProxyPingEvent event) {
		event.getResponse().getPlayers().setMax(getProxy().getConfig().getPlayerLimit());
	}

	class CommandSetSlots extends Command {

		public CommandSetSlots() {
			super("setslots", "changeslots.admin", "setslot", "changeslots");
		}

		@Override
		public void execute(CommandSender sender, String[] args) {
			if (args.length < 1) {
				sender.sendMessage(getConfigString("NoArgument"));
				return;
			}

			try {
				changeSlots(Integer.parseInt(args[0]));

				sender.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&',
						config.getString("Success").replace("%n", args[0]))));
			} catch (NumberFormatException numberFormatException) {
				sender.sendMessage(getConfigString("NoNumber"));
			} catch (ReflectiveOperationException fieldException) {
				sender.sendMessage(getConfigString("Error"));
				fieldException.printStackTrace();
			}
		}
	}
}
