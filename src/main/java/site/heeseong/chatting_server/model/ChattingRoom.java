package site.heeseong.chatting_server.model;
import lombok.Data;

@Data
public class ChattingRoom extends Users{

	private int programIdx;  //방 키
	private String name; //방이름
	private String description; //방 설명
	private String password; //방 비밀번호
	private int type; // 방 타입
	private long adminIdx; //대빵 idx  인가 ?
	private String status; //방 상태
	private long userIdx; //방에 들어온 유저


	public ChattingRoom(){}

	public ChattingRoom(String name, String description, String password, String status, int type, long userIdx, long adminIdx, int programIdx) {
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
