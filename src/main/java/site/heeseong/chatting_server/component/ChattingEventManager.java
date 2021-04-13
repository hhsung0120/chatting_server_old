package site.heeseong.chatting_server.component;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import site.heeseong.chatting_server.event_enum.MessageEventType;
import site.heeseong.chatting_server.event_enum.ChattingRoomType;
import site.heeseong.chatting_server.exceptions.*;
import site.heeseong.chatting_server.mapper.ChattingMapper;
import site.heeseong.chatting_server.model.*;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
@Component
public class ChattingEventManager {

	private ConcurrentHashMap<Integer, ChattingRoomData> chattingRooms = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Long, ChattingUserData> chattingUsers = new ConcurrentHashMap<>();
	private Object chattingRoomLock = new Object();
	private Object chattingUserLock = new Object();
	private long internalIndex = 0;

	final private ChattingMapper chattingMapper;
	public ChattingEventManager(ChattingMapper chattingMapper){
		this.chattingMapper = chattingMapper;
	}

	public ChattingRoom enterChattingRoom(ChattingRoom chattingRoom, Users users, boolean notify) throws Exception {
		if (users.getInternalIdx() != -1) {
			throw new BadArgumentException();
		}

		ChattingUserData chattingUserData = setChattingUser(users);

		ChattingRoomData chattingRoomData = chattingRooms.get(chattingRoom.getProgramIdx());
		if (chattingRoomData == null) {
			chattingRoomData = createChattingRoom(chattingRoom, true);
		}

		if (chattingRoomData.addUser(chattingUserData.getUsers()) == -1) {
			throw new UserExistException();
		}

		chattingUserData.setProgramIdx(chattingRoom.getProgramIdx());
		if (notify) {
			MessageEvent messageEvent = EventManager.makeEnterRoomEvent(chattingRoom.getProgramIdx(), users);
			sendEvent(chattingUserData.getInternalIdx(), messageEvent);
			chattingMapper.insertEvent(messageEvent);
		}

		chattingRoom.setInternalIdx(chattingUserData.getInternalIdx());
		return chattingRoom;
	}

	private ChattingRoomData createChattingRoom(ChattingRoom chattingRoom, boolean log) throws Exception {
		ChattingRoomData chattingRoomData;

		synchronized (chattingRoomLock) {
			if (chattingRooms.get(chattingRoom.getProgramIdx()) != null) {
				throw new ChatRoomExistException();
			}

			ChattingRoom newChattingRoomData = new ChattingRoom();
			newChattingRoomData.setProgramIdx(chattingRoom.getProgramIdx());
			newChattingRoomData.setName(chattingRoom.getName());
			newChattingRoomData.setPassword(chattingRoom.getPassword());
			newChattingRoomData.setDescription(chattingRoom.getDescription());
			newChattingRoomData.setStatus(chattingRoom.getStatus());
			newChattingRoomData.setType(chattingRoom.getType());
			newChattingRoomData.setAdminIdx(chattingRoom.getAdminIdx());
			newChattingRoomData.setUserIdx(chattingRoom.getUserIdx());

			WeakReference<ChattingRoomData> chatRoomRef = new WeakReference<ChattingRoomData>(new ChattingRoomData());
			chattingRoomData = chatRoomRef.get();
			chattingRoomData.setChattingRoom(newChattingRoomData);

			//programIdx가 유니크키 이기 때문에 그 키 값으로 새로운 방 생성
			chattingRooms.put(chattingRoomData.getProgramIdx(), chattingRoomData);
		}

		if (log) {
			MessageEvent messageEvent = EventManager.makeCreateRoomEvent(chattingRoom);
			//sendEvent(internalIdx, event);
			chattingMapper.insertEvent(messageEvent);
		}

		return chattingRoomData;
	}

	private void checkAdmin(long internalIdx) throws Exception {
		ChattingUserData user = chattingUsers.get(internalIdx);
		if (user == null || user.isAdmin() != true) {
			throw new UnauthorizedException();
		}
	}

	private void removeChatRoom(long internalIdx, int roomIdx) throws Exception {
		ChattingRoomData chatRoomManager = chattingRooms.get(roomIdx);
		if (chatRoomManager == null) {
			throw new ChatRoomNotExistException();
		}
		
		synchronized (chattingRoomLock) {
			chattingRooms.remove(roomIdx);
			chatRoomManager = null;
		}
		
		MessageEvent messageEvent = EventManager.removeChatRoomEvent(roomIdx);
		sendEvent(internalIdx, messageEvent);
		chattingMapper.insertEvent(messageEvent);
	}

	public ArrayList<ChattingRoom> getChatRoomList() {
		ArrayList<ChattingRoom> roomList = new ArrayList<ChattingRoom>();
		
		for (Entry<Integer, ChattingRoomData> roomEntry : chattingRooms.entrySet()) {
			ChattingRoomData room = roomEntry.getValue();
			if (room != null) {
				roomList.add(room.getChattingRoom());
			}
		}
		
		return roomList;
	}

