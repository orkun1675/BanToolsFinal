package com.orkunbulutduman.bantoolsfinal;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import org.bukkit.Bukkit;

import code.husky.mysql.MySQL;

public class MysqlManager {

	private MySQL db;
	private final BanToolsFinal main;
	private final String address;
	private final String port;
	private final String database;
	private final String user;
	private final String password;

	public MysqlManager(BanToolsFinal plugin, String address, String port, String database, String user, String password) {
		main = plugin;
		this.address = address;
		this.port = port;
		this.database = database;
		this.user = user;
		this.password = password;
	}

	public boolean openConnection() {
		try {
			db = new MySQL(this.main, address, port, database, user, password);
			db.openConnection();
			Statement st = db.getConnection().createStatement();
			st.executeUpdate("CREATE TABLE IF NOT EXISTS `banToolsFinal_bans` (`player` varchar(32) NOT NULL DEFAULT '?', `expiration` int(11) NOT NULL DEFAULT '0', `reason` longtext, `banned_by` varchar(32) DEFAULT NULL, `banned_at` int(11) NOT NULL DEFAULT '0', `expired` int(1) NOT NULL DEFAULT '0', `locked` int(1) NOT NULL DEFAULT '0') ENGINE=InnoDB DEFAULT CHARSET=utf8;");
			st.executeUpdate("CREATE TABLE IF NOT EXISTS `banToolsFinal_ipbans` (`ip` varchar(32) NOT NULL DEFAULT '?', `expiration` int(11) NOT NULL DEFAULT '0', `reason` longtext, `banned_by` varchar(32) DEFAULT NULL, `banned_at` int(11) NOT NULL DEFAULT '0', `expired` int(1) NOT NULL DEFAULT '0') ENGINE=InnoDB DEFAULT CHARSET=utf8;");
			st.executeUpdate("CREATE TABLE IF NOT EXISTS `banToolsFinal_stats` (`player` varchar(32) NOT NULL DEFAULT '?', `ban_count` int(11) NOT NULL DEFAULT '0', `warn_count` int(11) NOT NULL DEFAULT '0') ENGINE=InnoDB DEFAULT CHARSET=utf8;");
			st.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			main.getLogger().severe("Could not connect to database. Disabling plugin and SHUTTING DOWN SERVER.");
			main.getLogger().severe("Could not connect to database. Disabling plugin and SHUTTING DOWN SERVER.");
			main.getLogger().severe("Could not connect to database. Disabling plugin and SHUTTING DOWN SERVER.");
			Bukkit.getPluginManager().disablePlugin(main);
			Bukkit.getServer().shutdown();
			return false;
		}
	}
	
	public void refreshConnection(){
		try {
			if (!db.checkConnection()) {
				try {
					db.closeConnection();
					db = new MySQL(this.main, address, port, database, user, password);
					db.openConnection();
				} catch (Exception ex) {
					ex.printStackTrace();
					main.getLogger().severe("Could not refresh connection to database.");
				}
			}
		} catch (SQLException e) {
			try {
				db.closeConnection();
				db = new MySQL(this.main, address, port, database, user, password);
				db.openConnection();
			} catch (Exception ex) {
				ex.printStackTrace();
				main.getLogger().severe("Could not refresh connection to database.");
			}
		}
	}

	public void closeConnection() {
		try {
			db.closeConnection();
		} catch (SQLException e) {
			e.printStackTrace();
			main.getLogger().severe("Connection to database could not be closed.");
		}
	}
	
	public boolean isPlayerBanned(String playerName) {
		refreshConnection();
		try {
			Statement st = db.getConnection().createStatement();
			ResultSet res = st.executeQuery("SELECT `player` FROM `banToolsFinal_bans` WHERE LOWER(`player`) = LOWER('" + playerName.toLowerCase() + "') AND `expired` = '0';");
			boolean result = res.next();
			st.close();
			main.log("Check if banned for player: " + playerName);
			return result;
		} catch(Exception ex) {
			ex.printStackTrace();
			main.log("Check if banned failed for player: " + playerName);
			return false;
		}
	}
	
	public boolean isBanLocked(String playerName) {
		refreshConnection();
		try {
			Statement st = db.getConnection().createStatement();
			ResultSet res = st.executeQuery("SELECT `locked` FROM `banToolsFinal_bans` WHERE LOWER(`player`) = LOWER('" + playerName.toLowerCase() + "') AND `expired` = '0';");
			if (res.next()) {
				if (res.getInt("locked") > 0) {
					return true;
				}
			}
			st.close();
			main.log("Check if ban is locked for player: " + playerName);
			return false;
		} catch(Exception ex) {
			ex.printStackTrace();
			main.log("Check if ban is locked failed for player: " + playerName);
			return false;
		}
	}
	
