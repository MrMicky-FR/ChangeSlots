package fr.mrmicky.changeslots;

import java.lang.reflect.Field;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class ChangeSlotsBukkit extends JavaPlugin {

	@Override
	public void onEnable() {
		saveDefaultConfig();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String arg, String[] args) {
		if (sender.hasPermission("changeslots.admin")) {
			if (args.length == 1) {
				try {
					changeSlots(Integer.valueOf(args[0]));
					sender.sendMessage(getConfigString("Success").replace("%n", args[0]));
				} catch (NumberFormatException e) {
					sender.sendMessage(getConfigString("NoNumber"));
				} catch (Exception e) {
					sender.sendMessage(getConfigString("Error"));
					e.printStackTrace();
				}
			} else {
				sender.sendMessage(getConfigString("NoArgument"));
			}
		} else {
			sender.sendMessage(getConfigString("NoPermission"));
		}
		return true;
	}

	private String getConfigString(String key) {
		return ChatColor.translateAlternateColorCodes('&', getConfig().getString(key));
	}

	public void changeSlots(int slots) throws ReflectiveOperationException {
		Object playerList = getServer().getClass().getDeclaredMethod("getHandle").invoke(getServer());
		Field maxPlayers = playerList.getClass().getSuperclass().getDeclaredField("maxPlayers");
		maxPlayers.setAccessible(true);
		maxPlayers.set(playerList, slots);
	}
}
