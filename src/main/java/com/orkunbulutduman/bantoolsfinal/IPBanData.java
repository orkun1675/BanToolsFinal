package com.orkunbulutduman.bantoolsfinal;

import java.util.ArrayList;
import java.util.Calendar;

public class IPBanData {
	
	String ip;
	boolean banned;
	Calendar expiration;
	String reason;
	String banned_by;
	Calendar banned_at;
	ArrayList<String> previousBans;
	
	public IPBanData(String ip, boolean banned, ArrayList<String> previousBans) {
		this.ip = ip;
		this.banned = banned;
		this.previousBans = previousBans;
	}
	
	public IPBanData(String ip, boolean banned, Calendar expiration, String reason, String banned_by) {
		this.ip = ip;
		this.banned = banned;
		this.expiration = expiration;
		this.reason = reason;
		this.banned_by = banned_by;
	}
	
	public IPBanData(String ip, boolean banned, Calendar expiration, String reason, String banned_by, Calendar banned_at, ArrayList<String> previousBans) {
		this.ip = ip;
		this.banned = banned;
		this.expiration = expiration;
		this.reason = reason;
		this.banned_by = banned_by;
		this.banned_at = banned_at;
		this.previousBans = previousBans;
	}
	
	public String getIPAddress() {
		return ip;
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
	
	public Calendar getBanDate() {
		return banned_at;
	}
	
	public ArrayList<String> getPreviousBans() {
		return previousBans;
	}

}
