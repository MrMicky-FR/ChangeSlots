package fr.mrmicky.changeslots;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public final class ChangeSlotsBukkit extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
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
            getLogger().log(Level.SEVERE, "An error occurred", e);
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

    public void changeSlots(int slots) throws ReflectiveOperationException {
        Object playerList = getServer().getClass().getDeclaredMethod("getHandle").invoke(getServer());
        Field maxPlayersField = playerList.getClass().getSuperclass().getDeclaredField("maxPlayers");
        maxPlayersField.setAccessible(true);
        maxPlayersField.set(playerList, slots);
    }
}
