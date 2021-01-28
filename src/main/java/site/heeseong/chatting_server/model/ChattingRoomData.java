package site.heeseong.chatting_server.model;

import lombok.Data;

@Data
public class ChattingRoomData {
	private int programIdx;
	private String name;
	private String password;
	private String description;
	private String status;
	private int type;
	private long adminIdx;
	private long userIdx;
}
