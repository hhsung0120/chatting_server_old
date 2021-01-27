package site.heeseong.chattingserver.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import site.heeseong.chattingserver.service.ChattingService;

@Log4j2
@RestController
@RequestMapping("/chattingRoom")
public class ChatRoomController {

	private final ChattingService chattingService;

	@Autowired
	public ChatRoomController(ChattingService chattingService){
		this.chattingService = chattingService;
	}

	@RequestMapping(value="/enterUser", method=RequestMethod.POST)
	public EnterRoomResult enterChatRoom(
			@RequestHeader("userIdx") long userIdx,
			@RequestHeader("userId") String userId,
			@RequestHeader("userName") String userName,
			@RequestHeader("isAdmin") boolean isAdmin,
			@RequestBody ChatRoom chatRoom) throws Exception {

		chatRoom.setUserIdx(userIdx);
		chatRoom.setUserId(userId);
		chatRoom.setUserName(userName);
		chatRoom.setAdmin(isAdmin);
		Users users = new Users(chatRoom.getUserIdx(), chatRoom.getUserId(), chatRoom.getUserName(), chatRoom.isAdmin());

		return chattingService.enterChatRoom(chatRoom, users);
	}
}