	public boolean isIPBanned(String ip) {
		refreshConnection();
		try {
			Statement st = db.getConnection().createStatement();
			ResultSet res = st.executeQuery("SELECT `ip` FROM `banToolsFinal_ipbans` WHERE `ip` = '" + ip + "' AND `expired` = '0';");
			boolean result = res.next();
			st.close();
			main.log("Check if banned for ip: " + ip);
			return result;
		} catch(Exception ex) {
			ex.printStackTrace();
			main.log("Check if banned failed for ip: " + ip);
			return false;
		}
	}

	public PlayerBanData getPlayerBanData(String playerName) {
		refreshConnection();
		PlayerBanData data;
		int ban_count = 0;
		int warn_count = 0;
		try {
			Statement st = db.getConnection().createStatement();
			ResultSet res = st.executeQuery("SELECT * FROM `banToolsFinal_stats` WHERE LOWER(`player`) = LOWER('" + playerName.toLowerCase() + "');");
			if (res.next()) {
				ban_count = res.getInt("ban_count");
				warn_count = res.getInt("warn_count");
			}
			res = st.executeQuery("SELECT * FROM `banToolsFinal_bans` WHERE LOWER(`player`) = LOWER('" + playerName.toLowerCase() + "') AND `expired` = '0';");
			if (res.next()) {
				boolean l = false;
				if (res.getInt("locked") > 0) {
					l = true;
				}
				data = new PlayerBanData(playerName.toLowerCase(), true, epochToDate(res.getInt("expiration")), res.getString("reason"), res.getString("banned_by"), ban_count, warn_count, epochToDate(res.getInt("banned_at")), getPreviousBans("ban", playerName), l);
			} else {
				data = new PlayerBanData(playerName.toLowerCase(), false, ban_count, warn_count, getPreviousBans("ban", playerName));
			}
			st.close();
			main.log("Got ban info for player: " + playerName);
		} catch(Exception ex) {
			ex.printStackTrace();
			main.log("Could not get ban info for player: " + playerName);
			return null;
		}
		return data;
	}

	public ArrayList<PlayerBanData> getAllPlayerBanData() {
		refreshConnection();
		ArrayList<PlayerBanData> playerBanDataList = new ArrayList<>();
		try {
			Statement st = db.getConnection().createStatement();
			ResultSet res = st.executeQuery("SELECT * FROM `banToolsFinal_bans` WHERE `expired` = '0';");
			while (res.next()) {
				boolean l = false;
				if (res.getInt("locked") > 0) {
					l = true;
				}
				playerBanDataList.add(new PlayerBanData(res.getString("player"), true, epochToDate(res.getInt("expiration")), res.getString("reason"), res.getString("banned_by"), l));
			}
			st.close();
			main.log("Got all ban info.");
		} catch(Exception ex) {
			ex.printStackTrace();
			main.log("Could not get all ban info.");
			return null;
		}
		return playerBanDataList;
	}

	public IPBanData getIPBanData(String ip) {
		refreshConnection();
		IPBanData data;
		try {
			Statement st = db.getConnection().createStatement();
			ResultSet res = st.executeQuery("SELECT * FROM `banToolsFinal_ipbans` WHERE `ip`='" + ip+ "' AND `expired` = '0';");
			if (res.next()) {
				data = new IPBanData(ip, true, epochToDate(res.getInt("expiration")), res.getString("reason"), res.getString("banned_by"), epochToDate(res.getInt("banned_at")), getPreviousBans("ipban", ip));
			} else {
				data = new IPBanData(ip, false, getPreviousBans("ipban", ip));
			}
			st.close();
			main.log("Got ban info for IP: " + ip);
		} catch(Exception ex) {
			ex.printStackTrace();
			main.log("Could not get ban info for IP: " + ip);
			return null;
		}
		return data;
	}

	public ArrayList<IPBanData> getAllIPBanData() {
		refreshConnection();
		ArrayList<IPBanData> ipBanDataList = new ArrayList<>();
		try {
			Statement st = db.getConnection().createStatement();
			ResultSet res = st.executeQuery("SELECT * FROM `banToolsFinal_ipbans` WHERE `expired` = '0';");
			while (res.next()) {
				ipBanDataList.add(new IPBanData(res.getString("ip"), true, epochToDate(res.getInt("expiration")), res.getString("reason"), res.getString("banned_by")));
			}
			st.close();
			main.log("Got all ip ban info.");
		} catch(Exception ex) {
			ex.printStackTrace();
			main.log("Could not get all ip ban info.");
			return null;
		}
		return ipBanDataList;
	}

