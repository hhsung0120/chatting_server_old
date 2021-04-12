package site.heeseong.chatting_server.model;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChattingRoom extends Users{

	private long internalIdx;
	private int programIdx;
	private String name;
	private String description;
	private String password;
	private int type; //
	private long adminIdx;
	private String status;

	//roomIdx;
	//roomName;
	//roomDescription
	//roomPassword;
	//roomType;
	//roomAdminIdx;
	//roomStatus;
	//userIdx;

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
