package site.heeseong.chatting_server.model;

public class Event {
	private int type;
	private int programIdx;
	private String userId;
	private String name;
	private String to_userId;
	private long from_userIdx;
	private long to_userIdx;
	private String message;
	private String regidate;
	private int idx;
	
	public Event(int type, int programIdx, long fromUserIdx, long toUserIdx, String id, String name, String msg, String to_userId ) {
		this.type = type;
		this.programIdx = programIdx;
		this.from_userIdx = fromUserIdx;
		this.to_userIdx = toUserIdx;
		this.userId = id;
		this.name = name;
		this.message = msg;
		this.to_userId = to_userId;
	}
	
	public Event(int type, int idx, String userid, String username) {
		this(type, -1, idx, -1, userid, username, "","");
	}

	public Event() {
	}

	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}

	public int getProgramIdx() {
		return programIdx;
	}
	public void setProgramIdx(int programIdx) {
		this.programIdx = programIdx;
	}

	public long getFromUserIdx() {
		return from_userIdx;
	}
	public void setFromUserIdx(int fromUserIdx) {
		this.from_userIdx = fromUserIdx;
	}

	public long getToUserIdx() {
		return to_userIdx;
	}

	public void setToUserIdx(int userIdx) {
		this.to_userIdx = userIdx;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String id) {
		this.userId = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMsg() {
		return message;
	}
	public void setMsg(String msg) {
		this.message = msg;
	}

	public long getFrom_userIdx() {
		return from_userIdx;
	}

	public void setFrom_userIdx(long from_userIdx) {
		this.from_userIdx = from_userIdx;
	}

	public long getTo_userIdx() {
		return to_userIdx;
	}

	public void setTo_userIdx(long to_userIdx) {
		this.to_userIdx = to_userIdx;
	}
/*	public byte[] getMessage() {
		return message;
	}

	public void setMessage(byte[] message) {
		this.message = message;
	}*/
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getRegidate() {
		return regidate;
	}

	public void setRegidate(String regidate) {
		this.regidate = regidate;
	}
	public int getIdx() {
		return idx;
	}

	public void setIdx(int idx) {
		this.idx = idx;
	}

	public String getTo_UserId() {
		return to_userId;
	}

	public void setTo_UserId(String to_userId) {
		this.to_userId = to_userId;
	}



}
