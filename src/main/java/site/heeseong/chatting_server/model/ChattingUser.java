package site.heeseong.chatting_server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import site.heeseong.chatting_server.model.MessageEvent;
import site.heeseong.chatting_server.model.Users;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

@Data
public class ChattingUser {

	private Users users;
	private int programIdx;
	private ArrayBlockingQueue<MessageEvent> messageQueue;
	private long latestMessageTime;
	private long DEFAULT_MESSAGE_TIMEOUT = 60 * 1000 * 2; // 2 minutes
	private long userTimeout = DEFAULT_MESSAGE_TIMEOUT;

	public ChattingUser(Users userInfo) {
		users = userInfo;
		messageQueue = new ArrayBlockingQueue<MessageEvent>(10);
		latestMessageTime = System.currentTimeMillis();
	}

	public long getUserIdx() {
		return users.getUserIdx();
	}

	public long getInternalIdx() {
		return users.getInternalIdx();
	}

	public String getUserId() {
		return users.getUserId();
	}

	public String getUserName() {
		return users.getUserName();
	}

	public boolean isAdmin() {
		return users.isAdmin();
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
		ArrayList<MessageEvent> messageEvents = new ArrayList<MessageEvent>();
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
