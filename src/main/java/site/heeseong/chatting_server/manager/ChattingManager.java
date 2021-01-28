package site.heeseong.chatting_server.manager;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import site.heeseong.chatting_server.exceptions.BadArgumentException;
import site.heeseong.chatting_server.exceptions.ChatRoomExistException;
import site.heeseong.chatting_server.exceptions.UserExistException;
import site.heeseong.chatting_server.mapper.ChattingMapper;
import site.heeseong.chatting_server.mapper.ContextMapper;
import site.heeseong.chatting_server.model.*;


import java.lang.ref.WeakReference;
import java.util.HashMap;

@Log4j2
@Component
public class ChattingManager {

	private HashMap<Integer, ChattingRoomManager> chatRooms = new HashMap<Integer, ChattingRoomManager>();
	private HashMap<Long, ChattingUser> chatUsers = new HashMap<Long, ChattingUser>();
	private Object roomLock = new Object();
	private Object userLock = new Object();
	private long internalIndex = 0;

	final private ChattingMapper chattingMapper;
	final private ContextMapper contextMapper;

	public ChattingManager(ChattingMapper chattingMapper, ContextMapper contextMapper){
		this.chattingMapper = chattingMapper;
		this.contextMapper = contextMapper;
	}

	public EnterRoomResult enterChatRoom(ChattingRoom chattingRoom, Users userInfo, boolean notify) throws Exception {
		if (userInfo.getInternalIdx() != -1) {
			throw new BadArgumentException();
		}

		ChattingUser user = createuser(userInfo);

		ChattingRoomManager chatRoomManager = chatRooms.get(chattingRoom.getProgramIdx());
		if (chatRoomManager == null) {
			chatRoomManager = createChatRoom(user.getInternalIdx(), chattingRoom, true);
		}

		if (chatRoomManager.addUser(user.getUser()) == -1) {
			throw new UserExistException();
		}

		user.setProgramIdx(chattingRoom.getProgramIdx());

		if (notify) {
			Event event = EventManager.makeEnterRoomEvent(chattingRoom.getProgramIdx(), userInfo);
			//sendEvent(user.getInternalIdx(), event);
			chattingMapper.addEvent(event);
		}

		//saveUsers();
		//saveRooms();

		EnterRoomResult newResult = new EnterRoomResult(user.getInternalIdx(), chatRoomManager.getChatRoom());

		log.debug("enterchatroom user : " + chatUsers);
		log.debug("enterchatroom room : " + chatRooms);


		return newResult;
	}

