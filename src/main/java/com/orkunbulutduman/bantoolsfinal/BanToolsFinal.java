package com.orkunbulutduman.bantoolsfinal;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Logger;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.orkunbulutduman.bantoolsfinal.commands.Ban;
import com.orkunbulutduman.bantoolsfinal.commands.BanCheck;
import com.orkunbulutduman.bantoolsfinal.commands.BanClear;
import com.orkunbulutduman.bantoolsfinal.commands.BanIP;
import com.orkunbulutduman.bantoolsfinal.commands.BanImport;
import com.orkunbulutduman.bantoolsfinal.commands.LockBan;
import com.orkunbulutduman.bantoolsfinal.commands.Main;
import com.orkunbulutduman.bantoolsfinal.commands.TempBan;
import com.orkunbulutduman.bantoolsfinal.commands.TempBanIP;
import com.orkunbulutduman.bantoolsfinal.commands.UnBan;
import com.orkunbulutduman.bantoolsfinal.commands.UnBanIP;
import com.orkunbulutduman.bantoolsfinal.commands.UnLockBan;
import com.orkunbulutduman.bantoolsfinal.commands.Warn;
import com.orkunbulutduman.bantoolsfinal.listeners.PlayerLoginListener;

public class BanToolsFinal extends JavaPlugin{
	
	private static final Logger logger = Logger.getLogger("Minecraft");
	MysqlManager mysqlManager;
	SimpleDateFormat dateFormat;
	PlayerLoginListener playerLoginListener;
	
	@Override
	public void onEnable() {
		createDefaultConfig();
		log("Created config.");
		mysqlManager = new MysqlManager(this, getConfig().getString("MySQL.address"), getConfig().getString("MySQL.port"),
				getConfig().getString("MySQL.database"), getConfig().getString("MySQL.username"), getConfig().getString("MySQL.password"));
		if (!mysqlManager.openConnection()) {
			return;
		}
		log("Opened MYSQL connection.");
		if (getConfig().getBoolean("General.Enable_Login_Listener")) {
			registerListeners();
			log("Registered listeners.");
		}
		registerCommands();
		log("Registered commands.");
		initiliazeComponents();
		getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
		log("Setup plugin channel.");
		log("by orkun1675 is enabled.");
	}
	
	private void createDefaultConfig() {
		//getConfig().options().copyDefaults(true);
		saveDefaultConfig();
	}
	
	private void registerListeners() {
		playerLoginListener = new PlayerLoginListener(this, mysqlManager);
		getServer().getPluginManager().registerEvents(playerLoginListener, this);
	}
	
	private void registerCommands() {
		getCommand("banclear").setExecutor(new BanClear(this, mysqlManager));
		getCommand("bancheck").setExecutor(new BanCheck(this, mysqlManager));
		getCommand("warn").setExecutor(new Warn(this, mysqlManager));
		getCommand("ban").setExecutor(new Ban(this, mysqlManager));
		getCommand("tempban").setExecutor(new TempBan(this, mysqlManager));
		getCommand("unban").setExecutor(new UnBan(this, mysqlManager));
		getCommand("lockban").setExecutor(new LockBan(this, mysqlManager));
		getCommand("unlockban").setExecutor(new UnLockBan(this, mysqlManager));
		getCommand("banip").setExecutor(new BanIP(this, mysqlManager));
		getCommand("tempbanip").setExecutor(new TempBanIP(this, mysqlManager));
		getCommand("unbanip").setExecutor(new UnBanIP(this, mysqlManager));
		getCommand("bantoolsfinal").setExecutor(new Main(this, mysqlManager));
		getCommand("banimport").setExecutor(new BanImport(this, mysqlManager));
	}
	
	private void initiliazeComponents() {
		dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	}
	
	public String getStringInSelectedLanguage(String str) {
		String lang = getConfig().getString("locale.language");
		String msg = getConfig().getString("locale." + lang + "." + str);
		return msg;
	}
	
	public void sendMessage(CommandSender target, String message_key, String[] variables) {
		String lang = getConfig().getString("locale.language");
		String msg = getConfig().getString("locale." + lang + "." + message_key);
		for (int i = 0; i < variables.length; i++) {
			int num = i + 1;
			msg = msg.replaceAll("%" + num + "%", variables[i]);
		}
		log("Msg: " + msg);
		if (getConfig().getBoolean("General.Enable_Bungee") && target.getName() != null) {
			sendMessageToBungeePlayer(target.getName(), translateAltranateColorCodes(msg));
		} else {
			target.sendMessage(translateAltranateColorCodes(msg));
		}
	}
	
	public void sendDirectMessageToBungeePlayer(String targetName, String message_key, String[] variables) {
		String lang = getConfig().getString("locale.language");
		String msg = getConfig().getString("locale." + lang + "." + message_key);
		for (int i = 0; i < variables.length; i++) {
			int num = i + 1;
			msg = msg.replaceAll("%" + num + "%", variables[i]);
		}
		log("Msg: " + msg);
		sendMessageToBungeePlayer(targetName, translateAltranateColorCodes(msg));
	}
	
