package com.orkunbulutduman.bantoolsfinal.commands;

import java.util.ArrayList;
import java.util.Calendar;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.orkunbulutduman.bantoolsfinal.BanToolsFinal;
import com.orkunbulutduman.bantoolsfinal.IPBanData;
import com.orkunbulutduman.bantoolsfinal.MysqlManager;

public class TempBanIP implements CommandExecutor{
	
	BanToolsFinal main;
	MysqlManager mysql;

	public TempBanIP(BanToolsFinal main, MysqlManager mysql) {
		this.main = main;
		this.mysql = mysql;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmnd, String label, String[] args) {
		if (args.length > 2) {
			if (!main.isValidIP(args[0])) {
				main.sendMessage(sender, "not_ip", new String[] {});
				return true;
			}
			if ( (main.getConfig().getBoolean("General.Enable_Login_Listener") && main.getConfig().getBoolean("General.Enable_Listener_Cache") && main.ipIsBanned(args[0])) || mysql.isIPBanned(args[0])) {
				main.sendMessage(sender, "ip_already_banned", new String[] {});
				return true;
			}
			Player playerSender = main.getServer().getPlayer(sender.getName());
			if (playerSender != null && playerSender.getAddress().getHostName().equalsIgnoreCase(args[0])) {
				main.sendMessage(sender, "cannot_ipban_yourself", new String[] {});
				return true;
			}
			ArrayList<Player> players = new ArrayList<>();
			for (Player player : main.getServer().getOnlinePlayers()) {
				if (player != null) {
					String ip = player.getAddress().toString().substring(1);
					main.log("New IP: " + ip);
					if (ip != null && ip.length() > 1) {
						if (ip.equals(args[0])) {
							players.add(player);
						}
					}
				}
			}
			boolean prot = false;
			for (Player playerWithIPMatch : players) {
				if (playerWithIPMatch != null && playerWithIPMatch.hasPermission("bantoolsfinal.protected")) {
					prot = true;
					break;
				}
			}
			if (prot) {
				main.sendMessage(sender, "player_protected", new String[] {});
				return true;
			}
			Calendar calendar;
			try {
				calendar = main.inputToCalendar(args[1]);
			} catch (Exception ex) {
				main.sendMessage(sender, "tempbanip.time_error", new String[] {});
				return true;
			}
			int limitInDays = 0;
			if (sender.hasPermission("bantoolsfinal.tempbanip.extended")) {
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
			mysql.banIP(args[0], false, mysql.dateToEpoch(calendar), reason, sender.getName());
			//main.kickWithMessage(player, "tempbanip.kick", new String[] {reason, main.calendarToString(calendar), main.getRemainingMinutesUntilCalender(calendar) + "",  sender.getName()});
			if (main.getConfig().getBoolean("General.Enable_Login_Listener") && main.getConfig().getBoolean("General.Enable_Listener_Cache")) {
				main.addIPBan(args[0], new IPBanData(args[0], true, calendar, reason, sender.getName()));
			}
			main.sendMessage(sender, "tempbanip.inform", new String[] {args[0], main.calendarToString(calendar), reason});
			main.sendMessageToAuthorities("tempbanip.broadcast", new String[] {sender.getName(), args[0], main.calendarToString(calendar), reason});
			return true;
		}
		return false;
	}

}
