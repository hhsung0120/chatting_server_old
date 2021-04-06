package site.heeseong.chatting_server.manager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTask {

    @Autowired
	private ChattingManager chatManager;

    @Scheduled(fixedRate = 60000)
    public void checkUserTimeout() {
    	chatManager.checkUsersTimeout();
    }
}