	public String getModifiedMessage(String message_key, String[] variables) {
		String lang = getConfig().getString("locale.language");
		String msg = getConfig().getString("locale." + lang + "." + message_key);
		for (int i = 0; i < variables.length; i++) {
			int num = i + 1;
			msg = msg.replaceAll("%" + num + "%", variables[i]);
		}
		return translateAltranateColorCodes(msg);
	}
	
	public void kickWithMessage(String targetName, String message_key, String[] variables) {
		String lang = getConfig().getString("locale.language");
		String msg = getConfig().getString("locale." + lang + "." + message_key);
		for (int i = 0; i < variables.length; i++) {
			int num = i + 1;
			msg = msg.replaceAll("%" + num + "%", variables[i]);
		}
		if (getConfig().getBoolean("General.Enable_Bungee")) {
			kickBungeePlayer(targetName, translateAltranateColorCodes(msg));
		} else {
			Player player = getServer().getPlayer(targetName);
			if (player != null && player.isOnline()) {
				player.kickPlayer(translateAltranateColorCodes(msg));
			}
		}
	}
	
	public String calendarToString(Calendar calendar) {
		return dateFormat.format(calendar.getTime());
	}
	
	public String getRemainingMinutesUntilCalender(Calendar future) {
		Calendar now = Calendar.getInstance();
		long epochDiff = Math.abs(future.getTimeInMillis() - now.getTimeInMillis());
		int minutes = (int) (epochDiff / 60000);
		String result = "";
		if (minutes >= (24 * 60)) {
			int dayCount = minutes / (24 * 60);
			minutes = minutes % (24 * 60);
			if (dayCount > 1) {
				result += dayCount + " " + getStringInSelectedLanguage("days") + " ";
			} else {
				result += dayCount + " " + getStringInSelectedLanguage("day") + " ";
			}
		}
		if (minutes >= 60) {
			int hourCount = minutes / 60;
			minutes = minutes % 60;
			if (hourCount > 1) {
				result += hourCount + " " + getStringInSelectedLanguage("hours") + " ";
			} else {
				result += hourCount + " " + getStringInSelectedLanguage("hour") + " ";
			}
		}
		if (minutes == 1) {
			result += minutes + " " + getStringInSelectedLanguage("minute") + " ";
		} else {
			result += minutes + " " + getStringInSelectedLanguage("minutes") + " ";
		}
		result = result.substring(0, result.length() - 1);
		return result;
	}
	
	public String translateAltranateColorCodes(String msg) {
		msg = msg.replaceAll("&0", ChatColor.BLACK + "");
		msg = msg.replaceAll("&1", ChatColor.DARK_BLUE + "");
		msg = msg.replaceAll("&2", ChatColor.DARK_GREEN + "");
		msg = msg.replaceAll("&3", ChatColor.DARK_AQUA + "");
		msg = msg.replaceAll("&4", ChatColor.DARK_RED + "");
		msg = msg.replaceAll("&5", ChatColor.DARK_PURPLE + "");
		msg = msg.replaceAll("&6", ChatColor.GOLD + "");
		msg = msg.replaceAll("&7", ChatColor.GRAY + "");
		msg = msg.replaceAll("&8", ChatColor.DARK_GRAY + "");
		msg = msg.replaceAll("&9", ChatColor.BLUE + "");
		msg = msg.replaceAll("&a", ChatColor.GREEN + "");
		msg = msg.replaceAll("&b", ChatColor.AQUA + "");
		msg = msg.replaceAll("&c", ChatColor.RED + "");
		msg = msg.replaceAll("&d", ChatColor.LIGHT_PURPLE + "");
		msg = msg.replaceAll("&e", ChatColor.YELLOW + "");
		msg = msg.replaceAll("&f", ChatColor.WHITE+ "");
		msg = msg.replaceAll("&k", ChatColor.MAGIC + "");
		msg = msg.replaceAll("&l", ChatColor.BOLD + "");
		msg = msg.replaceAll("&m", ChatColor.STRIKETHROUGH + "");
		msg = msg.replaceAll("&n", ChatColor.UNDERLINE + "");
		msg = msg.replaceAll("&o", ChatColor.ITALIC + "");
		msg = msg.replaceAll("&r", ChatColor.RESET + "");
		return msg;
	}
	
	public boolean isNumeric(String str) {
		boolean result = true;
		String numbers = "0123456789";
		for (int i = 0; i < str.length(); i++) {
			if (!numbers.contains(str.substring(i, i + 1))) {
				result = false;
				break;
			}
		}
		return result;
	}
	
	public boolean isValidDateString(String str) {
		String pool = "ydhm";
		if (getConfig().getString("locale.language").equalsIgnoreCase("turkish")) {
			pool = "yagsd";
		}
		boolean result = true;
		for (int i = 0; i < str.length(); i++) {
			if (!pool.contains(str.substring(i, i + 1).toLowerCase())) {
				result = false;
				break;
			}
		}
		return result;
	}
	
