package com.orkunbulutduman.bantoolsfinal.commands;

import java.util.Calendar;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.orkunbulutduman.bantoolsfinal.BanToolsFinal;
import com.orkunbulutduman.bantoolsfinal.IPBanData;
import com.orkunbulutduman.bantoolsfinal.MysqlManager;
import com.orkunbulutduman.bantoolsfinal.PlayerBanData;

public class BanCheck implements CommandExecutor{
	
	BanToolsFinal main;
	MysqlManager mysql;
	
	public BanCheck(BanToolsFinal main, MysqlManager mysql) {
		this.main = main;
		this.mysql = mysql;
	}
	
	@Override
    public boolean onCommand(CommandSender sender, Command cmnd, String label, String[] args) {
		if (args.length > 0) {
			main.log(sender.getName() + " issued ban check command.");
			if (main.isValidName(args[0])) {
				PlayerBanData data = mysql.getPlayerBanData(args[0]);
				if (data != null && data.getBanStatus() && data.getBanExpiration().compareTo(Calendar.getInstance()) <= 0) {
					mysql.deleteBan(args[0], "ban");
					if (main.getConfig().getBoolean("General.Enable_Login_Listener") && main.getConfig().getBoolean("General.Enable_Listener_Cache")) {
						main.deleteBan(args[0]);
					}
					data = mysql.getPlayerBanData(args[0]);
				}
				if (data == null) {
					main.sendMessage(sender, "bancheck.error", new String[] {});
					return true;
				}
				main.sendMessage(sender, "bancheck.line1.player", new String[] {data.getPlayerName()});
				main.sendMessage(sender, "bancheck.line2", new String[] {main.getStringInSelectedLanguage(data.getBanStatus() + "")});
				if (data.getBanStatus()) {
					main.sendMessage(sender, "bancheck.line3", new String[] {data.getBanReason()});
					Calendar nowDate = Calendar.getInstance();
					Calendar expirationDate = data.getBanExpiration();
					if ((expirationDate.get(Calendar.YEAR) - 100) > nowDate.get(Calendar.YEAR)) {
						main.sendMessage(sender, "bancheck.line4", new String[] {main.getStringInSelectedLanguage("never")});
					} else {
						main.sendMessage(sender, "bancheck.line4", new String[] {main.getRemainingMinutesUntilCalender(data.getBanExpiration())});
					}
					main.sendMessage(sender, "bancheck.line5", new String[] {data.getBanner()});
					main.sendMessage(sender, "bancheck.line6", new String[] {main.calendarToString(data.getBanDate())});
					main.sendMessage(sender, "bancheck.line7", new String[] {main.getStringInSelectedLanguage(data.isBanLocked() + "")});
				}
				main.sendMessage(sender, "bancheck.line8", new String[] {data.getBanCount() + ""});
				main.sendMessage(sender, "bancheck.line9", new String[] {data.getWarnCount() + ""});
				main.sendMessage(sender, "bancheck.line10", new String[] {});
				for (String str : data.getPreviousBans()) {
					main.sendMessage(sender, "bancheck.line11", new String[] {str});
				}
				return true;
			} else if (main.isValidIP(args[0])) {
				IPBanData data = mysql.getIPBanData(args[0]);
				if (data != null && data.getBanStatus() && data.getBanExpiration().compareTo(Calendar.getInstance()) <= 0) {
					mysql.deleteBan(args[0], "ipban");
					if (main.getConfig().getBoolean("General.Enable_Login_Listener") && main.getConfig().getBoolean("General.Enable_Listener_Cache")) {
						main.deleteIPBan(args[0]);
					}
					data = mysql.getIPBanData(args[0]);
				}
				if (data == null) {
					main.sendMessage(sender, "bancheck.error", new String[] {});
					return true;
				}
				main.sendMessage(sender, "bancheck.line1.ip", new String[] {data.getIPAddress()});
				main.sendMessage(sender, "bancheck.line2", new String[] {main.getStringInSelectedLanguage(data.getBanStatus() + "")});
				if (data.getBanStatus()) {
					main.sendMessage(sender, "bancheck.line3", new String[] {data.getBanReason()});
					Calendar nowDate = Calendar.getInstance();
					Calendar expirationDate = data.getBanExpiration();
					if ((expirationDate.get(Calendar.YEAR) - 100) > nowDate.get(Calendar.YEAR)) {
						main.sendMessage(sender, "bancheck.line4", new String[] {main.getStringInSelectedLanguage("never")});
					} else {
						main.sendMessage(sender, "bancheck.line4", new String[] {main.getRemainingMinutesUntilCalender(data.getBanExpiration())});
					}
					main.sendMessage(sender, "bancheck.line5", new String[] {data.getBanner()});
					main.sendMessage(sender, "bancheck.line6", new String[] {main.calendarToString(data.getBanDate())});
				}
				main.sendMessage(sender, "bancheck.line10", new String[] {});
				for (String str : data.getPreviousBans()) {
					main.sendMessage(sender, "bancheck.line11", new String[] {str});
				}
				return true;
			} else {
				main.sendMessage(sender, "not_userNameOrIP", new String[] {});
				return true;
			}
		}
		return false;
	}
	
}
