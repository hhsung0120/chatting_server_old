package site.heeseong.chatting_server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import site.heeseong.chatting_server.model.Event;
import site.heeseong.chatting_server.service.ChattingService;

import java.util.ArrayList;

@RestController
@RequestMapping("/chattingRoom")
public class EventController {

	private final ChattingService chattingService;

	@Autowired
	public EventController(ChattingService chattingService){
		this.chattingService = chattingService;
	}

	

/*	@RequestMapping(value="/event", method=RequestMethod.POST)
	public EventDTO sendEvent(
			@RequestHeader("internalIdx") int internalIdx,
    		@RequestBody EventDTO chatDTO) throws Exception {
		chatDTO = chattingService.sendEvent(internalIdx,chatDTO);
		return chatDTO;
	}*/

	@RequestMapping(value="/event", method=RequestMethod.GET)
	public ArrayList<Event> getEvent(
			@RequestHeader("internalIdx") int internalIdx) throws Exception {
		

		ArrayList<Event> chattings = chattingService.getNewEvents(internalIdx);
		return chattings;
	}
	
	
	/*@RequestMapping(value="/message",method=RequestMethod.GET)
	public List<EventDTO> getBeforeMessage(
			@RequestParam("userIdx") int userIdx,
			@RequestParam("userId") String userId,
			@RequestParam("roomName") String roomName,
			@RequestParam("type") int type)throws Exception{
		
		List<EventDTO> list= null;
		
		if(type == 0){
			//n:m 메시지 get
			list = chattingService.getBeforeAllChatMessage(roomName);
		}else if(type == 1){
			//1:n 메시지 get
			list = chattingService.getBeforeMessage(userIdx, userId,roomName);
		}else{
			list = chattingService.getBeforeApproveMessage(roomName);
		}
		
		for(int i=0; i<list.size(); i++){
			list.get(i).setMessage(list.get(i).getMessage().replace(list.get(i).getMessage(), TripleDES.decrypt(list.get(i).getMessage())));
		}
	
		System.out.println("list : "+list);
		return list;
	}*/
	
	/*@RequestMapping(value="/updateMessageType", method=RequestMethod.GET)
	public String updateMessageType(@RequestParam("idx")int idx){
		chattingService.updateMessageType(idx);
		return "Ajax";
	}*/

	/*@RequestMapping(value="/blackUserList", method=RequestMethod.GET)
	public List<EventDTO> blackUserList(@RequestParam("roomName") String roomName){
		System.out.println("Server : blackUserList");
		return chattingService.blackUserList(roomName);
	}*/

	/*@RequestMapping(value="/removeBlackUser", method=RequestMethod.GET)
	public String removeBlackUser(@RequestParam("idx") int idx){
		String result = "";
		if(chattingService.removeBlackUser(idx)>0){
			result = "차단 해제";
		}else{
			result = "차단 해제 실패";
		}
		return result;
	}*/
}
