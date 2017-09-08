package com.orkunbulutduman.bantoolsfinal.commands;

import java.util.Calendar;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.orkunbulutduman.bantoolsfinal.BanToolsFinal;
import com.orkunbulutduman.bantoolsfinal.MysqlManager;
import com.orkunbulutduman.bantoolsfinal.PlayerBanData;

public class TempBan implements CommandExecutor{

	BanToolsFinal main;
	MysqlManager mysql;

	public TempBan(BanToolsFinal main, MysqlManager mysql) {
		this.main = main;
		this.mysql = mysql;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmnd, String label, String[] args) {
		if (args.length > 2) {
			if (!main.isValidName(args[0])) {
				main.sendMessage(sender, "not_username", new String[] {});
				return true;
			}
			if ( (main.getConfig().getBoolean("General.Enable_Login_Listener") && main.getConfig().getBoolean("General.Enable_Listener_Cache") && main.playerIsBanned(args[0])) || mysql.isPlayerBanned(args[0])){
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
			Calendar calendar;
			try {
				calendar = main.inputToCalendar(args[1]);
			} catch (Exception ex) {
				main.sendMessage(sender, "tempban.time_error", new String[] {});
				return true;
			}
			int limitInDays = 0;
			if (sender.hasPermission("bantoolsfinal.tempban.extended")) {
				limitInDays = main.getConfig().getInt("General.Max_Temp_Ban_Duration_In_Days_Extended");
			} else {
				limitInDays = main.getConfig().getInt("General.Max_Temp_Ban_Duration_In_Days_Limited");
			}
			if (limitInDays > 0) {
				Calendar limitDate = Calendar.getInstance();
				limitDate.add(Calendar.DAY_OF_MONTH, limitInDays);
				if (calendar.compareTo(limitDate) > 0) {
					main.sendMessage(sender, "max_time_reached", new String[] {limitInDays + ""});
					return true;
				}
			}
			String reason = "";
			for (int i = 2; i < args.length; i++) {
				reason += args[i] + " ";
			}
			reason = reason.substring(0, reason.length() - 1);
			if (!main.isParsableReason(reason)) {
				main.sendMessage(sender, "invalid_text", new String[] {});
				return true;
			}
			mysql.banPlayer(args[0], false, mysql.dateToEpoch(calendar), reason, sender.getName());
			main.kickWithMessage(args[0], "tempban.kick", new String[] {reason, main.calendarToString(calendar), main.getRemainingMinutesUntilCalender(calendar) + "", sender.getName()});
			if (main.getConfig().getBoolean("General.Enable_Login_Listener") && main.getConfig().getBoolean("General.Enable_Listener_Cache")) {
				boolean locked = false;
				if (reason.toLowerCase().contains("guvenlik")) {
					locked = true;
				}
				main.addBan(args[0], new PlayerBanData(args[0], true, calendar, reason, sender.getName(), locked));
			}
			main.sendMessage(sender, "tempban.inform", new String[] {args[0], main.calendarToString(calendar), reason});
			main.sendMessageToAuthorities("tempban.broadcast", new String[] {sender.getName(), args[0], main.calendarToString(calendar), reason});
			return true;
		}
		return false;
	}

}
