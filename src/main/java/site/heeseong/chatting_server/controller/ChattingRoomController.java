package site.heeseong.chatting_server.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import site.heeseong.chatting_server.model.ChattingRoom;
import site.heeseong.chatting_server.model.EnterRoomResult;
import site.heeseong.chatting_server.model.Users;
import site.heeseong.chatting_server.service.ChattingService;

import java.util.ArrayList;

@Log4j2
@RestController
@RequestMapping("/chattingRoom")
public class ChattingRoomController {

	private final ChattingService chattingService;

	@Autowired
	public ChattingRoomController(ChattingService chattingService){
		this.chattingService = chattingService;
	}

	@PostMapping(value="/enterUser")
	public EnterRoomResult enterChatRoom(
			@RequestHeader("userIdx") long userIdx
			, @RequestHeader("userId") String userId
			, @RequestHeader("userName") String userName
			, @RequestHeader("isAdmin") boolean isAdmin
			, @RequestBody ChattingRoom chattingRoom) throws Exception {

		//chattingRoom 에 뭐 담겨 오는지 체크하고 나중에 세션으로 처리
		chattingRoom.setUserIdx(userIdx);
		chattingRoom.setUserId(userId);
		chattingRoom.setUserName(userName);
		chattingRoom.setAdmin(isAdmin);
		
		//유저 데이터 셋팅
		//이부분은 전부가 세션으로 처리 가능
		Users users = new Users(chattingRoom.getUserIdx(), chattingRoom.getUserId(), chattingRoom.getUserName(), chattingRoom.isAdmin());

		return chattingService.enterChatRoom(chattingRoom, users);
	}

	@RequestMapping(value="/users", method=RequestMethod.GET)
	public ArrayList<Users> listUsers(
			@RequestParam("programIdx") int programIdx){
		return chattingService.listUsers(programIdx);
	}

	@RequestMapping(value="/user", method=RequestMethod.DELETE)
	public void leaveChatRoom(
			@RequestHeader("internalIdx") long internalIdx
			, @RequestHeader("programIdx") int programIdx
			, @RequestHeader("userIdx") int userIdx) throws Exception {
		log.debug("leave chatRoom");
		chattingService.leaveChatRoom(programIdx, userIdx, internalIdx);
	}

	@RequestMapping(value="/blacklist", method=RequestMethod.GET)
	public Integer[] getBlackList(
			@RequestHeader("internalIdx") long internalIdx,
			@RequestHeader("programIdx") int programIdx,
			@RequestHeader("admin") boolean admin) throws Exception {
		return chatService.getBlackList(internalIdx, programIdx);
	}

	@RequestMapping(value="/blacklist", method=RequestMethod.POST)
	public void addBlackList(
			@RequestHeader("internalIdx") long internalIdx,
			@RequestHeader("programIdx") int programIdx,
			@RequestHeader("userIdx") int userIdx,
			@RequestHeader("admin") boolean admin,
			@RequestBody UserRoomMapperDTO blackUser) throws Exception {
		chatService.addBlackList(internalIdx, userIdx, programIdx, blackUser.getUserIdx());
	}

	@RequestMapping(value="/blacklist", method=RequestMethod.DELETE)
	public void removeBlackList(
			@RequestHeader("internalIdx") long internalIdx,
			@RequestHeader("programIdx") int programIdx,
			@RequestHeader("userIdx") int userIdx,
			@RequestHeader("admin") boolean admin,
			@RequestBody UserRoomMapperDTO blackUser) throws Exception {
		chatService.removeBlackList(internalIdx, userIdx, programIdx, blackUser.getUserIdx());
	}


}