	public void banPlayer(String playerName, boolean infinite, int expiration, String reason, String banner) {
		refreshConnection();
		int locked = 0;
		if (infinite) {
			expiration = -1;
		}
		if (reason.toLowerCase().contains("guvenlik")) {
			locked = 1;
		}
		try {
			Statement st = db.getConnection().createStatement();
			ResultSet res = st.executeQuery("SELECT `player` FROM `banToolsFinal_bans` WHERE LOWER(`player`) = LOWER('" + playerName.toLowerCase() + "') AND `expired` = '0';");
			if (res.next()) {
				st.executeUpdate("UPDATE `banToolsFinal_bans` SET `expiration`='" + expiration + "', `reason`='" + reason + "', `banned_by`='" + banner + "', `banned_at`='" + dateToEpoch(Calendar.getInstance()) + "', `expired`='0', `locked`='" + locked + "'  WHERE LOWER(`player`) = LOWER('" + playerName.toLowerCase() + "');");
			} else {
				st.executeUpdate("INSERT INTO `banToolsFinal_bans` SET `player`='" + playerName + "', `expiration`='" + expiration + "', `reason`='" + reason + "', `banned_by`='" + banner + "', `banned_at`='" + dateToEpoch(Calendar.getInstance()) + "', `expired`='0', `locked`='" + locked + "';");
			}
			st.close();
			addCount(playerName, "ban");
			main.log("Banned player: " + playerName);
		} catch(Exception ex) {
			ex.printStackTrace();
			main.log("Could not ban player: " + playerName);
		}
	}
	
	public void lockBan(String playerName) {
		refreshConnection();
		try {
			Statement st = db.getConnection().createStatement();
			st.executeUpdate("UPDATE `banToolsFinal_bans` SET `locked`= '1' WHERE LOWER(`player`) = LOWER('" + playerName.toLowerCase() + "') AND `expired` = '0';");
			st.close();
			main.log("Locked ban for player: " + playerName);
		} catch(Exception ex) {
			ex.printStackTrace();
			main.log("Could not lock ban for player: " + playerName);
		}
	}
	
	public void unLockBan(String playerName) {
		refreshConnection();
		try {
			Statement st = db.getConnection().createStatement();
			st.executeUpdate("UPDATE `banToolsFinal_bans` SET `locked` = '0' WHERE LOWER(`player`) = LOWER('" + playerName.toLowerCase() + "') AND `expired` = '0';");
			st.close();
			main.log("Unlocked ban for player: " + playerName);
		} catch(Exception ex) {
			ex.printStackTrace();
			main.log("Could not unlock ban for player: " + playerName);
		}
	}

	public void banIP(String ip, boolean infinite, int expiration, String reason, String banner) {
		refreshConnection();
		if (infinite) {
			expiration = -1;
		}
		try {
			Statement st = db.getConnection().createStatement();
			ResultSet res = st.executeQuery("SELECT `ip` FROM `banToolsFinal_ipbans` WHERE `ip` = '" + ip + "' AND `expired` = '0';");
			if (res.next()) {
				st.executeUpdate("UPDATE `banToolsFinal_ipbans` SET `expiration`='" + expiration + "', `reason`='" + reason + "', `banned_by`='" + banner + "', `banned_at`='" + dateToEpoch(Calendar.getInstance()) + "', `expired`='0' WHERE `ip`='" + ip + "';");
			} else {
				st.executeUpdate("INSERT INTO `banToolsFinal_ipbans` SET `ip`='" + ip + "', `expiration`='" + expiration + "', `reason`='" + reason + "', `banned_by`='" + banner + "', `banned_at`='" + dateToEpoch(Calendar.getInstance()) + "', `expired`='0';");
			}
			st.close();
			main.log("Banned IP: " + ip);
		} catch(Exception ex) {
			ex.printStackTrace();
			main.log("Could not ban IP: " + ip);
		}
	}

