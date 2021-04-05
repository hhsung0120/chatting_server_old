package site.heeseong.chatting_server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
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


	@RequestMapping(value="/event", method=RequestMethod.GET)
	public ArrayList<Event> getEvent(
			@RequestHeader("internalIdx") int internalIdx) throws Exception {

		return chattingService.getNewEvents(internalIdx);
	}

	@RequestMapping(value="/event", method=RequestMethod.POST)
	public Event sendEvent(
			@RequestHeader("internalIdx") int internalIdx,
    		@RequestBody Event chatDTO) throws Exception {

		return chattingService.sendEvent(internalIdx,chatDTO);
	}
}
