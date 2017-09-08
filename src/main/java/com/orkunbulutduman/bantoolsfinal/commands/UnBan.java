package com.orkunbulutduman.bantoolsfinal.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.orkunbulutduman.bantoolsfinal.BanToolsFinal;
import com.orkunbulutduman.bantoolsfinal.MysqlManager;

public class UnBan implements CommandExecutor{
	
	BanToolsFinal main;
	MysqlManager mysql;
	
	public UnBan(BanToolsFinal main, MysqlManager mysql) {
		this.main = main;
		this.mysql = mysql;
	}
	
	@Override
    public boolean onCommand(CommandSender sender, Command cmnd, String label, String[] args) {
		if (args.length > 0) {
			if (!main.isValidName(args[0])) {
				main.sendMessage(sender, "not_username", new String[] {});
				return true;
			}
			if (mysql.isBanLocked(args[0]) && !sender.hasPermission("bantoolsfinal.unban.locked")) {
				main.sendMessage(sender, "no_permission", new String[] {});
				return true;
			}
			mysql.deleteBan(args[0], "ban");
			if (main.getConfig().getBoolean("General.Enable_Login_Listener") && main.getConfig().getBoolean("General.Enable_Listener_Cache")) {
				main.deleteBan(args[0]);
			}
			main.sendMessage(sender, "unban.complete", new String[] {args[0]});
			main.sendMessageToAuthorities("unban.broadcast", new String[] {sender.getName(), args[0]});
			return true;
		}
		return false;
	}

}
