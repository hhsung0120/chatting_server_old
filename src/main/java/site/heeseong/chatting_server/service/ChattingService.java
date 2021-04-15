package site.heeseong.chatting_server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import site.heeseong.chatting_server.event_enum.ChattingRoomType;
import site.heeseong.chatting_server.event_enum.MessageEventType;
import site.heeseong.chatting_server.exceptions.*;
import site.heeseong.chatting_server.mapper.ChattingMapper;
import site.heeseong.chatting_server.model.*;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;


@Service
public class ChattingService {

	private ConcurrentHashMap<Integer, ChattingRoomData> chattingRooms = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Long, ChattingUserData> chattingUsers = new ConcurrentHashMap<>();
	private Object chattingRoomLock = new Object();
	private Object chattingUserLock = new Object();
	private long internalIndex = 0;

	private final ChattingMapper chattingMapper;

	@Autowired
	private ChattingService(ChattingMapper chattingMapper){
		this.chattingMapper = chattingMapper;
	}

	public ChattingRoom enterChattingRoom(ChattingRoom chattingRoom) throws Exception {
		ChattingUsers chattingUsers = new ChattingUsers(chattingRoom.getUserIdx(), chattingRoom.getUserId(), chattingRoom.getUserName(), chattingRoom.isAdmin());
		ChattingRoom resultChattingRoom = this.enterChattingRoom(chattingRoom, chattingUsers, true);

		MessageEvent roomMessageEvent = new MessageEvent(
				MessageEventType.ENTER_USER.getValue()
				, resultChattingRoom.getProgramIdx()
				, chattingUsers.getUserIdx()
				, chattingUsers.getUserIdx()
				, chattingUsers.getUserName()
				, ""
				, chattingUsers.getUserId());

		//chattingMapper.insertEvent(roomMessageEvent);
		return resultChattingRoom;
	}


	public ArrayList<ChattingRoom> listChatRooms(){
		ArrayList<ChattingRoom> roomList = new ArrayList<ChattingRoom>();

		for (Entry<Integer, ChattingRoomData> roomEntry : chattingRooms.entrySet()) {
			ChattingRoomData room = roomEntry.getValue();
			if (room != null) {
				roomList.add(room.getChattingRoom());
			}
		}

		return roomList;
	}

	public ChattingRoom getChatRoom(int roomIdx){
		ChattingRoomData room = chattingRooms.get(roomIdx);
		if (room != null) {
			return room.getChattingRoom();
		}
		return null;
	}

	public ArrayList<ChattingUsers> listUsers(int roomIdx){
		ArrayList<ChattingUsers> userList = new ArrayList<ChattingUsers>();

		ChattingRoomData chattingRoomData = chattingRooms.get(roomIdx);
		if (chattingRoomData == null) {
			return null;
		}

		for (Entry<Long, ChattingUsers> userEntry : chattingRoomData.getUserList().entrySet()) {
			userList.add(userEntry.getValue());
		}

		return userList;
	}

	public void leaveChatRoom(int programIdx, int userIdx, long internalIdx) throws Exception {
		this.goleaveChatRoom(internalIdx, programIdx, null);
		MessageEvent roomMessageEvent = new MessageEvent(MessageEventType.LEAVE_USER.getValue(), programIdx, userIdx, 0, "","","");
		chattingMapper.insertEvent(roomMessageEvent);
	}




	public void addBlackList(long internalIdx, int userIdx, int programIdx, long blackUserIdx) throws Exception {
		//TODO 메모리에 담는 구조임 현재, 디비에도 담고 꺼낼 수 있도록 개선 해야함
		this.addBlackList(internalIdx, programIdx, blackUserIdx);

		MessageEvent roomMessageEvent = new MessageEvent(MessageEventType.ADD_BLACKLIST.getValue(), programIdx, blackUserIdx, userIdx, "", "","");
		chattingMapper.insertEvent(roomMessageEvent);
	}

	public void removeBlackList(long internalIdx, int userIdx, int programIdx, long blackUserIdx) throws Exception {
		this.removeBlackList(internalIdx, programIdx, blackUserIdx);
		MessageEvent messageEvent = new MessageEvent(MessageEventType.REMOVE_BLACKLIST.getValue(), programIdx, blackUserIdx, userIdx, "", "","");
		chattingMapper.insertEvent(messageEvent);
	}

