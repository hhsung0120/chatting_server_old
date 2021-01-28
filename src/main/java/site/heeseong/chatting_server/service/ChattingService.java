package site.heeseong.chatting_server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import site.heeseong.chatting_server.event_enum.EventType;
import site.heeseong.chatting_server.manager.ChattingManager;
import site.heeseong.chatting_server.mapper.ChattingMapper;
import site.heeseong.chatting_server.model.ChattingRoom;
import site.heeseong.chatting_server.model.EnterRoomResult;
import site.heeseong.chatting_server.model.Event;
import site.heeseong.chatting_server.model.Users;


@Service
public class ChattingService {

	final private ChattingMapper chattingMapper;
	final private ChattingManager chattingManagerService;

	@Autowired
	private ChattingService(ChattingMapper chattingMapper, ChattingManager chattingManagerService){
		this.chattingMapper = chattingMapper;
		this.chattingManagerService = chattingManagerService;
	}

	public EnterRoomResult enterChatRoom(ChattingRoom chattingRoom, Users users) throws Exception {
		EnterRoomResult enterRoomResult = chattingManagerService.enterChatRoom(chattingRoom, users, true);

		Event roomEvent = new Event(
				EventType.ENTER_USER.getValue()
				, enterRoomResult.getProgramIdx()
				, users.getUserIdx()
				, -1, users.getUserId()
				, users.getUserName()
				, enterRoomResult.getName() + "_" + enterRoomResult.getDescription(), "");

		chattingMapper.addEvent(roomEvent);
		return enterRoomResult;
	}

	/*public ArrayList<ChatRoomData> listChatRooms(){
		return chatManager.getChatRoomList();
	}

	public ChatRoomData getChatRoom(int roomIdx){
		return chatManager.getChatRoom(roomIdx);
	}
	
	public ChatRoomData updateChatRoom(long internalIdx, ChatRoom roomInfo) throws Exception {
		return chatManager.updateChatRoom(internalIdx, roomInfo);
	}

	public ArrayList<Users> listUsers(int roomIdx) {
		return chatManager.getUserList(roomIdx);
	}

	
	public void leaveChatRoom(int programIdx, int userIdx, long internalIdx) throws Exception {
		chatManager.leaveChatRoom(internalIdx, programIdx, null);

		Event roomEvent = new Event(EventType.LEAVE_USER, programIdx, userIdx, -1, "", "", "","");
		chattingMapper.addEvent(roomEvent);
	}
	
	public Integer[] getBlackList(long internalIdx, int roomIdx) throws Exception {
		return chatManager.getBlackList(internalIdx, roomIdx);
	}
	
	public void addBlackList(long internalIdx, int userIdx, int programIdx, int blackUser) throws Exception {
		chatManager.addBlackList(internalIdx, programIdx, blackUser);

		Event roomEvent = new Event(EventType.ADD_BLACKLIST, programIdx, userIdx, blackUser, "", "", "","");
		chattingMapper.addEvent(roomEvent);
	}
	
	public void removeBlackList(long internalIdx, int userIdx, int programIdx, int blackUser) throws Exception {
		chatManager.removeBlackList(internalIdx, programIdx, blackUser);

		Event roomEvent = new Event(EventType.REMOVE_BLACKLIST, programIdx, userIdx, blackUser, "", "", "","");
		chattingMapper.addEvent(roomEvent);
	}
	
	public Event sendEvent(long internalIdx, Event chatDTO) throws Exception{

		System.out.println(chatDTO);
		Event e = new Event();
		String enen = chatDTO.getMessage();
		e.setType(chatDTO.getType());
		e.setProgramIdx(chatDTO.getProgramIdx());
		e.setFrom_userIdx(chatDTO.getFrom_userIdx());
		e.setTo_userIdx(chatDTO.getTo_userIdx());
		e.setTo_UserId(chatDTO.getTo_UserId());
		e.setUserId(chatDTO.getUserId());
		e.setName(chatDTO.getName());
		e.setMsg(enen);
		
		chattingMapper.addEvent(e);
		chatDTO.setIdx(e.getIdx());
		chatManager.sendEvent(internalIdx, chatDTO);
		chattingMapper.insertMessageTypeUpdate(chatDTO);
		
		return chatDTO;
	}
	
	public ArrayList<Event> getNewEvents(long internalIdx) throws Exception {
		return chatManager.getNewEvents(internalIdx);
	}

	public List<Event> getBeforeMessage(int userIdx, String userId, String roomName) {
		// TODO Auto-generated method stub
		if("admin".equals(userId)){
			return chattingMapper.getBeforeAdminMessage(roomName);
		}else{
			return chattingMapper.getBeforeMessage(userIdx,userId,roomName);
		}
	}

	public List<Event> getBeforeAllChatMessage(String roomName) {
		// TODO Auto-generated method stub
		return chattingMapper.getBeforeAllChatMessage(roomName);
	}

	public List<Event> getBeforeApproveMessage(String roomName) {
		// TODO Auto-generated method stub
		return chattingMapper.getBeforeApproveMessage(roomName);
	}

	public void updateMessageType(int idx) {
		// TODO Auto-generated method stub
		chattingMapper.updateMessageType(idx);
	}

	public List<Event> blackUserList(String roomName) {
		// TODO Auto-generated method stub
		return chattingMapper.blackUserList(roomName);
	}

	public int removeBlackUser(int idx) {
		// TODO Auto-generated method stub
		if(chattingMapper.removeBlackUser(idx)>0){
			return chattingMapper.removeBlackUser(idx);
		}
		return -1;
	}*/
}
