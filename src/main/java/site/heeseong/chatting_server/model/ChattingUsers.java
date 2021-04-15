package site.heeseong.chatting_server.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChattingUsers {

	private long internalIdx;
	private long userIdx;
	private String userId;
	private String userName;
	private boolean isAdmin;

	public ChattingUsers(long userIdx, String userId, String userName, boolean isAdmin){
		this.userIdx = userIdx;
		this.userId = userId;
		this.userName = userName;
		this.isAdmin = isAdmin;
		this.internalIdx = -1;
	}
}
