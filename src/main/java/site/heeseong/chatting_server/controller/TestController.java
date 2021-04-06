package site.heeseong.chatting_server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import site.heeseong.chatting_server.service.ChattingService;

@RestController
@RequestMapping("/test")
public class TestController {

	private final ChattingService chattingService;

	@Autowired
	public TestController(ChattingService chattingService){
		this.chattingService = chattingService;
	}


	@RequestMapping(value="/listChattingRoom", method=RequestMethod.GET)
	public String getEvent() {
		System.out.println(chattingService.listChatRooms());
		return "ddd";
	}
}