	public MessageEvent sendEvent(long internalIdx, MessageEvent messageEvent) throws Exception{
		chattingMapper.insertEvent(messageEvent);
		messageEvent.setIdx(messageEvent.getIdx());
		this.sendMessageEvent(internalIdx, messageEvent);

		return messageEvent;
	}

	public ArrayList<MessageEvent> getNewEvents(long internalIdx) throws Exception {
		ChattingUserData user = chattingUsers.get(internalIdx);
		if (user == null) {
			throw new UserNotExistException();
		}

		return user.getEvents();
	}

	private ChattingRoom enterChattingRoom(ChattingRoom chattingRoom, ChattingUsers chattingUsers, boolean notify) throws Exception {
		if (chattingUsers.getInternalIdx() != -1) {
			throw new BadArgumentException();
		}

		ChattingUserData chattingUserData = setChattingUser(chattingUsers);
		ChattingRoomData chattingRoomData = chattingRooms.get(chattingRoom.getProgramIdx());
		if (chattingRoomData == null) {
			chattingRoomData = createChattingRoom(chattingRoom, true);
		}

		if (chattingRoomData.addUser(chattingUserData.getChattingUsers()) == -1) {
			throw new UserExistException();
		}

		chattingUserData.setProgramIdx(chattingRoom.getProgramIdx());
		if (notify) {
			MessageEvent messageEvent = EventManager.makeEnterRoomEvent(chattingRoom.getProgramIdx(), chattingUsers);
			sendMessageEvent(chattingUserData.getInternalIdx(), messageEvent);
			chattingMapper.insertEvent(messageEvent);
		}

		chattingRoom.setInternalIdx(chattingUserData.getInternalIdx());
		return chattingRoom;
	}

