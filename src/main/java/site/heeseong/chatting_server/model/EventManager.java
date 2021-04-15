package site.heeseong.chatting_server.model;


import site.heeseong.chatting_server.event_enum.MessageEventType;

public class EventManager {
	public static MessageEvent makeCreateRoomEvent(ChattingRoom chattingRoom) {
		MessageEvent messageEvent = new MessageEvent();
		messageEvent.setMessageEventType(MessageEventType.CREATE_CHATROOM.getValue());
		messageEvent.setProgramIdx(chattingRoom.getProgramIdx());
		messageEvent.setToUserIdx(0);
		messageEvent.setFromUserIdx(chattingRoom.getAdminIdx());
		messageEvent.setUserName(chattingRoom.getName());
		messageEvent.setMessage(chattingRoom.getDescription());
		
		return messageEvent;
	}

	public static MessageEvent makeEnterRoomEvent(int roomIdx, ChattingUsers chatroomUser) {
		MessageEvent messageEvent = new MessageEvent();
		messageEvent.setMessageEventType(MessageEventType.ENTER_USER.getValue());
		messageEvent.setProgramIdx(roomIdx);
		messageEvent.setToUserIdx(0);
		messageEvent.setFromUserIdx(chatroomUser.getUserIdx());
		messageEvent.setUserId(chatroomUser.getUserId());
		messageEvent.setUserName(chatroomUser.getUserName());
		
		return messageEvent;
	}
	
	public static MessageEvent makeLeaveRoomEvent(int programIdx, long userIdx) {
		MessageEvent messageEvent = new MessageEvent();
		messageEvent.setMessageEventType(MessageEventType.LEAVE_USER.getValue());
		messageEvent.setProgramIdx(programIdx);
		messageEvent.setFromUserIdx(userIdx);
		
		return messageEvent;
	}
	
	public static MessageEvent removeChatRoomEvent(int roomIdx) {
		MessageEvent messageEvent = new MessageEvent();
		messageEvent.setMessageEventType(MessageEventType.REMOVE_CHATROOM.getValue());
		messageEvent.setProgramIdx(roomIdx);
		
		return messageEvent;
	}
	
	public static MessageEvent cloneEvent(MessageEvent messageEvent) {
		MessageEvent newMessageEvent = new MessageEvent();
		newMessageEvent.setProgramIdx(messageEvent.getProgramIdx());
		newMessageEvent.setFromUserIdx(messageEvent.getFromUserIdx());
		newMessageEvent.setMessageEventType(messageEvent.getMessageEventType());
		if (messageEvent.getUserId() != null) {
			newMessageEvent.setUserId(messageEvent.getUserId());
		}
		if (messageEvent.getUserName() != null) {
			newMessageEvent.setUserName(messageEvent.getUserName());
		}
		newMessageEvent.setToUserIdx(messageEvent.getToUserIdx());
		if (messageEvent.getMessage() != null) {
			newMessageEvent.setMessage(messageEvent.getMessage());
		}

		return newMessageEvent;
	}
}
