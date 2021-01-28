package site.heeseong.chatting_server.manager;

import com.fasterxml.jackson.annotation.JsonIgnore;
import site.heeseong.chatting_server.model.ChattingRoomData;
import site.heeseong.chatting_server.model.Users;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ChattingRoomManager {
	private ChattingRoomData chatRoom;
	private HashMap<Long, Users> users;
	private Object userLock = new Object();
	private HashSet<Integer> blackList = new HashSet<Integer>();
	private Object blackLock = new Object();
	
	public void setChatRoom(ChattingRoomData chatRoom) {
		this.chatRoom = chatRoom;
	}
	
	public ChattingRoomData getChatRoom() {
		return chatRoom;
	}
	
	@JsonIgnore
	public String getName() {
		if (chatRoom != null) {
			return chatRoom.getName();
		}
		return null;
	}
	
	@JsonIgnore
	public String getPassword() {
		if (chatRoom != null) {
			return chatRoom.getPassword();
		}
		return null;
	}
	
	@JsonIgnore
	public String getDescription() {
		if (chatRoom != null) {
			return chatRoom.getDescription();
		}
		return null;
	}
	
	@JsonIgnore
	public int getProgramIdx() {
		if (chatRoom != null) {
			return chatRoom.getProgramIdx();
		}
		return -1;
	}

	@JsonIgnore
	public long getUserIdx() {
		if (chatRoom != null) {
			return chatRoom.getUserIdx();
		}
		return -1;
	}
	
	@JsonIgnore
	public String getStatus() {
		if (chatRoom != null) {
			return chatRoom.getStatus();
		}
		return null;
	}
	
	@JsonIgnore
	public int getType() {
		if (chatRoom != null) {
			return chatRoom.getType();
		}
		return 0;
	}
	
	@JsonIgnore
	public long getAdminIdx() {
		if (chatRoom != null) {
			return chatRoom.getAdminIdx();
		}
		return 0;
	}
	
	@JsonIgnore
	public HashMap<Long, Users> getUserList() {
		if (users == null) {
			users = new HashMap<Long, Users>();
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
			users = new HashMap<Long, Users>();
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
	
	public void addBlackList(int userIdx) {
		synchronized(blackLock) {
			blackList.add(userIdx);
		}
	}
	
	public boolean isBlackList(int userIdx) {
		return blackList.contains(userIdx);
	}

	public HashSet<Integer> getBlackList() {
		return blackList;
	}

	@JsonIgnore
	public Integer[] getBlackListArray() {
		return (Integer[])blackList.toArray();
	}
	
	public void removeBlackList(int userIdx) {
		synchronized(blackLock) {
			blackList.remove(userIdx);
		}
	}

	@Override
	public String toString() {
		return "ChatRoom [chatRoom=" + chatRoom + ", users=" + users + ", blackList=" + blackList + "]";
	}
}
