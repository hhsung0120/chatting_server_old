package site.heeseong.chatting_server.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.ArrayBlockingQueue;

@Data
public class Users {

	private long internalIdx;
	private long userIdx;
	private String userId;
	private String userName;
	private boolean isAdmin;

	private int programIdx;
	private ArrayBlockingQueue<MessageEvent> messageQueue;
	private long latestMessageTime;
	private long DEFAULT_MESSAGE_TIMEOUT = 60 * 1000 * 2; // 2 minutes
	private long userTimeout = DEFAULT_MESSAGE_TIMEOUT;

	public Users() {
		System.out.println("디폴트 생성자 실행");
	}

	public Users(long userIdx, String userId, String userName, boolean isAdmin){
		this.userIdx = userIdx;
		this.userId = userId;
		this.userName = userName;
		this.isAdmin = isAdmin;
		this.internalIdx = -1;
	}

}
