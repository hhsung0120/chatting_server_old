package site.heeseong.chatting_server.model;


import site.heeseong.chatting_server.event_enum.MessageEventType;

public class EventManager {
	public static MessageEvent makeCreateRoomEvent(ChattingRoom chattingRoom) {
		MessageEvent messageEvent = new MessageEvent();
		
		messageEvent.setType(MessageEventType.CREATE_CHATROOM.getValue());
		messageEvent.setProgramIdx(chattingRoom.getProgramIdx());
		messageEvent.setFrom_userIdx(chattingRoom.getUserIdx());
		messageEvent.setTo_userIdx(chattingRoom.getAdminIdx());
		messageEvent.setName(chattingRoom.getName());
		messageEvent.setMessage(chattingRoom.getDescription());
		
		return messageEvent;
	}

	public static MessageEvent makeEnterRoomEvent(int roomIdx, ChattingUsers chatroomUser) {
		MessageEvent messageEvent = new MessageEvent();
		
		messageEvent.setType(MessageEventType.ENTER_USER.getValue());
		messageEvent.setProgramIdx(roomIdx);
		messageEvent.setFrom_userIdx((int)chatroomUser.getUserIdx());
		messageEvent.setFromUserIdx((int)chatroomUser.getUserIdx());
		messageEvent.setUserId(chatroomUser.getUserId());
		messageEvent.setName(chatroomUser.getUserName());
		
		return messageEvent;
	}
	
	public static MessageEvent makeLeaveRoomEvent(int programIdx, long userIdx) {
		MessageEvent messageEvent = new MessageEvent();
		
		messageEvent.setType(MessageEventType.LEAVE_USER.getValue());
		messageEvent.setProgramIdx(programIdx);
		messageEvent.setFrom_userIdx(userIdx);
		
		return messageEvent;
	}
	
	public static MessageEvent removeChatRoomEvent(int roomIdx) {
		MessageEvent messageEvent = new MessageEvent();
		
		messageEvent.setType(MessageEventType.REMOVE_CHATROOM.getValue());
		messageEvent.setProgramIdx(roomIdx);
		
		return messageEvent;
	}
	
	public static MessageEvent cloneEvent(MessageEvent messageEvent) {
		MessageEvent newMessageEvent = new MessageEvent();

		newMessageEvent.setProgramIdx(messageEvent.getProgramIdx());
		newMessageEvent.setFrom_userIdx(messageEvent.getFrom_userIdx());
		newMessageEvent.setType(messageEvent.getType());
		if (messageEvent.getUserId() != null) {
			newMessageEvent.setUserId(messageEvent.getUserId());
		}
		if (messageEvent.getName() != null) {
			newMessageEvent.setName(messageEvent.getName());
		}
		newMessageEvent.setTo_userIdx(messageEvent.getTo_userIdx());
		if (messageEvent.getMessage() != null) {
			newMessageEvent.setMessage(messageEvent.getMessage());
		}
		return newMessageEvent;
	}
}
