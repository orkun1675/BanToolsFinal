package com.orkunbulutduman.bantoolsfinal.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.orkunbulutduman.bantoolsfinal.BanToolsFinal;
import com.orkunbulutduman.bantoolsfinal.MysqlManager;

public class LockBan implements CommandExecutor {
	
	BanToolsFinal main;
	MysqlManager mysql;
	
	public LockBan(BanToolsFinal main, MysqlManager mysql) {
		this.main = main;
		this.mysql = mysql;
	}
	
	@Override
    public boolean onCommand(CommandSender sender, Command cmnd, String label, String[] args) {
		if (args.length == 1) {
			if (!main.isValidName(args[0])) {
				main.sendMessage(sender, "not_username", new String[] {});
				return true;
			}
			if ((main.getConfig().getBoolean("General.Enable_Login_Listener") && main.getConfig().getBoolean("General.Enable_Listener_Cache") && !main.playerIsBanned(args[0])) || !mysql.isPlayerBanned(args[0])) {
				main.sendMessage(sender, "player_not_banned", new String[] {});
				return true;
			}
			if (mysql.isBanLocked(args[0])) {
				main.sendMessage(sender, "ban_already_locked", new String[] {});
				return true;
			}
			mysql.lockBan(args[0]);
			main.sendMessage(sender, "lockban.inform", new String[] {args[0]});
			return true;
		}
		return false;
	}

}
