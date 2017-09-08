package com.orkunbulutduman.bantoolsfinal.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.orkunbulutduman.bantoolsfinal.BanToolsFinal;
import com.orkunbulutduman.bantoolsfinal.MysqlManager;

public class BanClear implements CommandExecutor{
	
	BanToolsFinal main;
	MysqlManager mysql;
	
	public BanClear(BanToolsFinal main, MysqlManager mysql) {
		this.main = main;
		this.mysql = mysql;
	}
	
	@Override
    public boolean onCommand(CommandSender sender, Command cmnd, String label, String[] args) {
		if (args.length > 0) {
			main.log(sender.getName() + " issued ban clear command.");
			if (main.isValidName(args[0])) {
				mysql.clearBanRecord(args[0], "ban");
				if (main.getConfig().getBoolean("General.Enable_Login_Listener") && main.getConfig().getBoolean("General.Enable_Listener_Cache")) {
					main.deleteBan(args[0]);
				}
				main.sendMessage(sender, "bancheck.clearban", new String[] {args[0]});
			} else if (main.isValidIP(args[0])) {
				mysql.clearBanRecord(args[0], "ipban");
				if (main.getConfig().getBoolean("General.Enable_Login_Listener") && main.getConfig().getBoolean("General.Enable_Listener_Cache")) {
					main.deleteIPBan(args[0]);
				}
				main.sendMessage(sender, "bancheck.clearipban", new String[] {args[0]});
			} else {
				main.sendMessage(sender, "not_userNameOrIP", new String[] {});
			}
			return true;
		}
		return false;
	}

}
