package com.orkunbulutduman.bantoolsfinal.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.orkunbulutduman.bantoolsfinal.BanToolsFinal;
import com.orkunbulutduman.bantoolsfinal.MysqlManager;

public class UnBanIP implements CommandExecutor{
	
	BanToolsFinal main;
	MysqlManager mysql;
	
	public UnBanIP(BanToolsFinal main, MysqlManager mysql) {
		this.main = main;
		this.mysql = mysql;
	}
	
	@Override
    public boolean onCommand(CommandSender sender, Command cmnd, String label, String[] args) {
		if (args.length > 0) {
			if (!main.isValidIP(args[0])) {
				main.sendMessage(sender, "not_ip", new String[] {});
				return true;
			}
			mysql.deleteBan(args[0], "ipban");
			if (main.getConfig().getBoolean("General.Enable_Login_Listener") && main.getConfig().getBoolean("General.Enable_Listener_Cache")) {
				main.deleteIPBan(args[0]);
			}
			main.sendMessage(sender, "unbanip.complete", new String[] {args[0]});
			main.sendMessageToAuthorities("unbanip.broadcast", new String[] {sender.getName(), args[0]});
			return true;
		}
		return false;
	}

}
