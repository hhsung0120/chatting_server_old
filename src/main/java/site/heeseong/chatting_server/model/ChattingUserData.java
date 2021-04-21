package site.heeseong.chatting_server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

@Data
public class ChattingUserData {

	private ChattingUsers chattingUsers;
	private int programIdx;
	private ArrayBlockingQueue<MessageEvent> messageQueue;
	private long latestMessageTime;
	private long DEFAULT_MESSAGE_TIMEOUT = 60 * 1000 * 2; // 2 minutes
	private long userTimeout = DEFAULT_MESSAGE_TIMEOUT;

	public ChattingUserData(ChattingUsers chattingUsers) {
		this.chattingUsers = chattingUsers;
		messageQueue = new ArrayBlockingQueue<>(10);
		latestMessageTime = System.currentTimeMillis();
	}

	public long getUserIdx() {
		return chattingUsers.getUserIdx();
	}

	public long getInternalIdx() {
		return chattingUsers.getInternalIdx();
	}

	public String getUserId() {
		return chattingUsers.getUserId();
	}

	public String getUserName() {
		return chattingUsers.getUserName();
	}

	public boolean isAdmin() {
		return chattingUsers.isAdmin();
	}

	public void postMessage(MessageEvent messageEvent) {
		if (messageQueue != null) {
			try {
				messageQueue.add(messageEvent);
			} catch (Exception e) {
				decreaseUserTimeOut();
				e.printStackTrace();
			}
		}
	}
	
	private void decreaseUserTimeOut() {
		userTimeout = userTimeout / 2;
		if (userTimeout < 60000) {
			userTimeout = 60000;
		}
	}
	
	public boolean checkTimeout() {
		if (latestMessageTime != 0) {
			if ((System.currentTimeMillis() - latestMessageTime) > userTimeout) {
				return true;
			}
		}
		return false;
	}
	
	private void setLatestTime() {
		latestMessageTime = System.currentTimeMillis();
	}
	
	@JsonIgnore
	public ArrayList<MessageEvent> getEvents() {
		setLatestTime();
		ArrayList<MessageEvent> messageEvents = new ArrayList<>();
		if (messageQueue != null) {
			try {
				MessageEvent messageEvent = messageQueue.poll(5000, TimeUnit.MILLISECONDS);
				if (messageEvent != null && messageQueue != null) {
					messageEvents.add(messageEvent);
					if (messageQueue.size() != 0) {
						for (int i = 0; i < messageQueue.size(); i++) {
							messageEvents.add(messageQueue.take());
						}
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		return messageEvents;
	}
	
	public void removeAll() {
		if (messageQueue != null) {
			messageQueue.clear();
			messageQueue = null;
		}
	}



}