	public int goleaveChatRoom(long internalIdx, int roomIdx, Iterator<Entry<Long, ChattingUserData>> userIteration) throws Exception {
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
				sendMessageEvent(internalIdx, messageEvent);
				chattingMapper.insertEvent(messageEvent);
			}
		}
		else {
			throw new BadArgumentException();
		}



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

	private void addBlackList(long internalIdx, int programIdx, long blackUser) throws Exception {
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

	private void removeBlackList(long internalIdx, int programIdx, long blackUser) throws Exception {
		if (programIdx != -1) {
			ChattingRoomData chatRoomManager = chattingRooms.get(programIdx);
			if (chatRoomManager == null) {
				throw new ChatRoomNotExistException();
			}
			checkAdmin(internalIdx);
			chatRoomManager.removeBlackList(blackUser);
		}
	}

	private void sendMessageEvent(long internalIdx, MessageEvent messageEvent) throws Exception {
		if(messageEvent.getMessageEventType() == MessageEventType.NORMAL_MSG.getValue()) {
			sendMessage(internalIdx, messageEvent);

		}else if(messageEvent.getMessageEventType() == MessageEventType.ENTER_USER.getValue()){
			sendEventToRoom(internalIdx, messageEvent, false);

		}else if(messageEvent.getMessageEventType() == MessageEventType.LEAVE_USER.getValue()){
			sendEventToRoom(internalIdx, messageEvent, false);

		}else if(messageEvent.getMessageEventType() == MessageEventType.APPROVED_MSG.getValue()){
			sendEventToPerson(messageEvent.getProgramIdx(), messageEvent.getFromUserIdx(), messageEvent);
			MessageEvent newMessageEvent = EventManager.cloneEvent(messageEvent);
			newMessageEvent.setMessageEventType(MessageEventType.NORMAL_MSG.getValue());
			sendEventToRoom(internalIdx, newMessageEvent);

		}else if(messageEvent.getMessageEventType() == MessageEventType.REJECTED_MSG.getValue()){
			sendEventToPerson(messageEvent.getProgramIdx(), messageEvent.getFromUserIdx(), messageEvent);

		}else if(messageEvent.getMessageEventType() == MessageEventType.DIRECT_MSG.getValue()){
			sendEventToPerson(messageEvent.getProgramIdx(), messageEvent.getToUserIdx(), messageEvent);
			sendEventToPerson(internalIdx, messageEvent);

		}else if(messageEvent.getMessageEventType() == MessageEventType.ADMIN_MSG.getValue()){
			sendEventToRoom(internalIdx, messageEvent, true);

		}else{
			throw new Exception();
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

	public ChattingUserData setChattingUser(ChattingUsers chattingUsers) {
		internalIndex++;
		chattingUsers.setInternalIdx(internalIndex);

		//자주 사용 되는 객체 이기 때문에 약한 참조 처리
		WeakReference<ChattingUserData> userRef = new WeakReference<>(new ChattingUserData(chattingUsers));
		ChattingUserData chattingUserData = userRef.get();

		synchronized (chattingUserLock) {
			this.chattingUsers.put(internalIndex, chattingUserData);
		}
		return chattingUserData;
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
				this.removeUser(internalIdx, userIteration);
			}

			if (chatRoomManager.getInternalUsers().size() == 0) {
				this.removeChatRoom(internalIdx, roomIdx);
			}
			else {
				MessageEvent messageEvent = EventManager.makeLeaveRoomEvent(roomIdx, user.getUserIdx());
				this.sendEvent(internalIdx, messageEvent);
				chattingMapper.insertEvent(messageEvent);
			}
		}
		else {
			throw new BadArgumentException();
		}



		return 0;
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

	private void checkAdmin(long internalIdx) throws Exception {
		ChattingUserData user = chattingUsers.get(internalIdx);
		if (user == null || user.isAdmin() != true) {
			throw new UnauthorizedException();
		}
	}

	private ChattingRoomData createChattingRoom(ChattingRoom chattingRoom, boolean log) throws Exception {
		ChattingRoomData chattingRoomData;

		synchronized (chattingRoomLock) {
			if (chattingRooms.get(chattingRoom.getProgramIdx()) != null) {
				throw new ChatRoomExistException();
			}

			WeakReference<ChattingRoomData> chatRoomRef = new WeakReference<ChattingRoomData>(new ChattingRoomData());
			chattingRoomData = chatRoomRef.get();
			chattingRoomData.setChattingRoom(chattingRoom);
			chattingRooms.put(chattingRoomData.getProgramIdx(), chattingRoomData);
		}

		if (log) {
			MessageEvent messageEvent = EventManager.makeCreateRoomEvent(chattingRoom);
			//sendEvent(internalIdx, event);
			chattingMapper.insertEvent(messageEvent);
		}

		return chattingRoomData;
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



	public void sendMessage(long internalIdx, MessageEvent messageEvent) throws Exception{

		ChattingRoomData room = chattingRooms.get(messageEvent.getProgramIdx());
		if (room != null) {
			ChattingUserData user;

			if (room.isBlackList(messageEvent.getFromUserIdx())) {
				messageEvent.setMessageEventType(MessageEventType.BLOCKED_MSG.getValue());
				sendEventToPerson(internalIdx, messageEvent);
				return;
			}

			if(room.getChattingRoomType() == ChattingRoomType.MANY_TO_MANY.getValue()){
				sendEventToRoom(internalIdx, messageEvent);
			}else if(room.getChattingRoomType() == ChattingRoomType.ONE_TO_MANY.getValue()){
				user = chattingUsers.get(internalIdx);
				if (user != null && user.isAdmin()) {
					sendEventToRoom(internalIdx, messageEvent);
				}else {
					sendEventToPerson(room.getProgramIdx(), room.getAdminIdx(), messageEvent);
					sendEventToPerson(room.getProgramIdx(), messageEvent.getFromUserIdx(), messageEvent);
				}
			}else if(room.getChattingRoomType() == ChattingRoomType.APPROVAL.getValue()){
				user = chattingUsers.get(internalIdx);
				if (user != null && user.isAdmin()) {
					// admin user : without approval
					sendEventToRoom(internalIdx, messageEvent);
				} else {
					// normal user : send approval request to admin
					messageEvent.setMessageEventType(MessageEventType.REQ_APPROVAL_MSG.getValue());
					sendEventToPerson(room.getProgramIdx(), room.getAdminIdx(), messageEvent);
					MessageEvent waitMessageEvent = EventManager.cloneEvent(messageEvent);
					waitMessageEvent.setMessageEventType(MessageEventType.WAIT_APPROVAL_MSG.getValue());
					sendEventToPerson(waitMessageEvent.getProgramIdx(), waitMessageEvent.getFromUserIdx(), waitMessageEvent);
				}
			}
		}else {
			throw new BadArgumentException();
		}
	}

	public void checkUsersTimeout() {
		Iterator<Entry<Long, ChattingUserData>> iter = chattingUsers.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<Long, ChattingUserData> userEntry = iter.next();
			ChattingUserData user = userEntry.getValue();
			if (user != null) {
				if (user.checkTimeout()) {
					try {
						leaveChatRoom(user.getInternalIdx(), user.getProgramIdx(), iter);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

}
