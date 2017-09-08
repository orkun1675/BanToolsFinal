package com.orkunbulutduman.bantoolsfinal.commands;

import java.io.FileReader;
import java.util.Calendar;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.orkunbulutduman.bantoolsfinal.BanToolsFinal;
import com.orkunbulutduman.bantoolsfinal.IPBanData;
import com.orkunbulutduman.bantoolsfinal.MysqlManager;
import com.orkunbulutduman.bantoolsfinal.PlayerBanData;

public class BanImport implements CommandExecutor{

	BanToolsFinal main;
	MysqlManager mysql;

	public BanImport(BanToolsFinal main, MysqlManager mysql) {
		this.main = main;
		this.mysql = mysql;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmnd, String label, String[] args) {
		if (args.length > 1 && (args[0].equalsIgnoreCase("player") || args[0].equalsIgnoreCase("ip"))) {
			String defaultreason = "";
			for (int i = 1; i < args.length; i++) {
				defaultreason += args[i] + " ";
			}
			defaultreason = defaultreason.substring(0, defaultreason.length() - 1);
			if (!main.isParsableReason(defaultreason)) {
				main.sendMessage(sender, "invalid_text", new String[] {});
				return true;
			}
			JSONParser jsonParser = new JSONParser();
			Object obj;
			int counter = 0;
			try {
				if (args[0].equalsIgnoreCase("player")) {
					obj = jsonParser.parse(new FileReader(main.getServer().getWorldContainer().getAbsolutePath() + "/banned-players.json"));
				} else {
					obj = jsonParser.parse(new FileReader(main.getServer().getWorldContainer().getAbsolutePath() + "/banned-ips.json"));
				}
				JSONArray jsonArray = (JSONArray) obj;
				for (int i = 0; i < jsonArray.size(); i ++) {
					JSONObject jsonObject = (JSONObject) jsonArray.get(i);
					String expires = (String) jsonObject.get("expires");
					if (expires.equalsIgnoreCase("forever")) {
						String name;
						if (args[0].equalsIgnoreCase("player")) {
							name = (String) jsonObject.get("name");
						} else {
							name = (String) jsonObject.get("ip");
						}
						String actor = (String) jsonObject.get("source");
						///////////////////////////////////////////
						actor = actor.substring(actor.lastIndexOf(" ") + 1, actor.length() - 2);
						String reason = (String) jsonObject.get("reason");
						if (reason.equalsIgnoreCase("guvenlik") || reason.equalsIgnoreCase("güvenlik")) {
							reason = "Guvenlik";
						} else {
							reason = defaultreason;
						}
						///////////////////////////////////////////
						Calendar farAwayDate = Calendar.getInstance();
						farAwayDate.add(Calendar.YEAR, 150);
						if (args[0].equalsIgnoreCase("player")) {
							if (main.isValidName(name)) {
								mysql.banPlayer(name, true, -1, reason, actor);
								if (main.getConfig().getBoolean("General.Enable_Login_Listener") && main.getConfig().getBoolean("General.Enable_Listener_Cache")) {
									boolean locked = false;
									if (reason.toLowerCase().contains("guvenlik")) {
										locked = true;
									}
									main.addBan(name, new PlayerBanData(name, true, farAwayDate, reason, actor, locked));
								}
								counter++;
							}
						} else {
							if (main.isValidIP(name)) {
								mysql.banIP(name, true, -1, reason, actor);
								if (main.getConfig().getBoolean("General.Enable_Login_Listener") && main.getConfig().getBoolean("General.Enable_Listener_Cache")) {
									main.addIPBan(name, new IPBanData(name, true, farAwayDate, reason, actor));
								}
								counter++;
							}
						}
					}
				}
				main.sendMessage(sender, "import.success", new String[] {counter + "", args[0].toLowerCase()});
			} catch (Exception ex) {
				main.sendMessage(sender, "import.fail", new String[] {args[0].toLowerCase()});
				ex.printStackTrace();
			}
			return true;
		}
		return false;
	}

}
