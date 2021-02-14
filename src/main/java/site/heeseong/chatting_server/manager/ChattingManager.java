package site.heeseong.chatting_server.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import site.heeseong.chatting_server.event_enum.EventType;
import site.heeseong.chatting_server.event_enum.RoomType;
import site.heeseong.chatting_server.exceptions.*;
import site.heeseong.chatting_server.mapper.ChattingMapper;
import site.heeseong.chatting_server.mapper.ContextMapper;
import site.heeseong.chatting_server.model.*;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
@Component
public class ChattingManager {

	//채팅방
	private ConcurrentHashMap<Integer, ChattingRoomManager> chattingRooms = new ConcurrentHashMap<Integer, ChattingRoomManager>();
	//채팅 유저
	private ConcurrentHashMap<Long, ChattingUser> chattingUsers = new ConcurrentHashMap<Long, ChattingUser>();
	//블락 채팅방
	private Object chattingRoomLock = new Object();
	//블락 유저
	private Object chattingUserLock = new Object();
	private long internalIndex = 0;

	final private ChattingMapper chattingMapper;
	final private ContextMapper contextMapper;
	public ChattingManager(ChattingMapper chattingMapper, ContextMapper contextMapper){
		this.chattingMapper = chattingMapper;
		this.contextMapper = contextMapper;
	}

	public EnterRoomResult enterChatRoom(ChattingRoom chattingRoom, Users users, boolean notify) throws Exception {
		if (users.getInternalIdx() != -1) {
			throw new BadArgumentException();
		}

		//유저 정보 -> 채팅 유저 정보로 변환
		ChattingUser chattingUser = createChattingUser(users);

		//채팅 방을 만들어주는 로직
		//프로그램 idx 유니크 키
		ChattingRoomManager chattingRoomManager = chattingRooms.get(chattingRoom.getProgramIdx());
		//채팅 룸이 존재 하지 않는다면 새로 생성 해야함
		if (chattingRoomManager == null) {
			chattingRoomManager = createChattingRoom(chattingRoom, true);
		}

		if (chattingRoomManager.addUser(chattingUser.getUser()) == -1) {
			throw new UserExistException();
		}

		chattingUser.setProgramIdx(chattingRoom.getProgramIdx());
		if (notify) {
			Event event = EventManager.makeEnterRoomEvent(chattingRoom.getProgramIdx(), users);
			chattingMapper.insertEvent(event);
		}

		//saveUsers();
		//saveRooms();

		EnterRoomResult newResult = new EnterRoomResult(chattingUser.getInternalIdx(), chattingRoomManager.getChattingRoomData());

		log.info("enter chatting user : " + chattingUsers);
		log.info("enter chatting room : " + chattingRooms);

		return newResult;
	}