	public ChattingRoom getChatRoom(int roomIdx) {
		ChattingRoomData room = chattingRooms.get(roomIdx);
		if (room != null) {
			return room.getChattingRoom();
		}
		return null;
	}





	public int removeUser(long internalIdx, Iterator<Entry<Long, ChattingUserData>> userIteration) throws Exception {
		ChattingUserData user = chattingUsers.get(internalIdx);
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
	


	public int leaveChatRoom(long internalIdx, int roomIdx, Iterator<Entry<Long, ChattingUserData>> userIteration) throws Exception {
		if (roomIdx != -1) {
			ChattingRoomData chatRoomManager = chattingRooms.get(roomIdx);
			if (chatRoomManager == null) {
				throw new ChatRoomNotExistException();
			}
	
			chatRoomManager.removeUser(internalIdx);
			
			ChattingUserData user = chattingUsers.get(internalIdx);
			if (user != null) {
				removeUser(internalIdx, userIteration);
			}
			
			if (chatRoomManager.getInternalUsers().size() == 0) {
				removeChatRoom(internalIdx, roomIdx);
			}
			else {
				MessageEvent messageEvent = EventManager.makeLeaveRoomEvent(roomIdx, user.getUserIdx());
				sendEvent(internalIdx, messageEvent);
				chattingMapper.insertEvent(messageEvent);
			}
		}
		else {
			throw new BadArgumentException();
		}
		
		log.debug("leaveChatRoom user : " + chattingUsers);
		log.debug("leaveChatRoom room : " + chattingRooms);

		return 0;
	}

	public Long[] getBlackList(long internalIdx, int roomIdx) throws Exception {
		//개별 채팅방 안에 유저가 담기도록 개선해야함
		ChattingRoomData chatRoomManager = chattingRooms.get(roomIdx);
		if (chatRoomManager == null) {
			return null;
		}
		
		checkAdmin(internalIdx);
		
		return chatRoomManager.getBlackListArray();
	}
	
	public void addBlackList(long internalIdx, int programIdx, long blackUser) throws Exception {
		if (programIdx != -1) {
			ChattingRoomData chatRoomManager = chattingRooms.get(programIdx);
			if (chatRoomManager == null) {
				throw new ChatRoomNotExistException();
			}

			checkAdmin(internalIdx);
			chatRoomManager.addBlackList(blackUser);
		}
		else {
			throw new BadArgumentException();
		}
	}
	
	public void removeBlackList(long internalIdx, int programIdx, long blackUser) throws Exception {
		if (programIdx != -1) {
			ChattingRoomData chatRoomManager = chattingRooms.get(programIdx);
			if (chatRoomManager == null) {
				throw new ChatRoomNotExistException();
			}
			
			checkAdmin(internalIdx);
			
			chatRoomManager.removeBlackList(blackUser);
		}
	}

	public ArrayList<Users> getUserList(int roomIdx) {
		ArrayList<Users> userList = new ArrayList<Users>();
		
		ChattingRoomData chattingRoomData = chattingRooms.get(roomIdx);
		if (chattingRoomData == null) {
			return null;
		}

		for (Entry<Long, Users> userEntry : chattingRoomData.getUserList().entrySet()) {
			userList.add(userEntry.getValue());
		}

		return userList;
	}

	public ArrayList<MessageEvent> getNewEvents(long internalIdx) throws Exception {
		ChattingUserData user = chattingUsers.get(internalIdx);
		System.out.println(user.toString());
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
	private void sendEventToRoom(long internalIdx, MessageEvent messageEvent, boolean sendMyself) {
		ChattingRoomData room = chattingRooms.get(messageEvent.getProgramIdx());
		if (room != null) {
			for (Long keyIndex : room.getInternalUsers()) {
				if (sendMyself == true || (sendMyself == false && internalIdx != keyIndex)) {
					ChattingUserData user = chattingUsers.get(keyIndex);
					if (user != null) {
						user.postMessage(messageEvent);
					}
				}
			}
		}
	}
	
	private void sendEventToRoom(long internalIdx, MessageEvent messageEvent) {
		sendEventToRoom(internalIdx, messageEvent, true);
	}

	private void sendEventToPerson(long internalIdx, MessageEvent messageEvent) {
		ChattingUserData user = chattingUsers.get(internalIdx);
		if (user != null) {
			try {
				//메시지 보내는 구간
				user.postMessage(messageEvent);
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

	private void sendEventToPerson(int roomIdx, long userIdx, MessageEvent messageEvent) {
		ChattingRoomData room = chattingRooms.get(roomIdx);
		if (room != null) {
			for (Long keyIndex : room.getInternalUsers()) {
				ChattingUserData user = chattingUsers.get(keyIndex);
				if (userIdx == user.getUserIdx()) {
					sendEventToPerson(keyIndex, messageEvent);
				}
			}
		}
	}

	public void sendMessage(long internalIdx, MessageEvent messageEvent) throws Exception{

		ChattingRoomData room = chattingRooms.get(messageEvent.getProgramIdx());
		if (room != null) { 
			ChattingUserData user;

			if (room.isBlackList(messageEvent.getFromUserIdx())) {
				messageEvent.setType(MessageEventType.BLOCKED_MSG.getValue());
				sendEventToPerson(internalIdx, messageEvent);
				return;
			}

			if(room.getType() == ChattingRoomType.MANY_TO_MANY.getValue()){
				sendEventToRoom(internalIdx, messageEvent);
			}else if(room.getType() == ChattingRoomType.ONE_TO_MANY.getValue()){
				user = chattingUsers.get(internalIdx);
				if (user != null && user.isAdmin()) {
					sendEventToRoom(internalIdx, messageEvent);
				}else {
					sendEventToPerson(room.getProgramIdx(), room.getAdminIdx(), messageEvent);
					sendEventToPerson(room.getProgramIdx(), messageEvent.getFromUserIdx(), messageEvent);
				}
			}else if(room.getType() == ChattingRoomType.APPROVAL.getValue()){
				user = chattingUsers.get(internalIdx);
				if (user != null && user.isAdmin()) {
					// admin user : without approval
					sendEventToRoom(internalIdx, messageEvent);
				} else {
					// normal user : send approval request to admin
					messageEvent.setType(MessageEventType.REQ_APPROVAL_MSG.getValue());
					sendEventToPerson(room.getProgramIdx(), room.getAdminIdx(), messageEvent);
					MessageEvent waitMessageEvent = EventManager.cloneEvent(messageEvent);
					waitMessageEvent.setType(MessageEventType.WAIT_APPROVAL_MSG.getValue());
					sendEventToPerson(waitMessageEvent.getProgramIdx(), waitMessageEvent.getFromUserIdx(), waitMessageEvent);
				}
			}
		}else {
			throw new BadArgumentException();
		}
	}

	public void sendEvent(long internalIdx, MessageEvent messageEvent) throws Exception {
		if(messageEvent.getType() == MessageEventType.NORMAL_MSG.getValue()) {
			sendMessage(internalIdx, messageEvent);
		}else if(messageEvent.getType() == MessageEventType.ENTER_USER.getValue()){
			sendEventToRoom(internalIdx, messageEvent, false);
		}else if(messageEvent.getType() == MessageEventType.LEAVE_USER.getValue()){
			sendEventToRoom(internalIdx, messageEvent, false);
		}else if(messageEvent.getType() == MessageEventType.APPROVED_MSG.getValue()){
			sendEventToPerson(messageEvent.getProgramIdx(), messageEvent.getFromUserIdx(), messageEvent);
			MessageEvent newMessageEvent = EventManager.cloneEvent(messageEvent);
			newMessageEvent.setType(MessageEventType.NORMAL_MSG.getValue());
			sendEventToRoom(internalIdx, newMessageEvent);
		}else if(messageEvent.getType() == MessageEventType.REJECTED_MSG.getValue()){
			sendEventToPerson(messageEvent.getProgramIdx(), messageEvent.getFromUserIdx(), messageEvent);
		}else if(messageEvent.getType() == MessageEventType.DIRECT_MSG.getValue()){
			sendEventToPerson(messageEvent.getProgramIdx(), messageEvent.getToUserIdx(), messageEvent);
			sendEventToPerson(internalIdx, messageEvent);
		}else if(messageEvent.getType() == MessageEventType.ADMIN_MSG.getValue()){
			sendEventToRoom(internalIdx, messageEvent, true);
		}

		/*switch (event.getType()) {
		case EventType.CREATE_CHATROOM:
			sendEventToAll(internalIdx, event);
			break;
		case EventType.REMOVE_CHATROOM:
			sendEventToAll(internalIdx, event);
			break;
		}*/
	}


	public void checkUsersTimeout() {
		Iterator<Entry<Long, ChattingUserData>> iter = chattingUsers.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<Long, ChattingUserData> userEntry = iter.next();
			ChattingUserData user = userEntry.getValue();
			if (user != null) {
				if (user.checkTimeout()) {
					try {
						log.debug("timeout occurred : " + user.getUserIdx());
						leaveChatRoom(user.getInternalIdx(), user.getProgramIdx(), iter);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public ChattingUserData setChattingUser(Users users) {
		internalIndex++;
		users.setInternalIdx(internalIndex);

		//자주 사용 되는 객체 이기 때문에 약한 참조 처리
		WeakReference<ChattingUserData> userRef = new WeakReference<>(new ChattingUserData(users));
		ChattingUserData chattingUserData = userRef.get();

		//internalIndex 를 기준으로 채팅 유저를 제어해야 함
		synchronized (chattingUserLock) {
			chattingUsers.put(internalIndex, chattingUserData);
		}
		return chattingUserData;
	}
}
