package site.heeseong.chatting_server.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Users {

	private long internalIdx;
	private long userIdx;
	private String userId;
	private String userName;
	private boolean isAdmin;


	public Users(long userIdx, String userId, String userName, boolean isAdmin){
		this.internalIdx = -1;
		this.userIdx = userIdx;
		this.userId = userId;
		this.userName = userName;
		this.isAdmin = isAdmin;
	}

}
