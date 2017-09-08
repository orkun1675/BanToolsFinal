package com.orkunbulutduman.bantoolsfinal.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.orkunbulutduman.bantoolsfinal.BanToolsFinal;
import com.orkunbulutduman.bantoolsfinal.MysqlManager;

public class Warn implements CommandExecutor{
	
	BanToolsFinal main;
	MysqlManager mysql;
	
	public Warn(BanToolsFinal main, MysqlManager mysql) {
		this.main = main;
		this.mysql = mysql;
	}
	
	@Override
    public boolean onCommand(CommandSender sender, Command cmnd, String label, String[] args) {
		if (args.length > 1) {
			Player player = main.getServer().getPlayer(args[0]);
			if (!main.getConfig().getBoolean("General.Enable_Bungee") && (player == null || !player.isOnline()) ) {
				main.sendMessage(sender, "warn.player_not_found", new String[] {args[0]});
				return true;
			}
			if (sender.getName().equalsIgnoreCase(args[0])) {
				main.sendMessage(sender, "cannot_warn_yourself", new String[] {});
				return true;
			}
			if (!main.getConfig().getBoolean("General.Enable_Bungee") && player.hasPermission("bantoolsfinal.protected")) {
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
			mysql.addCount(args[0], "warn");
			if (player == null) {
				main.sendDirectMessageToBungeePlayer(args[0], "warn.warn", new String[] {sender.getName(), reason});
			} else {
				main.sendMessage(player, "warn.warn", new String[] {sender.getName(), reason});
			}
			main.sendMessage(sender, "warn.inform", new String[] {args[0], reason});
			main.sendMessageToAuthorities("warn.broadcast", new String[] {sender.getName(), args[0], reason});
			return true;
		}
		return false;
	}

}
