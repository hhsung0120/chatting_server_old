package site.heeseong.chatting_server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class ChattingRoomData {

	private ChattingRoom chattingRoom;
	private ConcurrentHashMap<Long, Users> users;
	private Object userLock = new Object();
	private Object blackLock = new Object();
	private HashSet<Long> blackList = new HashSet<Long>();

	@JsonIgnore
	public String getName() {
		if (chattingRoom != null) {
			return chattingRoom.getName();
		}
		return null;
	}
	
	@JsonIgnore
	public String getPassword() {
		if (chattingRoom != null) {
			return chattingRoom.getPassword();
		}
		return null;
	}
	
	@JsonIgnore
	public String getDescription() {
		if (chattingRoom != null) {
			return chattingRoom.getDescription();
		}
		return null;
	}
	
	@JsonIgnore
	public int getProgramIdx() {
		if (chattingRoom != null) {
			return chattingRoom.getProgramIdx();
		}
		return -1;
	}

	@JsonIgnore
	public long getUserIdx() {
		if (chattingRoom != null) {
			return chattingRoom.getUserIdx();
		}
		return -1;
	}
	
	@JsonIgnore
	public String getStatus() {
		if (chattingRoom != null) {
			return chattingRoom.getStatus();
		}
		return null;
	}
	
	@JsonIgnore
	public int getType() {
		if (chattingRoom != null) {
			return chattingRoom.getType();
		}
		return 0;
	}
	
	@JsonIgnore
	public long getAdminIdx() {
		if (chattingRoom != null) {
			return chattingRoom.getAdminIdx();
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
