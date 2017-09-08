package com.orkunbulutduman.bantoolsfinal.commands;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.orkunbulutduman.bantoolsfinal.BanToolsFinal;
import com.orkunbulutduman.bantoolsfinal.MysqlManager;

public class Main implements CommandExecutor{
	
	BanToolsFinal main;
	MysqlManager mysql;
	
	public Main(BanToolsFinal main, MysqlManager mysql) {
		this.main = main;
		this.mysql = mysql;
	}
	
	@Override
    public boolean onCommand(CommandSender sender, Command cmnd, String label, String[] args) {
		if (args.length == 0) {
			sender.sendMessage(ChatColor.DARK_PURPLE + "[BanToolsFinal] " + ChatColor.GREEN + "A plugin devoloped and maintained by orkun1675");
			return true;
		}
		if (args.length == 1 && args[0].equals("reload")) {
			if (sender.hasPermission("bantoolsfinal.reload")) {
				main.reloadConfig();
				mysql.closeConnection();
				if (mysql.openConnection()) {
					if (main.getConfig().getBoolean("General.Enable_Login_Listener") && main.getConfig().getBoolean("General.Enable_Listener_Cache")) {
						main.reloadDataFromMysql();
					}
					main.sendMessage(sender, "reloaded", new String[] {});
				} else {
					main.sendMessage(sender, "cant_reload", new String[] {});
				}
			} else {
				main.sendMessage(sender, "no_permission", new String[] {});
			}
			return true;
		}
		return false;
	}

}
