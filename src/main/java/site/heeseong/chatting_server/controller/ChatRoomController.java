package site.heeseong.chatting_server.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import site.heeseong.chatting_server.event_enum.EventType;
import site.heeseong.chatting_server.model.ChattingRoom;
import site.heeseong.chatting_server.model.EnterRoomResult;
import site.heeseong.chatting_server.model.Users;
import site.heeseong.chatting_server.service.ChattingService;

@Log4j2
@RestController
@RequestMapping("/chattingRoom")
public class ChatRoomController {

	private final ChattingService chattingService;

	@Autowired
	public ChatRoomController(ChattingService chattingService){
		this.chattingService = chattingService;
	}

	@PostMapping(value="/enterUser")
	public EnterRoomResult enterChatRoom(
			@RequestHeader("userIdx") long userIdx,
			@RequestHeader("userId") String userId,
			@RequestHeader("userName") String userName,
			@RequestHeader("isAdmin") boolean isAdmin,
			@RequestBody ChattingRoom chatRoom) throws Exception {


		chatRoom.setUserIdx(userIdx);
		chatRoom.setUserId(userId);
		chatRoom.setUserName(userName);
		chatRoom.setAdmin(isAdmin);
		Users users = new Users(chatRoom.getUserIdx(), chatRoom.getUserId(), chatRoom.getUserName(), chatRoom.isAdmin());

		return chattingService.enterChatRoom(chatRoom, users);
	}
}
