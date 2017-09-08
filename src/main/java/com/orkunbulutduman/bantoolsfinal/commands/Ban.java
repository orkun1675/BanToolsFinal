package com.orkunbulutduman.bantoolsfinal.commands;

import java.util.Calendar;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.orkunbulutduman.bantoolsfinal.BanToolsFinal;
import com.orkunbulutduman.bantoolsfinal.MysqlManager;
import com.orkunbulutduman.bantoolsfinal.PlayerBanData;

public class Ban implements CommandExecutor{
	
	BanToolsFinal main;
	MysqlManager mysql;
	
	public Ban(BanToolsFinal main, MysqlManager mysql) {
		this.main = main;
		this.mysql = mysql;
	}
	
	@Override
    public boolean onCommand(CommandSender sender, Command cmnd, String label, String[] args) {
		if (args.length > 1) {
			if (!main.isValidName(args[0])) {
				main.sendMessage(sender, "not_username", new String[] {});
				return true;
			}
			if ( (main.getConfig().getBoolean("General.Enable_Login_Listener") && main.getConfig().getBoolean("General.Enable_Listener_Cache") && main.playerIsBanned(args[0])) || mysql.isPlayerBanned(args[0])) {
				main.sendMessage(sender, "player_already_banned", new String[] {});
				return true;
			}
			if (sender.getName().equalsIgnoreCase(args[0])) {
				main.sendMessage(sender, "cannot_ban_yourself", new String[] {});
				return true;
			}
			Player targetPlayer = main.getServer().getPlayer(args[0]);
			if (targetPlayer != null && targetPlayer.hasPermission("bantoolsfinal.protected")) {
				main.sendMessage(sender, "player_protected", new String[] {});
				return true;
			}
			String reason = "";
			for (int i = 1; i < args.length; i++) {
				reason += args[i] + " ";
			}
			reason = reason.substring(0, reason.length() - 1);
			if (!main.isParsableReason(reason)) {
				main.sendMessage(sender, "invalid_text", new String[] {});
				return true;
			}
			mysql.banPlayer(args[0], true, -1, reason, sender.getName());
			main.kickWithMessage(args[0], "ban.kick", new String[] {reason, sender.getName()});
			Calendar farAwayDate = Calendar.getInstance();
			farAwayDate.add(Calendar.YEAR, 150);
			if (main.getConfig().getBoolean("General.Enable_Login_Listener") && main.getConfig().getBoolean("General.Enable_Listener_Cache")) {
				boolean locked = false;
				if (reason.toLowerCase().contains("guvenlik")) {
					locked = true;
				}
				main.addBan(args[0], new PlayerBanData(args[0], true, farAwayDate, reason, sender.getName(), locked));
			}
			main.sendMessage(sender, "ban.inform", new String[] {args[0], reason});
			main.sendMessageToAuthorities("ban.broadcast", new String[] {sender.getName(), args[0], reason});
			return true;
		}
		return false;
	}

}