	/*public void saveRooms() {
		ContextDTO ctx = new ContextDTO();
		
		//Object to JSON in String
		ObjectMapper mapper = new ObjectMapper();
		
		ctx.setType("ROOM");
		try {
			String jsonInString = mapper.writeValueAsString(chatRooms);
			log.debug("JSON - room : " + jsonInString);
			ctx.setContent(jsonInString);
			contextMapper.setContext(ctx);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void saveUsers() {
		ContextDTO ctx = new ContextDTO();
		
		//Object to JSON in String
		ObjectMapper mapper = new ObjectMapper();
		
		ctx.setType("USER");
		try {
			String jsonInString = mapper.writeValueAsString(chatUsers);
			log.debug("JSON - user : " + jsonInString);
			ctx.setContent(jsonInString);
			contextMapper.setContext(ctx);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void loadRoom() {
		try {
			ContextDTO ctx = contextMapper.getRoomContext();
			if (ctx != null) {
				String roomData = ctx.getContent();
				log.debug("load - room : " + roomData);
				
				JsonParser springParser = JsonParserFactory.getJsonParser();
				Map<String, Object> result = springParser.parseMap(roomData);
				
				for (Entry<String, Object> idx : result.entrySet()) {
					@SuppressWarnings("unchecked")
					Map<String, Object> chatRoom = (Map<String, Object>)idx.getValue();
					
					if (chatRoom != null) {
						@SuppressWarnings("unchecked")
						Map<String, Object> room = (Map<String, Object>)chatRoom.get("chatRoom");
						if (room != null) {
							site.heeseong.chatting.model.ChatRoom newChatRoom = new site.heeseong.chatting.model.ChatRoom((int)room.get("programIdx"), (String)room.get("name"), (String)room.get("password"),
									(String)room.get("description"), (String)room.get("status"), (int)room.get("type"), (int)room.get("adminIdx"), (int)room.get("userIdx"));
							ChatRoomManager currentRoom = createChatRoom(-1, newChatRoom, false);
							
							@SuppressWarnings("unchecked")
							List<Integer> users = (List<Integer>)chatRoom.get("users");
							if (users != null) {
								for (Integer userIdx : users) {
									for (Entry<Long, ChatUser> userEntry : chatUsers.entrySet()) {
										ChatUser user = userEntry.getValue();
										if (user.getUserIdx() == userIdx) {
											currentRoom.addUser(user.getUser());
											user.setProgramIdx(currentRoom.getProgramIdx());
										}
									}
								}
							}
							
							@SuppressWarnings("unchecked")
							List<Integer> blackList = (List<Integer>)chatRoom.get("blackList");
							if (blackList != null) {
								for (Integer blackUser : blackList) {
									currentRoom.addBlackList(blackUser);
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void loadUser() {
		try {
			ContextDTO ctx = contextMapper.getUserContext();
			if (ctx != null) {
				String userData = ctx.getContent();
				log.debug("load - user : " + userData);

				JsonParser springParser = JsonParserFactory.getJsonParser();
				Map<String, Object> result = springParser.parseMap(userData);

				for (Entry<String, Object> idx : result.entrySet()) {
					@SuppressWarnings("unchecked")
					Map<String, Object> chatUser = (Map<String, Object>)idx.getValue();
					
					if (chatUser != null) {
						Number tmpInternalIdx = (Number)chatUser.get("internalIdx");
						Long userInternalIdx = tmpInternalIdx.longValue();
						
						Users userInfo = new Users(userInternalIdx, (int)chatUser.get("userIdx"), (String)chatUser.get("userId"), (String)chatUser.get("userName"), (boolean)chatUser.get("admin"));
						createuser(userInfo);
						
						if (internalIndex < userInternalIdx) {
							internalIndex = userInternalIdx + 1;
						}
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void loadContext() {
		loadUser();
		loadRoom();
		
		log.debug("loaded user : " + chatUsers);
		log.debug("loaded room : " + chatRooms);
	}
	
	private void checkAdmin(long internalIdx) throws Exception {
		ChatUser user = chatUsers.get(internalIdx);
		if (user == null || user.isAdmin() != true) {
			throw new UnauthorizedException();
		}
	}

	*/
	private ChattingRoomManager createChatRoom(long internalIdx, ChattingRoom chatRoomData, boolean log) throws Exception {
		ChattingRoomManager chatRoomManager;
		
		synchronized (roomLock) {
			if (chatRooms.get(chatRoomData.getProgramIdx()) != null) {
				throw new ChatRoomExistException();
			}
			
			ChattingRoomData newRoomData = new ChattingRoomData();
			newRoomData.setProgramIdx(chatRoomData.getProgramIdx());
			newRoomData.setName(chatRoomData.getName());
			newRoomData.setPassword(chatRoomData.getPassword());
			newRoomData.setDescription(chatRoomData.getDescription());
			newRoomData.setStatus(chatRoomData.getStatus());
			newRoomData.setType(chatRoomData.getType());
			newRoomData.setAdminIdx(chatRoomData.getAdminIdx());
			newRoomData.setUserIdx(chatRoomData.getUserIdx());
			
			WeakReference<ChattingRoomManager> chatRoomRef = new WeakReference<ChattingRoomManager>(new ChattingRoomManager());
			chatRoomManager = chatRoomRef.get();
			chatRoomManager.setChatRoom(newRoomData);
			chatRooms.put(chatRoomManager.getProgramIdx(), chatRoomManager);
		}

		if (log == true) {
			Event event = EventManager.makeCreateRoomEvent(chatRoomData);
	//		sendEvent(internalIdx, event);
			chattingMapper.addEvent(event);
		}
		
		return chatRoomManager;
	}
	/*
	
	public ChatRoomData updateChatRoom(long internalIdx, site.heeseong.chatting.model.ChatRoom roomInfo) throws Exception {
		ChatRoomManager chatRoomManager = chatRooms.get(roomInfo.getProgramIdx());
		
		checkAdmin(internalIdx);
		
		if (chatRoomManager == null) {
			throw new ChatRoomNotExistException();
		}
		
		ChatRoomData upadteRoomData = new ChatRoomData();
		if (roomInfo.getName() != null) {
			upadteRoomData.setName(roomInfo.getName());
		}
		else {
			upadteRoomData.setName(chatRoomManager.getName());
		}
		if (roomInfo.getPassword() != null) {
			upadteRoomData.setPassword(roomInfo.getPassword());
		}
		else {
			upadteRoomData.setPassword(chatRoomManager.getPassword());
		}
		if (roomInfo.getDescription() != null) {
			upadteRoomData.setDescription(roomInfo.getDescription());
		}
		else {
			upadteRoomData.setDescription(chatRoomManager.getDescription());
		}
		if (roomInfo.getStatus() != null) {
			upadteRoomData.setStatus(roomInfo.getStatus());
		}
		else {
			upadteRoomData.setStatus(chatRoomManager.getStatus());
		}
		if (roomInfo.getType() != -1) {
			upadteRoomData.setType(roomInfo.getType());
		}
		else {
			upadteRoomData.setType(chatRoomManager.getType());
		}
		if (roomInfo.getAdminIdx() != -1) {
			upadteRoomData.setAdminIdx(roomInfo.getAdminIdx());
		}
		else {
			upadteRoomData.setAdminIdx(chatRoomManager.getAdminIdx());
		}
		upadteRoomData.setUserIdx(chatRoomManager.getUserIdx());
		
		chatRoomManager.setChatRoom(upadteRoomData);
		
		Event event = EventManager.makeUpdateRoomEvent(roomInfo);
		sendEvent(internalIdx, event);
		chattingMapper.addEvent(event);
		
		return upadteRoomData;
	}
		
	private void removeChatRoom(long internalIdx, int roomIdx) throws Exception {
		ChatRoomManager chatRoomManager = chatRooms.get(roomIdx);
		if (chatRoomManager == null) {
			throw new ChatRoomNotExistException();
		}
		
		synchronized (roomLock) {
			chatRooms.remove(roomIdx);
			chatRoomManager = null;
		}
		
		Event event = EventManager.removeChatRoomEvent(roomIdx);
		sendEvent(internalIdx, event);
		chattingMapper.addEvent(event);
	}
	
	public ArrayList<ChatRoomData> getChatRoomList() {
		ArrayList<ChatRoomData> roomList = new ArrayList<ChatRoomData>();
		
		for (Entry<Integer, ChatRoomManager> roomEntry : chatRooms.entrySet()) {
			ChatRoomManager room = roomEntry.getValue();
			if (room != null) {
				roomList.add(room.getChatRoom());
			}
		}
		
		return roomList;
	}
	
	public ChatRoomData getChatRoom(int roomIdx) {
		ChatRoomManager room = chatRooms.get(roomIdx);
		if (room != null) {
			return room.getChatRoom();
		}
		return null;
	}
	*/
	public ChattingUser createuser(Users userInfo) {
		internalIndex++;
		
		userInfo.setInternalIdx(internalIndex);
		
		WeakReference<ChattingUser> userRef = new WeakReference<ChattingUser>(new ChattingUser(userInfo));
		ChattingUser user = userRef.get();

		synchronized (userLock) {
			chatUsers.put(internalIndex, user);
		}
		return user;
	}

