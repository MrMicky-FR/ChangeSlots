package fr.mrmicky.changeslots;

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.function.UnaryOperator;
import java.util.logging.Level;

public final class ChangeSlotsBungee extends Plugin implements Listener {

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

            File configFile = new File(getDataFolder(), "config.yml");
            if (!configFile.exists()) {
                try (InputStream is = getResourceAsStream("config.yml");
                     OutputStream os = new FileOutputStream(configFile)) {
                    ByteStreams.copy(is, os);
                }
            }

            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
        } catch (IOException e) {
            throw new RuntimeException("Unable to create configuration file", e);
        }
    }

    public void changeSlots(int slots) throws ReflectiveOperationException {
        Class<?> configClass = getProxy().getConfig().getClass();

        if (!configClass.getSuperclass().equals(Object.class)) {
             configClass = configClass.getSuperclass();
        }

        Field playerLimitField = configClass.getDeclaredField("playerLimit");
        playerLimitField.setAccessible(true);
        playerLimitField.set(getProxy().getConfig(), slots);
    }

    private BaseComponent[] getConfigString(String key) {
        return getConfigString(key, null);
    }

    private BaseComponent[] getConfigString(String key, UnaryOperator<String> operator) {
        String s = ChatColor.translateAlternateColorCodes('&', config.getString(key));
        return TextComponent.fromLegacyText(operator != null ? operator.apply(s) : s);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPing(ProxyPingEvent event) {
        event.getResponse().getPlayers().setMax(getProxy().getConfig().getPlayerLimit());
    }

    class CommandSetSlots extends Command {

        public CommandSetSlots() {
            super("setslots", "changeslots.admin", "setslot", "changeslots", "gsetslots");
        }

        @Override
        public void execute(CommandSender sender, String[] args) {
            if (args.length == 0) {
                sender.sendMessage(getConfigString("NoArgument"));
                return;
            }

            try {
                changeSlots(Integer.parseInt(args[0]));

                sender.sendMessage(getConfigString("Success", s -> s.replace("%n", args[0])));
            } catch (NumberFormatException e) {
                sender.sendMessage(getConfigString("NoNumber"));
            } catch (ReflectiveOperationException e) {
                sender.sendMessage(getConfigString("Error"));
                getLogger().log(Level.SEVERE, "An error occurred", e);
            }
        }
    }
}