	private ChattingRoomManager createChattingRoom(ChattingRoom chattingRoom, boolean log) throws Exception {
		ChattingRoomManager chattingRoomManager;

		synchronized (chattingRoomLock) {
			if (chattingRooms.get(chattingRoom.getProgramIdx()) != null) {
				throw new ChatRoomExistException();
			}

			ChattingRoomData newChattingRoomData = new ChattingRoomData();
			newChattingRoomData.setProgramIdx(chattingRoom.getProgramIdx());
			newChattingRoomData.setName(chattingRoom.getName());
			newChattingRoomData.setPassword(chattingRoom.getPassword());
			newChattingRoomData.setDescription(chattingRoom.getDescription());
			newChattingRoomData.setStatus(chattingRoom.getStatus());
			newChattingRoomData.setType(chattingRoom.getType());
			newChattingRoomData.setAdminIdx(chattingRoom.getAdminIdx());
			newChattingRoomData.setUserIdx(chattingRoom.getUserIdx());

			WeakReference<ChattingRoomManager> chatRoomRef = new WeakReference<ChattingRoomManager>(new ChattingRoomManager());
			chattingRoomManager = chatRoomRef.get();
			chattingRoomManager.setChattingRoomData(newChattingRoomData);

			//programIdx가 유니크키 이기 때문에 그 키 값으로 새로운 방 생성
			chattingRooms.put(chattingRoomManager.getProgramIdx(), chattingRoomManager);
		}

		if (log) {
			Event event = EventManager.makeCreateRoomEvent(chattingRoom);
			//		sendEvent(internalIdx, event);
			chattingMapper.insertEvent(event);
		}

		return chattingRoomManager;
	}
	public void saveRooms() {
		ContextDTO ctx = new ContextDTO();
		
		//Object to JSON in String
		ObjectMapper mapper = new ObjectMapper();
		
		ctx.setType("ROOM");
		try {
			String jsonInString = mapper.writeValueAsString(chattingRooms);
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
			String jsonInString = mapper.writeValueAsString(chattingUsers);
			log.debug("JSON - user : " + jsonInString);
			ctx.setContent(jsonInString);
			contextMapper.setContext(ctx);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/*
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
							ChatRoomManager currentRoom = createChattingRoom(newChatRoom, false);
							
							@SuppressWarnings("unchecked")
							List<Integer> users = (List<Integer>)chatRoom.get("users");
							if (users != null) {
								for (Integer userIdx : users) {
									for (Entry<Long, ChatUser> userEntry : chattingUsers.entrySet()) {
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
						createChattingUser(userInfo);
						
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
		
		log.debug("loaded user : " + chattingUsers);
		log.debug("loaded room : " + chattingRooms);
	}
	
	private void checkAdmin(long internalIdx) throws Exception {
		ChatUser user = chattingUsers.get(internalIdx);
		if (user == null || user.isAdmin() != true) {
			throw new UnauthorizedException();
		}
	}

	*/

	/*
	
	public ChatRoomData updateChatRoom(long internalIdx, site.heeseong.chatting.model.ChatRoom roomInfo) throws Exception {
		ChatRoomManager chatRoomManager = chattingRooms.get(roomInfo.getProgramIdx());
		
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

	*/
	private void removeChatRoom(long internalIdx, int roomIdx) throws Exception {
		ChattingRoomManager chatRoomManager = chattingRooms.get(roomIdx);
		if (chatRoomManager == null) {
			throw new ChatRoomNotExistException();
		}
		
		synchronized (chattingRoomLock) {
			chattingRooms.remove(roomIdx);
			chatRoomManager = null;
		}
		
		Event event = EventManager.removeChatRoomEvent(roomIdx);
		sendEvent(internalIdx, event);
		chattingMapper.insertEvent(event);
	}
	/*
	public ArrayList<ChatRoomData> getChatRoomList() {
		ArrayList<ChatRoomData> roomList = new ArrayList<ChatRoomData>();
		
		for (Entry<Integer, ChatRoomManager> roomEntry : chattingRooms.entrySet()) {
			ChatRoomManager room = roomEntry.getValue();
			if (room != null) {
				roomList.add(room.getChatRoom());
			}
		}
		
		return roomList;
	}
	
	public ChatRoomData getChatRoom(int roomIdx) {
		ChatRoomManager room = chattingRooms.get(roomIdx);
		if (room != null) {
			return room.getChatRoom();
		}
		return null;
	}
	*/

	/**
	 * 화면에서 넘겨준 유저 정보를 최종적으로 채팅할 유저에 대한 정보로 셋팅
	 * @param users
	 * @return
	 */
	public ChattingUser createChattingUser(Users users) {
		internalIndex++;

		users.setInternalIdx(internalIndex);

		//자주 사용 되는 객체 이기 때문에 약한 참조 처리
		WeakReference<ChattingUser> userRef = new WeakReference<ChattingUser>(new ChattingUser(users));
		ChattingUser chattingUser = userRef.get();

		//internalIndex 를 기준으로 채팅 유저를 제어해야 함
		synchronized (chattingUserLock) {
			//키, 벨류
			chattingUsers.put(internalIndex, chattingUser);
		}
		return chattingUser;
	}


	public int removeUser(long internalIdx, Iterator<Map.Entry<Long, ChattingUser>> userIteration) throws Exception {
		ChattingUser user = chattingUsers.get(internalIdx);
		if (user == null) {
			return -1;
		}
		
		user.removeAll();
		
		synchronized (chattingUserLock) {
			if (userIteration != null) {
				userIteration.remove();
			}
			else {
				chattingUsers.remove(internalIdx);
			}
			user = null;
		}
		return 0;
	}
	


	public int leaveChatRoom(long internalIdx, int roomIdx, Iterator<Map.Entry<Long, ChattingUser>> userIteration) throws Exception {
		if (roomIdx != -1) {
			ChattingRoomManager chatRoomManager = chattingRooms.get(roomIdx);
			if (chatRoomManager == null) {
				throw new ChatRoomNotExistException();
			}
	
			chatRoomManager.removeUser(internalIdx);
			
			ChattingUser user = chattingUsers.get(internalIdx);
			if (user != null) {
				removeUser(internalIdx, userIteration);
			}
			
			if (chatRoomManager.getInternalUsers().size() == 0) {
				removeChatRoom(internalIdx, roomIdx);
			}
			else {
				Event event = EventManager.makeLeaveRoomEvent(roomIdx, user.getUserIdx());
				sendEvent(internalIdx, event);
				chattingMapper.insertEvent(event);
			}
			
			saveUsers();
			saveRooms();
		}
		else {
			throw new BadArgumentException();
		}
		
		log.debug("leaveChatRoom user : " + chattingUsers);
		log.debug("leaveChatRoom room : " + chattingRooms);

		return 0;
	}
	/*
	public Integer[] getBlackList(long internalIdx, int roomIdx) throws Exception {
		ChatRoomManager chatRoomManager = chattingRooms.get(roomIdx);
		if (chatRoomManager == null) {
			return null;
		}
		
		checkAdmin(internalIdx);
		
		return chatRoomManager.getBlackListArray();
	}
	
	public void addBlackList(long internalIdx, int programIdx, int blackUser) throws Exception {
		if (programIdx != -1) {
			ChatRoomManager chatRoomManager = chattingRooms.get(programIdx);
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
			ChatRoomManager chatRoomManager = chattingRooms.get(programIdx);
			if (chatRoomManager == null) {
				throw new ChatRoomNotExistException();
			}
			
			checkAdmin(internalIdx);
			
			chatRoomManager.removeBlackList(blackUser);
			saveRooms();
		}
	}
	*/
	public ArrayList<Users> getUserList(int roomIdx) {
		ArrayList<Users> userList = new ArrayList<Users>();
		
		ChattingRoomManager chattingRoomManager = chattingRooms.get(roomIdx);
		if (chattingRoomManager == null) {
			return null;
		}
		for (Map.Entry<Long, Users> userEntry : chattingRoomManager.getUserList().entrySet()) {
			
			userList.add(userEntry.getValue());
		}
		return userList;
	}

	public ArrayList<Event> getNewEvents(long internalIdx) throws Exception {
		ChattingUser user = chattingUsers.get(internalIdx);
		if (user == null) {
			log.debug("getNewEvents : NO User : " + internalIdx);
			throw new UserNotExistException();
		}
		
		return user.getEvents();
	}
	/*
	private void sendEventToAll(long internalIdx, Event event, boolean sendMyself) {
		for (Entry<Long, ChatUser> userEntry : chattingUsers.entrySet()) {
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

*/
	private void sendEventToRoom(long internalIdx, Event event, boolean sendMyself) {
		ChattingRoomManager room = chattingRooms.get(event.getProgramIdx());
		if (room != null) {
			for (Long keyIndex : room.getInternalUsers()) {
				if (sendMyself == true || (sendMyself == false && internalIdx != keyIndex)) {
					ChattingUser user = chattingUsers.get(keyIndex);
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
		ChattingUser user = chattingUsers.get(internalIdx);
		if (user != null) {
			try {
				user.postMessage(event);
			} catch (Exception e) {
				if (user.checkTimeout()) {
					try {
						leaveChatRoom(internalIdx, user.getProgramIdx(), null);
					} catch (Exception ex) {
						e.printStackTrace();
					}
				}

			}
		}
	}

	private void sendEventToPerson(int roomIdx, long userIdx, Event event) {
		ChattingRoomManager room = chattingRooms.get(roomIdx);
		if (room != null) {
			for (Long keyIndex : room.getInternalUsers()) {
				ChattingUser user = chattingUsers.get(keyIndex);
				if (userIdx == user.getUserIdx()) {
					sendEventToPerson(keyIndex, event);
				}
			}
		}
	}

	public void sendMessage(long internalIdx, Event event) throws Exception{

		ChattingRoomManager room = chattingRooms.get(event.getProgramIdx());
		if (room != null) { 
			ChattingUser user;
			
			if (room.isBlackList(event.getFrom_userIdx())) {
				event.setType(EventType.BLOCKED_MSG.getValue());
				sendEventToPerson(internalIdx, event);
				return;
			}

			if(room.getType() == RoomType.MANY_TO_MANY.getValue()){
				sendEventToRoom(internalIdx, event);
			}else if(room.getType() == RoomType.ONE_TO_MANY.getValue()){
				user = chattingUsers.get(internalIdx);
				if (user != null && user.isAdmin() == true) {
					// admin user : send to all user
					sendEventToRoom(internalIdx, event);
				}
				else {
					// normal user : send to admin
					sendEventToPerson(room.getProgramIdx(), room.getAdminIdx(), event);
					sendEventToPerson(room.getProgramIdx(), event.getFrom_userIdx(), event);
				}
			}else if(room.getType() == RoomType.APPROVAL.getValue()){
				user = chattingUsers.get(internalIdx);
				if (user != null && user.isAdmin() == true) {
					// admin user : without approval
					sendEventToRoom(internalIdx, event);
				} else {
					// normal user : send approval request to admin
					event.setType(EventType.REQ_APPROVAL_MSG.getValue());
					sendEventToPerson(room.getProgramIdx(), room.getAdminIdx(), event);
					Event waitEvent = EventManager.cloneEvent(event);
					waitEvent.setType(EventType.WAIT_APPROVAL_MSG.getValue());
					sendEventToPerson(waitEvent.getProgramIdx(), waitEvent.getFrom_userIdx(), waitEvent);
				}
			}
			/*switch(room.getType()) {
			case RoomType.MANY_TO_MANY :
				sendEventToRoom(internalIdx, event);
				break;
			case RoomType.ONE_TO_MANY :
				user = chattingUsers.get(internalIdx);
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
				user = chattingUsers.get(internalIdx);
				if (user != null && user.isAdmin() == true) {
					// admin user : without approval
					sendEventToRoom(internalIdx, event);
				} else {
					// normal user : send approval request to admin
					event.setType(EventType.REQ_APPROVAL_MSG.getValue());
					sendEventToPerson(room.getProgramIdx(), room.getAdminIdx(), event);
					Event waitEvent = EventManager.cloneEvent(event);
					waitEvent.setType(EventType.WAIT_APPROVAL_MSG.getValue());
					sendEventToPerson(waitEvent.getProgramIdx(), waitEvent.getFrom_userIdx(), waitEvent);
				}
				break;
			}*/
		}else {
			throw new BadArgumentException();
		}
	}

	public void sendEvent(long internalIdx, Event event) throws Exception {

		if(event.getType() == EventType.NORMAL_MSG.getValue()){
			sendMessage(internalIdx, event);
		}
		switch (event.getType()) {
		/*case EventType.NORMAL_MSG:
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
			break;*/
		}
	}

	/*
	public void checkUsersTimeout() {
		Iterator<Entry<Long, ChatUser>> iter = `chattingUsers`.entrySet().iterator();
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
