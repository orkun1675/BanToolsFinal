package com.orkunbulutduman.bantoolsfinal;

import java.util.ArrayList;
import java.util.Calendar;

public class PlayerBanData {
	
	String name;
	boolean banned;
	Calendar expiration;
	String reason;
	String banned_by;
	int ban_count;
	int warn_count;
	Calendar banned_at;
	ArrayList<String> previousBans;
	boolean locked;
	
	public PlayerBanData(String name, boolean banned, Calendar expiration, String reason, String banned_by, boolean locked) {
		this.name = name;
		this.banned = banned;
		this.expiration = expiration;
		this.reason = reason;
		this.banned_by = banned_by;
		this.locked = locked;
	}
	
	public PlayerBanData(String name, boolean banned, int ban_count, int warn_count, ArrayList<String> previousBans) {
		this.name = name;
		this.banned = banned;
		this.ban_count = ban_count;
		this.warn_count = warn_count;
		this.previousBans = previousBans;
	}
	
	public PlayerBanData(String name, boolean banned, Calendar expiration, String reason, String banned_by, int ban_count, int warn_count, Calendar banned_at, ArrayList<String> previousBans, boolean locked) {
		this.name = name;
		this.banned = banned;
		this.expiration = expiration;
		this.reason = reason;
		this.banned_by = banned_by;
		this.ban_count = ban_count;
		this.warn_count = warn_count;
		this.banned_at = banned_at;
		this.previousBans = previousBans;
		this.locked = locked;
	}
	
	public String getPlayerName() {
		return name;
	}
	
	public boolean getBanStatus() {
		return banned;
	}
	
	public Calendar getBanExpiration() {
		return expiration;
	}
	
	public String getBanReason() {
		return reason;
	}
	
	public String getBanner() {
		return banned_by;
	}
	
	public int getBanCount() {
		return ban_count;
	}
	
	public int getWarnCount() {
		return warn_count;
	}
	
	public Calendar getBanDate() {
		return banned_at;
	}
	
	public ArrayList<String> getPreviousBans() {
		return previousBans;
	}
	
	public boolean isBanLocked() {
		return locked;
	}

}
