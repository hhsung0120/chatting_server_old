package site.heeseong.chatting_server.model;

import lombok.Data;

@Data
public class Event {
	private int type;
	private int programIdx;
	private String userId;
	private String name;
	private String to_userId;
	private long from_userIdx;
	private long from_user_idx;
	private long fromUserIdx;
	private long to_userIdx;
	private long toUserIdx;
	private String message;
	private String regidate;
	private int idx;

	public Event(){}

	public Event(int type, int programIdx, long fromUserIdx, long toUserIdx, String id, String name, String msg, String to_userId ) {
		this.type = type;
		this.programIdx = programIdx;
		this.from_userIdx = fromUserIdx;
		this.fromUserIdx = fromUserIdx;
		this.to_userIdx = toUserIdx;
		this.userId = id;
		this.name = name;
		this.message = msg;
		this.to_userId = to_userId;
	}


	public Event(int type, int idx, String userid, String username) {
		this(type, -1, idx, -1, userid, username, "","");
	}
}
