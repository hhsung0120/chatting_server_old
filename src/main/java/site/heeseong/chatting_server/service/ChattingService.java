package site.heeseong.chatting_server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import site.heeseong.chatting_server.event_enum.MessageEventType;
import site.heeseong.chatting_server.component.ChattingEventManager;
import site.heeseong.chatting_server.mapper.ChattingMapper;
import site.heeseong.chatting_server.model.ChattingRoom;
import site.heeseong.chatting_server.model.MessageEvent;
import site.heeseong.chatting_server.model.Users;

import java.util.ArrayList;


@Service
public class ChattingService {

	private final ChattingEventManager chattingEventManagerService;
	private final ChattingMapper chattingMapper;

	@Autowired
	private ChattingService(ChattingEventManager chattingEventManagerService, ChattingMapper chattingMapper){
		this.chattingEventManagerService = chattingEventManagerService;
		this.chattingMapper = chattingMapper;
	}

	public ChattingRoom enterChattingRoom(ChattingRoom chattingRoom) throws Exception {
		Users users = new Users(chattingRoom.getUserIdx(), chattingRoom.getUserId(), chattingRoom.getUserName(), chattingRoom.isAdmin());
		ChattingRoom resultChattingRoom = chattingEventManagerService.enterChattingRoom(chattingRoom, users, true);

		MessageEvent roomMessageEvent = new MessageEvent(
				MessageEventType.ENTER_USER.getValue()
				, resultChattingRoom.getProgramIdx()
				, users.getUserIdx()
				, -1
				, users.getUserId()
				, users.getUserName()
				, resultChattingRoom.getName() + "_" + resultChattingRoom.getDescription(), "");

		chattingMapper.insertEvent(roomMessageEvent);
		return resultChattingRoom;
	}


	public ArrayList<ChattingRoom> listChatRooms(){
		return chattingEventManagerService.getChatRoomList();
	}

	public ChattingRoom getChatRoom(int roomIdx){
		return chattingEventManagerService.getChatRoom(roomIdx);
	}

	public ArrayList<Users> listUsers(int roomIdx){
		return chattingEventManagerService.getUserList(roomIdx);
	}

	public void leaveChatRoom(int programIdx, int userIdx, long internalIdx) throws Exception {
		chattingEventManagerService.leaveChatRoom(internalIdx, programIdx, null);

		MessageEvent roomMessageEvent = new MessageEvent(MessageEventType.LEAVE_USER.getValue(), programIdx, userIdx, -1, "", "", "","");
		chattingMapper.insertEvent(roomMessageEvent);
	}


	public Long[] getBlackList(long internalIdx, int roomIdx) throws Exception {
		return chattingEventManagerService.getBlackList(internalIdx, roomIdx);
	}

	public void addBlackList(long internalIdx, int userIdx, int programIdx, long blackUserIdx) throws Exception {
		//TODO 메모리에 담는 구조임 현재, 디비에도 담고 꺼낼 수 있도록 개선 해야함
		chattingEventManagerService.addBlackList(internalIdx, programIdx, blackUserIdx);

		MessageEvent roomMessageEvent = new MessageEvent(MessageEventType.ADD_BLACKLIST.getValue(), programIdx, userIdx, blackUserIdx, "", "", "","");
		chattingMapper.insertEvent(roomMessageEvent);
	}

	public void removeBlackList(long internalIdx, int userIdx, int programIdx, long blackUserIdx) throws Exception {
		chattingEventManagerService.removeBlackList(internalIdx, programIdx, blackUserIdx);

		MessageEvent roomMessageEvent = new MessageEvent(MessageEventType.REMOVE_BLACKLIST.getValue(), programIdx, userIdx, blackUserIdx, "", "", "","");
		chattingMapper.insertEvent(roomMessageEvent);
	}

	public MessageEvent sendEvent(long internalIdx, MessageEvent chatDTO) throws Exception{
		chattingMapper.insertEvent(chatDTO);
		chatDTO.setIdx(chatDTO.getIdx());
		chattingEventManagerService.sendEvent(internalIdx, chatDTO);
		
		return chatDTO;
	}

	public ArrayList<MessageEvent> getNewEvents(long internalIdx) throws Exception {
		return chattingEventManagerService.getNewEvents(internalIdx);
	}
}
