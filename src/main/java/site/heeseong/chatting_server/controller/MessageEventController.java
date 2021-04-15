package site.heeseong.chatting_server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import site.heeseong.chatting_server.model.MessageEvent;
import site.heeseong.chatting_server.service.ChattingService;

import java.util.ArrayList;

@RestController
@RequestMapping("/message")
public class MessageEventController {

	final private ChattingService chattingService;
	@Autowired
	public MessageEventController(ChattingService chattingService){
		this.chattingService = chattingService;
	}

	@RequestMapping(value="/event", method=RequestMethod.GET)
	public ArrayList<MessageEvent> getEvent(
			@RequestHeader("internalIdx") int internalIdx) throws Exception {
		return chattingService.getNewEvents(internalIdx);
	}

	@RequestMapping(value="/event", method=RequestMethod.POST)
	public MessageEvent sendEvent(
			@RequestHeader("internalIdx") int internalIdx,
    		@RequestBody MessageEvent messageEvent) throws Exception {
		System.out.println(messageEvent.toString());
		return chattingService.sendEvent(internalIdx, messageEvent);
	}
}
