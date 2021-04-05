package site.heeseong.chatting_server.manager;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import site.heeseong.chatting_server.model.ChattingRoom;
import site.heeseong.chatting_server.model.Users;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class ChattingRoomManager {

	private ChattingRoom chattingRoomData;
	private ConcurrentHashMap<Long, Users> users;
	private Object userLock = new Object();
	private HashSet<Long> blackList = new HashSet<Long>();
	private Object blackLock = new Object();

	@JsonIgnore
	public String getName() {
		if (chattingRoomData != null) {
			return chattingRoomData.getName();
		}
		return null;
	}
	
	@JsonIgnore
	public String getPassword() {
		if (chattingRoomData != null) {
			return chattingRoomData.getPassword();
		}
		return null;
	}
	
	@JsonIgnore
	public String getDescription() {
		if (chattingRoomData != null) {
			return chattingRoomData.getDescription();
		}
		return null;
	}
	
	@JsonIgnore
	public int getProgramIdx() {
		if (chattingRoomData != null) {
			return chattingRoomData.getProgramIdx();
		}
		return -1;
	}

	@JsonIgnore
	public long getUserIdx() {
		if (chattingRoomData != null) {
			return chattingRoomData.getUserIdx();
		}
		return -1;
	}
	
	@JsonIgnore
	public String getStatus() {
		if (chattingRoomData != null) {
			return chattingRoomData.getStatus();
		}
		return null;
	}
	
	@JsonIgnore
	public int getType() {
		if (chattingRoomData != null) {
			return chattingRoomData.getType();
		}
		return 0;
	}
	
	@JsonIgnore
	public long getAdminIdx() {
		if (chattingRoomData != null) {
			return chattingRoomData.getAdminIdx();
		}
		return 0;
	}
	
	@JsonIgnore
	public ConcurrentHashMap<Long, Users> getUserList() {
		if (users == null) {
			users = new ConcurrentHashMap<Long, Users>();
		}
		return users;
	}
	
	public Set<Long> getUsers() {
		Set<Long> userIdxs = new HashSet<Long>();
		
		for (Long keyIndex : getInternalUsers()) {
			Users user = users.get(keyIndex);
			userIdxs.add(user.getUserIdx());
		}
		return userIdxs;
	}
	
	@JsonIgnore
	public Set<Long> getInternalUsers() {
		if (users == null) {
			users = new ConcurrentHashMap<Long, Users>();
		}
		return users.keySet();
	}
	
	public int addUser(Users user) {
		if (getInternalUsers().contains(user.getInternalIdx()) == true) {
			return -1;
		}
		synchronized (userLock) {
			users.put(user.getInternalIdx(), user);
		}
		return 0;
	}
	
	public int removeUser(long internalIdx) {
		if (getInternalUsers().contains(internalIdx) == false) {
			return -1;
		}

		synchronized (userLock) {
			users.remove(internalIdx);
		}
		
		return 0;
	}
	
	public void addBlackList(long userIdx) {
		synchronized(blackLock) {
			blackList.add(userIdx);
		}
	}
	
	public boolean isBlackList(long userIdx) {
		return blackList.contains(userIdx);
	}

	public HashSet<Long> getBlackList() {
		return blackList;
	}

	@JsonIgnore
	public Long[] getBlackListArray() {
		return (Long[])blackList.toArray();
	}
	
	public void removeBlackList(long userIdx) {
		synchronized(blackLock) {
			blackList.remove(userIdx);
		}
	}
}
