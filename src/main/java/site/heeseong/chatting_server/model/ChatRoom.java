package site.heeseong.chatting_server.model;

import lombok.Data;

@Data
public class ChatRoom extends Users{

	private String name;
	private String description;
	private String password;
	private int type;
	private long adminIdx;
	private String status;
	private int programIdx;

	public ChatRoom(String name, String description, String password, String status, int type, long userIdx, long adminIdx, int programIdx) {
		this.name = name;
		this.description = description;
		this.password = password;
		this.status = status;
		this.type = type;
		this.adminIdx = adminIdx;
		this.programIdx = programIdx;
		this.setUserIdx(userIdx);
	}

}
