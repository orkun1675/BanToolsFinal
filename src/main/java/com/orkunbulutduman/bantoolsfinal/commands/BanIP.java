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

public class BanIP implements CommandExecutor{

	BanToolsFinal main;
	MysqlManager mysql;

	public BanIP(BanToolsFinal main, MysqlManager mysql) {
		this.main = main;
		this.mysql = mysql;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmnd, String label, String[] args) {
		if (args.length > 1) {
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
			String reason = "";
			for (int i = 1; i < args.length; i++) {
				reason += args[i] + " ";
			}
			reason = reason.substring(0, reason.length() - 1);
			if (!main.isParsableReason(reason)) {
				main.sendMessage(sender, "invalid_text", new String[] {});
				return true;
			}
			mysql.banIP(args[0], true, -1, reason, sender.getName());
			//main.kickWithMessage(player, "ipban.kick", new String[] {reason, sender.getName()});
			Calendar farAwayDate = Calendar.getInstance();
			farAwayDate.add(Calendar.YEAR, 150);
			if (main.getConfig().getBoolean("General.Enable_Login_Listener") && main.getConfig().getBoolean("General.Enable_Listener_Cache")) {
				main.addIPBan(args[0], new IPBanData(args[0], true, farAwayDate, reason, sender.getName()));
			}
			main.sendMessage(sender, "ipban.inform", new String[] {args[0], reason});
			main.sendMessageToAuthorities("ipban.broadcast", new String[] {sender.getName(), args[0], reason});
			return true;
		}
		return false;
	}

}
