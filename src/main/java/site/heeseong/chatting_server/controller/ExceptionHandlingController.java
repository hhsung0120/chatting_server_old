package site.heeseong.chatting_server.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import site.heeseong.chatting_server.exceptions.*;

@ControllerAdvice
public class ExceptionHandlingController {
	  @ResponseStatus(value=HttpStatus.BAD_REQUEST, reason="There is no specific chat room.")
	  @ExceptionHandler(ChatRoomNotExistException.class)
	  public void handleChatRoomNotExsitException() {}
	  
	  @ResponseStatus(value=HttpStatus.BAD_REQUEST, reason="Chat room name is already existed.")
	  @ExceptionHandler(ChatRoomExistException.class)
	  public void handleChatRoomExsitException() {}
	  
	  @ResponseStatus(value=HttpStatus.BAD_REQUEST, reason="User is already existed in chat room.")
	  @ExceptionHandler(UserExistException.class)
	  public void handleUserExsitException() {}
	  
	  @ResponseStatus(value=HttpStatus.BAD_REQUEST, reason="There is no specific user.")
	  @ExceptionHandler(UserNotExistException.class)
	  public void handleUserNotExsitException() {}

	  @ResponseStatus(value=HttpStatus.BAD_REQUEST, reason="User doesn't have authrization")
	  @ExceptionHandler(UnauthorizedException.class)
	  public void handleUnauthorizedException() {}
}