	/*
	public int removeUser(long internalIdx, Iterator<Entry<Long, ChatUser>> userIteration) throws Exception {
		ChatUser user = chatUsers.get(internalIdx);
		if (user == null) {
			return -1;
		}
		
		user.removeAll();
		
		synchronized (userLock) {
			if (userIteration != null) {
				userIteration.remove();
			}
			else {
				chatUsers.remove(internalIdx);
			}
			user = null;
		}
		return 0;
	}
	

	
	public int leaveChatRoom(long internalIdx, int roomIdx, Iterator<Entry<Long, ChatUser>> userIteration) throws Exception {
		if (roomIdx != -1) {
			ChatRoomManager chatRoomManager = chatRooms.get(roomIdx);
			if (chatRoomManager == null) {
				throw new ChatRoomNotExistException();
			}
	
			chatRoomManager.removeUser(internalIdx);
			
			ChatUser user = chatUsers.get(internalIdx);
			if (user != null) {
				removeUser(internalIdx, userIteration);
			}
			
			if (chatRoomManager.getInternalUsers().size() == 0) {
				removeChatRoom(internalIdx, roomIdx);
			}
			else {
				Event event = EventManager.makeLeaveRoomEvent(roomIdx, user.getUserIdx());
				sendEvent(internalIdx, event);
				chattingMapper.addEvent(event);
			}
			
			saveUsers();
			saveRooms();
		}
		else {
			throw new BadArgumentException();
		}
		
		log.debug("leaveChatRoom user : " + chatUsers);
		log.debug("leaveChatRoom room : " + chatRooms);

		return 0;
	}
	
	public Integer[] getBlackList(long internalIdx, int roomIdx) throws Exception {
		ChatRoomManager chatRoomManager = chatRooms.get(roomIdx);
		if (chatRoomManager == null) {
			return null;
		}
		
		checkAdmin(internalIdx);
		
		return chatRoomManager.getBlackListArray();
	}
	
	public void addBlackList(long internalIdx, int programIdx, int blackUser) throws Exception {
		if (programIdx != -1) {
			ChatRoomManager chatRoomManager = chatRooms.get(programIdx);
			if (chatRoomManager == null) {
				throw new ChatRoomNotExistException();
			}
			
			checkAdmin(internalIdx);

			chatRoomManager.addBlackList(blackUser);
			saveRooms();
		}
		else {
			throw new BadArgumentException();
		}
	}
	
	public void removeBlackList(long internalIdx, int programIdx, int blackUser) throws Exception {
		if (programIdx != -1) {
			ChatRoomManager chatRoomManager = chatRooms.get(programIdx);
			if (chatRoomManager == null) {
				throw new ChatRoomNotExistException();
			}
			
			checkAdmin(internalIdx);
			
			chatRoomManager.removeBlackList(blackUser);
			saveRooms();
		}
	}
	
	public ArrayList<Users> getUserList(int roomIdx) {
		ArrayList<Users> userList = new ArrayList<Users>();
		
		ChatRoomManager chatRoomManager = chatRooms.get(roomIdx);
		if (chatRoomManager == null) {
			return null;
		}
		for (Entry<Long, Users> userEntry : chatRoomManager.getUserList().entrySet()) {
			
			userList.add(userEntry.getValue());
		}
		return userList;
	}
	
	public ArrayList<Event> getNewEvents(long internalIdx) throws Exception {
		ChatUser user = chatUsers.get(internalIdx);
		if (user == null) {
			log.debug("getNewEvents : NO User : " + internalIdx);
			throw new UserNotExistException();
		}
		
		return user.getEvents();
	}
	
	private void sendEventToAll(long internalIdx, Event event, boolean sendMyself) {
		for (Entry<Long, ChatUser> userEntry : chatUsers.entrySet()) {
			ChatUser user = userEntry.getValue();
			if (sendMyself == true || (sendMyself == false && user.getInternalIdx() != internalIdx)) {
				if (user != null) {
					user.postMessage(event);
				}
			}
		}
	}
	
	private void sendEventToAll(long internalIdx, Event event) {
		sendEventToAll(internalIdx, event, true);
	}

	private void sendEventToRoom(long internalIdx, Event event, boolean sendMyself) {
		ChatRoomManager room = chatRooms.get(event.getProgramIdx());
		if (room != null) {
			for (Long keyIndex : room.getInternalUsers()) {
				if (sendMyself == true || (sendMyself == false && internalIdx != keyIndex)) {
					ChatUser user = chatUsers.get(keyIndex);
					if (user != null) {
						user.postMessage(event);
					}
				}
			}
		}
	}
	
	private void sendEventToRoom(long internalIdx, Event event) {
		sendEventToRoom(internalIdx, event, true);
	}

	private void sendEventToPerson(long internalIdx, Event event) {
		ChatUser user = chatUsers.get(internalIdx);
		if (user != null) {
			try {
				user.postMessage(event);
			} catch (Exception e) {
				if (user.checkTimeout()) {
					try {
						leaveChatRoom(internalIdx, user.getProgramIdx(), null);
					} catch (Exception ex) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
		}
	}
	
	private void sendEventToPerson(int roomIdx, int userIdx, Event event) {
		ChatRoomManager room = chatRooms.get(roomIdx);
		if (room != null) {
			for (Long keyIndex : room.getInternalUsers()) {
				ChatUser user = chatUsers.get(keyIndex);
				if (userIdx == user.getUserIdx()) {
					sendEventToPerson(keyIndex, event);
				}
			}
		}
	}
	
	public void sendMessage(long internalIdx, Event event) throws Exception{
		// depends on room type
		ChatRoomManager room = chatRooms.get(event.getProgramIdx());
		if (room != null) { 
			ChatUser user;
			
			if (room.isBlackList(event.getFrom_userIdx())) {
				event.setType(EventType.BLOCKED_MSG);
				sendEventToPerson(internalIdx, event);
				return;
			}
					
			switch(room.getType()) {
			case RoomType.MANY_TO_MANY :
				sendEventToRoom(internalIdx, event);
				break;
			case RoomType.ONE_TO_MANY :
				user = chatUsers.get(internalIdx);
				if (user != null && user.isAdmin() == true) {
					// admin user : send to all user
					sendEventToRoom(internalIdx, event);
				}
				else {
					// normal user : send to admin
					sendEventToPerson(room.getProgramIdx(), room.getAdminIdx(), event);
					sendEventToPerson(room.getProgramIdx(), event.getFrom_userIdx(), event);
				}
				break;
			case RoomType.APPROVAL :
				user = chatUsers.get(internalIdx);
				if (user != null && user.isAdmin() == true) {
					// admin user : without approval
					sendEventToRoom(internalIdx, event);
				} else {
					// normal user : send approval request to admin
					event.setType(EventType.REQ_APPROVAL_MSG);
					sendEventToPerson(room.getProgramIdx(), room.getAdminIdx(), event);
					Event waitEvent = EventManager.cloneEvent(event);
					waitEvent.setType(EventType.WAIT_APPROVAL_MSG);
					sendEventToPerson(waitEvent.getProgramIdx(), waitEvent.getFrom_userIdx(), waitEvent);
				}
				break;
			}
		}
		else {
			throw new BadArgumentException();
		}
	}
 */
	/*public void sendEvent(long internalIdx, Event event) throws Exception {
		switch (event.getType()) {
		case EventType.NORMAL_MSG:
			sendMessage(internalIdx, event);
			break;
		case EventType.DIRECT_MSG:
			sendEventToPerson(event.getProgramIdx(), event.getTo_userIdx(), event);
			sendEventToPerson(internalIdx, event);
			break;
		case EventType.ADMIN_MSG:
			sendEventToRoom(internalIdx, event, true);
			break;
		case EventType.APPROVED_MSG: 
			log.debug("APPROVED_MSG");
			sendEventToPerson(event.getProgramIdx(), event.getFrom_userIdx(), event);
			Event newEvent = EventManager.cloneEvent(event);
			newEvent.setType(EventType.NORMAL_MSG);
			sendEventToRoom(internalIdx, newEvent);
			break;
		case EventType.REJECTED_MSG:
			log.debug("REJECTED_MSG");
			sendEventToPerson(event.getProgramIdx(), event.getFrom_userIdx(), event);
			break;
		case EventType.CREATE_CHATROOM:
			sendEventToAll(internalIdx, event);
			break;
		case EventType.REMOVE_CHATROOM:
			sendEventToAll(internalIdx, event);
			break;
		case EventType.ENTER_USER:
			sendEventToRoom(internalIdx, event, false);
			break;
		case EventType.LEAVE_USER:
			sendEventToRoom(internalIdx, event, false);
			break;
		case EventType.UPDATE_CHATROOM:
			sendEventToRoom(internalIdx, event, false);
			break;
		}
	}*/

	/*
	public void checkUsersTimeout() {
		Iterator<Entry<Long, ChatUser>> iter = chatUsers.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<Long, ChatUser> userEntry = iter.next();
			ChatUser user = userEntry.getValue();
			if (user != null) {
				if (user.checkTimeout()) {
					try {
						log.debug("timeout occurred : " + user.getUserIdx());
						leaveChatRoom(user.getInternalIdx(), user.getProgramIdx(), iter);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}*/
}