	public boolean isValidName(String str) {
		String pool = "0123456789qwertyuiopasdfghjklzxcvbnm_QWERTYUIOPASDFGHJKLZXCVBNM";
		boolean result = true;
		for (int i = 0; i < str.length(); i++) {
			if (!pool.contains(str.substring(i, i + 1))) {
				result = false;
				break;
			}
		}
		return result;
	}
	
	public boolean isParsableReason(String str) {
		String pool = " 0123456789qwertyuiopasdfghjklzxcvbnm!@#&*[](){}=+<>,.;?-_";
		boolean result = true;
		for (int i = 0; i < str.length(); i++) {
			if (!pool.contains(str.substring(i, i + 1).toLowerCase())) {
				result = false;
				break;
			}
		}
		return result;
	}
	
	public boolean isValidIP(String ip) {
		try {
	        if (ip == null || ip.isEmpty()) {
	            return false;
	        }
	        String[] parts = ip.split( "\\." );
	        if ( parts.length != 4 ) {
	            return false;
	        }
	        for ( String s : parts ) {
	            int i = Integer.parseInt( s );
	            if ( (i < 0) || (i > 255) ) {
	                return false;
	            }
	        }
	        if(ip.endsWith(".")) {
	                return false;
	        }
	        return true;
	    } catch (NumberFormatException nfe) {
	        return false;
	    }
	}
	
	public void log(String msg) {
		if (getConfig().getBoolean("General.Debug")) {
			logger.info("[Ban Tools Final] " + msg);
		}
	}

	public void addBan(String playerName, PlayerBanData data) {
		playerLoginListener.addElementToBanList(playerName, data);
	}

	public void deleteBan(String playerName) {
		playerLoginListener.removeElementFromBanList(playerName);
	}

	public void addIPBan(String ip, IPBanData data) {
		playerLoginListener.addElementToIPBanList(ip, data);
	}

	public void deleteIPBan(String ip) {
		playerLoginListener.removeElementFromIPBanList(ip);
	}

	public void reloadDataFromMysql() {
		playerLoginListener.reloadHashMaps();
	}

	public boolean playerIsBanned(String name) {
		return playerLoginListener.playerIsBanned(name);
	}

	public boolean ipIsBanned(String ip) {
		return playerLoginListener.ipIsBanned(ip);
	}
	
	public void sendMessageToAuthorities(String message_key, String[] variables) {
		for (Player player : getServer().getOnlinePlayers()) {
			if (!player.getName().equalsIgnoreCase(variables[0]) && player.hasPermission("bantoolsfinal.inform")) {
				sendMessage(player, message_key, variables);
			}
		}
	}
	
	public void kickBungeePlayer(String name, String reason) {
		log("Start kickBungeePlayer");
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("KickPlayer");
		out.writeUTF(name);
		out.writeUTF(reason);
		Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
		player.sendPluginMessage(this, "BungeeCord", out.toByteArray());
		log("End kickBungeePlayer");
	}
	
	public void sendMessageToBungeePlayer(String name, String msg) {
		log("Start sendMessageToBungeePlayer");
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("Message");
		out.writeUTF(name);
		out.writeUTF(msg);
		Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
		player.sendPluginMessage(this, "BungeeCord", out.toByteArray());
		log("End sendMessageToBungeePlayer");
	}
	
	public Calendar inputToCalendar(String str) throws Exception {
		log("Got string date input: " + str);
		Calendar calendar = Calendar.getInstance();
		String holder = "";
		for (int i = 0 ; i < str.length(); i++) {
			String ch = str.substring(i, i + 1);
			if (isNumeric(ch)) {
				holder += ch;
			} else if (isValidDateString(ch)) {
				if (getConfig().getString("locale.language").equalsIgnoreCase("turkish")) {
					if (ch.equalsIgnoreCase("y")) {
						calendar.add(Calendar.YEAR, Integer.parseInt(holder));
					} else if (ch.equalsIgnoreCase("a")) {
						calendar.add(Calendar.MONTH, Integer.parseInt(holder));
					} else if (ch.equalsIgnoreCase("g")) {
						calendar.add(Calendar.DAY_OF_MONTH, Integer.parseInt(holder));
					} else if (ch.equalsIgnoreCase("s")) {
						calendar.add(Calendar.HOUR, Integer.parseInt(holder));
					} else {
						calendar.add(Calendar.MINUTE, Integer.parseInt(holder));
					}
				} else {
					if (ch.equalsIgnoreCase("y")) {
						calendar.add(Calendar.YEAR, Integer.parseInt(holder));
					} else if (ch.equalsIgnoreCase("d")) {
						calendar.add(Calendar.DAY_OF_MONTH, Integer.parseInt(holder));
					} else if (ch.equalsIgnoreCase("h")) {
						calendar.add(Calendar.HOUR, Integer.parseInt(holder));
					} else {
						calendar.add(Calendar.MINUTE, Integer.parseInt(holder));
					}
				}
				holder = "";
			} else {
				log("Could not parse date string.");
				throw new Exception();
			}
		}
		if (holder.length() > 0) {
			log("Could not parse date string.");
			throw new Exception();
		}
		log("Parsed input string to date: " + calendarToString(calendar));
		return calendar;
	}
}
