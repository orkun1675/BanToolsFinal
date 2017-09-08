package com.orkunbulutduman.bantoolsfinal.listeners;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import com.orkunbulutduman.bantoolsfinal.BanToolsFinal;
import com.orkunbulutduman.bantoolsfinal.IPBanData;
import com.orkunbulutduman.bantoolsfinal.MysqlManager;
import com.orkunbulutduman.bantoolsfinal.PlayerBanData;

public class PlayerLoginListener implements Listener{

	BanToolsFinal main;
	MysqlManager mysql;
	private ConcurrentHashMap<String, PlayerBanData> playerBans;
	private ConcurrentHashMap<String, IPBanData> ipBans;

	public PlayerLoginListener(BanToolsFinal main, MysqlManager mysql) {
		this.main = main;
		this.mysql = mysql;
		if (this.main.getConfig().getBoolean("General.Enable_Listener_Cache")) {
			reloadHashMaps();
		}
	}
	
	public void reloadHashMaps() {
		playerBans = new ConcurrentHashMap<>();
		ArrayList<PlayerBanData> dataList = mysql.getAllPlayerBanData();
		if (dataList != null) {
			for (PlayerBanData data : dataList) {
				playerBans.put(data.getPlayerName().toLowerCase(), data);
			}
		}
		ipBans = new ConcurrentHashMap<>();
		ArrayList<IPBanData> ipDataList = mysql.getAllIPBanData();
		if (ipDataList != null) {
			for (IPBanData data : ipDataList) {
				ipBans.put(data.getIPAddress().toLowerCase(), data);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onJoin(AsyncPlayerPreLoginEvent event) {
		if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
			return;
		}
		PlayerBanData mysqlData = null;
		if (!this.main.getConfig().getBoolean("General.Enable_Listener_Cache")) {
			mysqlData = mysql.getPlayerBanData(event.getName().toLowerCase());
		}
		if ( (main.getConfig().getBoolean("General.Enable_Listener_Cache") && playerBans.containsKey(event.getName().toLowerCase())) || (mysqlData != null && mysqlData.getBanStatus()) ) {
			String msg = "";
			PlayerBanData data;
			if (mysqlData != null) {
				data = mysqlData;
			} else {
				data = playerBans.get(event.getName().toLowerCase());
			}
			Calendar expirationDate = data.getBanExpiration();
			Calendar nowDate = Calendar.getInstance();
			if (expirationDate.compareTo(nowDate) <= 0) {
				mysql.deleteBan(event.getName().toLowerCase(), "ban");
				playerBans.remove(event.getName().toLowerCase());
				return;
			}
			if ((expirationDate.get(Calendar.YEAR) - 100) > nowDate.get(Calendar.YEAR)) {
				msg = main.getModifiedMessage("ban.kick", new String[] {data.getBanReason(), data.getBanner()});
			} else {
				msg = main.getModifiedMessage("tempban.kick", new String[] {data.getBanReason(), main.calendarToString(expirationDate), main.getRemainingMinutesUntilCalender(expirationDate), data.getBanner()});
			}
			event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, msg);
			return;
		}
		String playerIP = event.getAddress().toString().substring(1);
		IPBanData mysqlIPData = null;
		if (!this.main.getConfig().getBoolean("General.Enable_Listener_Cache")) {
			mysqlIPData = mysql.getIPBanData(playerIP);
		}
		if ( (main.getConfig().getBoolean("General.Enable_Listener_Cache") && ipBans.containsKey(playerIP)) || (mysqlIPData != null && mysqlIPData.getBanStatus()) ) {
			String msg = "";
			IPBanData data;
			if (mysqlIPData != null) {
				data = mysqlIPData;
			} else {
				data = ipBans.get(playerIP);
			}
			Calendar expirationDate = data.getBanExpiration();
			Calendar nowDate = Calendar.getInstance();
			if (expirationDate.compareTo(nowDate) <= 0) {
				mysql.deleteBan(playerIP, "ipban");
				playerBans.remove(playerIP);
				return;
			}
			if ((expirationDate.get(Calendar.YEAR) - 100) > nowDate.get(Calendar.YEAR)) {
				msg = main.getModifiedMessage("ipban.kick", new String[] {data.getBanReason(), data.getBanner()});
			} else {
				msg = main.getModifiedMessage("tempbanip.kick", new String[] {data.getBanReason(), main.calendarToString(expirationDate), main.getRemainingMinutesUntilCalender(expirationDate), data.getBanner()});
			}
			event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, msg);
			return;
		}
	}
	
	public void addElementToBanList(String playerName, PlayerBanData data) {
		playerBans.put(playerName.toLowerCase(), data);
	}

	public void removeElementFromBanList(String playerName) {
		playerBans.remove(playerName.toLowerCase());
	}

	public void addElementToIPBanList(String ip, IPBanData data) {
		ipBans.put(ip, data);
	}

	public void removeElementFromIPBanList(String ip) {
		ipBans.remove(ip);
	}

	public boolean playerIsBanned(String name) {
		return playerBans.containsKey(name.toLowerCase());
	}

	public boolean ipIsBanned(String ip) {
		return ipBans.containsKey(ip.toLowerCase());
	}

}
