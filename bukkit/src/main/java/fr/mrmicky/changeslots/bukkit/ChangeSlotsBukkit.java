package fr.mrmicky.changeslots.bukkit;

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

    private Field maxPlayersField;

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
            sender.sendMessage(getMessage("NoPermission"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /setslots <slots>");
            return true;
        }

        try {
            changeSlots(Integer.parseInt(args[0]));

            sender.sendMessage(getMessage("Success").replace("%n", args[0]));
        } catch (NumberFormatException e) {
            sender.sendMessage(getMessage("NoNumber"));
        } catch (ReflectiveOperationException e) {
            sender.sendMessage(getMessage("Error"));

            getLogger().log(Level.SEVERE, "An error occurred while updating max players", e);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }

    private String getMessage(String key) {
        return ChatColor.translateAlternateColorCodes('&', getConfig().getString(key));
    }

    /**
     * Change the max players of the Bukkit server.
     *
     * @param slots the amount of players the server should allow
     * @throws ReflectiveOperationException if an error occurs
     */
    public void changeSlots(int slots) throws ReflectiveOperationException {
        Method serverGetHandle = getServer().getClass().getDeclaredMethod("getHandle");
        Object playerList = serverGetHandle.invoke(getServer());

        if (this.maxPlayersField == null) {
            this.maxPlayersField = getMaxPlayersField(playerList);
        }

        this.maxPlayersField.setInt(playerList, slots);
    }

    private Field getMaxPlayersField(Object playerList) throws ReflectiveOperationException {
        Class<?> playerListClass = playerList.getClass().getSuperclass();

        try {
            Field field = playerListClass.getDeclaredField("maxPlayers");
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            for (Field field : playerListClass.getDeclaredFields()) {
                if (field.getType() != int.class) {
                    continue;
                }

                field.setAccessible(true);

                if (field.getInt(playerList) == getServer().getMaxPlayers()) {
                    return field;
                }
            }

            throw new NoSuchFieldException("Unable to find maxPlayers field in " + playerListClass.getName());
        }
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
            getLogger().log(Level.SEVERE, "An error occurred while updating the server properties", e);
        }
    }
}