	public void addCount(String playerName, String type) {
		refreshConnection();
		if (!type.equals("ban") && !type.equals("warn") ) {
			main.log("addCount Type Error.");
			return;
		}
		try {
			Statement st = db.getConnection().createStatement();
			ResultSet res = st.executeQuery("SELECT `player` FROM `banToolsFinal_stats` WHERE LOWER(`player`) = LOWER('" + playerName.toLowerCase() + "');");
			if (res.next()) {
				st.executeUpdate("UPDATE `banToolsFinal_stats` SET `" + type + "_count`= (" + type + "_count + 1) where `player`='" + playerName + "';");
			} else {
				st.executeUpdate("INSERT INTO `banToolsFinal_stats` SET `player`='" + playerName + "', `" + type + "_count`='1';");
			}
			st.close();
			main.log("Added " + type + " count to: " + playerName);
		} catch(Exception ex) {
			ex.printStackTrace();
			main.log("Could not add " + type + " count to: " + playerName);
		}
	}

	public void deleteBan(String playerNameOrIP, String type) {
		refreshConnection();
		if (!type.equals("ban") && !type.equals("ipban")) {
			main.log("deleteBan Type Error.");
			return;
		}
		try {
			Statement st = db.getConnection().createStatement();
			if (type.equals("ban")) {
				st.executeUpdate("UPDATE `banToolsFinal_bans` SET `expired` = '1' WHERE LOWER(`player`)=LOWER('" + playerNameOrIP + "') AND `expired` = '0';");
			} else if (type.equals("ipban")) {
				st.executeUpdate("UPDATE `banToolsFinal_ipbans` SET `expired` = '1' WHERE LOWER(`ip`)=LOWER('" + playerNameOrIP + "') AND `expired` = '0';");
			}
			st.close();
			main.log("Removed the " + type + " on: " + playerNameOrIP);
		} catch(Exception ex) {
			ex.printStackTrace();
			main.log("Could not remove the " + type + " on: " + playerNameOrIP);
		}
	}
	
	public void clearBanRecord(String playerNameOrIP, String type) {
		refreshConnection();
		if (!type.equals("ban") && !type.equals("ipban")) {
			main.log("clearBan Type Error.");
			return;
		}
		try {
			Statement st = db.getConnection().createStatement();
			if (type.equals("ban")) {
				st.executeUpdate("DELETE FROM `banToolsFinal_bans` WHERE LOWER(`player`)=LOWER('" + playerNameOrIP + "');");
				st.executeUpdate("DELETE FROM `banToolsFinal_stats` WHERE LOWER(`player`)=LOWER('" + playerNameOrIP + "');");
			} else if (type.equals("ipban")) {
				st.executeUpdate("DELETE FROM `banToolsFinal_ipbans` `ip`='" + playerNameOrIP + "';");
			}
			st.close();
			main.log("Cleared " + type + " records for: " + playerNameOrIP);
		} catch(Exception ex) {
			ex.printStackTrace();
			main.log("Could not clear " + type + " records for: " + playerNameOrIP);
		}
	}

	public int dateToEpoch(Calendar date) {
		int epoch = (int) (date.getTimeInMillis() / 1000);
		main.log("dateToEpoch: " + epoch);
		return epoch;
	}

	public Calendar epochToDate(int epoch) {
		Calendar date = Calendar.getInstance();
		if (epoch == -1) {
			date.add(Calendar.YEAR, 150);
		} else {
			date.setTimeInMillis(0);
			date.add(Calendar.SECOND, epoch);
		}
		main.log("epochToDate: " + epoch + " --- " + date);
		return date;
	}
	
	public ArrayList<String> getPreviousBans(String type, String playerNameOrIP) {
		refreshConnection();
		if (!type.equals("ban") && !type.equals("ipban") ) {
			main.log("getPreviousBans Type Error.");
			return new ArrayList<>();
		}
		try {
			ArrayList<String> list = new ArrayList<String>();
			Statement st = db.getConnection().createStatement();
			ResultSet res;
			if (type.equals("ban")) {
				res = st.executeQuery("SELECT * FROM `banToolsFinal_bans` WHERE LOWER(`player`) = LOWER('" + playerNameOrIP.toLowerCase() + "') AND `expired` = '1';");
			} else {
				res = st.executeQuery("SELECT * FROM `banToolsFinal_ipbans` WHERE `ip` = '" + playerNameOrIP + "' AND `expired` = '1';");
			}
			while (res.next()) {
				list.add("&a[" + main.calendarToString(epochToDate(res.getInt("banned_at"))) + "] &c: &a" + res.getString("reason"));
			}
			if (list.size() < 1) {
				list.add(main.getStringInSelectedLanguage("none"));
			}
			st.close();
			main.log("Got previous " + type + "s for: " + playerNameOrIP);
			return list;
		} catch(Exception ex) {
			ex.printStackTrace();
			main.log("Could not get previous " + type + "s for: " + playerNameOrIP);
			return new ArrayList<>();
		}
	}
}
