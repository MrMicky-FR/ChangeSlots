package fr.mrmicky.changeslots;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

public final class ChangeSlotsBukkit extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        if (getConfig().getBoolean("SaveOnRestart")) {
            updateServerProperties();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("changeslots.admin")) {
            sender.sendMessage(getConfigString("NoPermission"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(getConfigString("NoArgument"));
            return true;
        }

        try {
            changeSlots(Integer.parseInt(args[0]));

            sender.sendMessage(getConfigString("Success").replace("%n", args[0]));
        } catch (NumberFormatException e) {
            sender.sendMessage(getConfigString("NoNumber"));
        } catch (ReflectiveOperationException e) {
            sender.sendMessage(getConfigString("Error"));

            getLogger().log(Level.SEVERE, "An error occurred while updating max players", e);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }

    private String getConfigString(String key) {
        return ChatColor.translateAlternateColorCodes('&', getConfig().getString(key));
    }

    private void changeSlots(int slots) throws ReflectiveOperationException {
        Method serverGetHandle = getServer().getClass().getDeclaredMethod("getHandle");

        Object playerList = serverGetHandle.invoke(getServer());
        Field maxPlayersField = playerList.getClass().getSuperclass().getDeclaredField("maxPlayers");

        maxPlayersField.setAccessible(true);
        maxPlayersField.set(playerList, slots);
    }

    private void updateServerProperties() {
        Properties properties = new Properties();
        File propertiesFile = new File("server.properties");

        try {
            try (InputStream is = new FileInputStream(propertiesFile)) {
                properties.load(is);
            }

            String maxPlayers = Integer.toString(getServer().getMaxPlayers());

            if (properties.getProperty("max-players").equals(maxPlayers)) {
                return;
            }

            getLogger().info("Saving max players to server.properties...");
            properties.setProperty("max-players", maxPlayers);

            try (OutputStream os = new FileOutputStream(propertiesFile)) {
                properties.store(os, "Minecraft server properties");
            }
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Error while saving max players in server properties", e);
        }
    }
}
