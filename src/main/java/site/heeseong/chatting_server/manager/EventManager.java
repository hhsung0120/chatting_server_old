package site.heeseong.chatting_server.manager;


import site.heeseong.chatting_server.event_enum.EventType;
import site.heeseong.chatting_server.model.ChattingRoom;
import site.heeseong.chatting_server.model.Event;
import site.heeseong.chatting_server.model.Users;

public class EventManager {
	public static Event makeCreateRoomEvent(ChattingRoom chattingRoom) {
		Event event = new Event();
		
		event.setType(EventType.CREATE_CHATROOM.getValue());
		event.setProgramIdx(chattingRoom.getProgramIdx());
		event.setFrom_userIdx(chattingRoom.getUserIdx());
		event.setTo_userIdx(chattingRoom.getAdminIdx());
		event.setName(chattingRoom.getName());
		event.setMessage(chattingRoom.getDescription());
		
		return event;
	}
	
	public static Event makeUpdateRoomEvent(ChattingRoom chattingRoom) {
		Event event = new Event();
		
		event.setType(EventType.UPDATE_CHATROOM.getValue());
		event.setProgramIdx(chattingRoom.getProgramIdx());
		event.setFrom_userIdx(chattingRoom.getUserIdx());
		event.setTo_userIdx(chattingRoom.getAdminIdx());
		event.setName(chattingRoom.getName());
		event.setMessage(chattingRoom.getDescription());
		
		return event;
	}
	
	public static Event makeEnterRoomEvent(int roomIdx, Users chatroomUser) {
		Event event = new Event();
		
		event.setType(EventType.ENTER_USER.getValue());
		event.setProgramIdx(roomIdx);
		event.setFrom_userIdx((int)chatroomUser.getUserIdx());
		event.setUserId(chatroomUser.getUserId());
		event.setName(chatroomUser.getUserName());
		
		return event;
	}
	
	public static Event makeLeaveRoomEvent(int programIdx, int userIdx) {
		Event event = new Event();
		
		event.setType(EventType.LEAVE_USER.getValue());
		event.setProgramIdx(programIdx);
		event.setFrom_userIdx(userIdx);
		
		return event;
	}
	
	public static Event removeChatRoomEvent(int roomIdx) {
		Event event = new Event();
		
		event.setType(EventType.REMOVE_CHATROOM.getValue());
		event.setProgramIdx(roomIdx);
		
		return event;
	}
	
	public static Event cloneEvent(Event event) {
		Event newEvent = new Event();

		newEvent.setProgramIdx(event.getProgramIdx());
		newEvent.setFrom_userIdx(event.getFrom_userIdx());
		newEvent.setType(event.getType());
		if (event.getUserId() != null) {
			newEvent.setUserId(event.getUserId());
		}
		if (event.getName() != null) {
			newEvent.setName(event.getName());
		}
		newEvent.setTo_userIdx(event.getTo_userIdx());
		if (event.getMessage() != null) {
			newEvent.setMessage(event.getMessage());
		}
		return newEvent;
	}
}
