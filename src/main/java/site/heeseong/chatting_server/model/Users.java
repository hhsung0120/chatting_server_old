package site.heeseong.chatting_server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

@Data
@NoArgsConstructor
public class Users {

	private long internalIdx;
	private long userIdx;
	private String userId;
	private String userName;
	private boolean isAdmin;

	public Users(long userIdx, String userId, String userName, boolean isAdmin){
		this.userIdx = userIdx;
		this.userId = userId;
		this.userName = userName;
		this.isAdmin = isAdmin;
		this.internalIdx = -1;
	}
}
