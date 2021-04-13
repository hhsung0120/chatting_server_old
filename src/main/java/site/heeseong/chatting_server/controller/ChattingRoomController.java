package site.heeseong.chatting_server.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import site.heeseong.chatting_server.model.ChattingRoom;
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
	public ChattingRoom enterChatRoom(
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
		return chattingService.enterChattingRoom(chattingRoom);
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
		chattingService.leaveChatRoom(programIdx, userIdx, internalIdx);
	}

	@RequestMapping(value="/blacklist", method=RequestMethod.GET)
	public Long[] getBlackList(
			@RequestHeader("internalIdx") long internalIdx,
			@RequestHeader("programIdx") int programIdx) throws Exception {
		return chattingService.getBlackList(internalIdx, programIdx);
	}

	@RequestMapping(value="/blacklist", method=RequestMethod.POST)
	public void addBlackList(
			@RequestHeader("internalIdx") long internalIdx,
			@RequestHeader("programIdx") int programIdx,
			@RequestHeader("userIdx") int userIdx,
			@RequestBody Users blackUser) throws Exception {
		chattingService.addBlackList(internalIdx, userIdx, programIdx, blackUser.getUserIdx());
	}

	@RequestMapping(value="/blacklist", method=RequestMethod.DELETE)
	public void removeBlackList(
			@RequestHeader("internalIdx") long internalIdx,
			@RequestHeader("programIdx") int programIdx,
			@RequestHeader("userIdx") int userIdx,
			@RequestBody Users blackUser) throws Exception {
		chattingService.removeBlackList(internalIdx, userIdx, programIdx, blackUser.getUserIdx());
	}


}
