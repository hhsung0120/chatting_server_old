package site.heeseong.chatting_server.component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTask {

	final private ChattingEventManager chatManager;
    @Autowired
    private ScheduledTask(ChattingEventManager chatManager){
        this.chatManager = chatManager;
    }

    @Scheduled(fixedRate = 60000)
    public void checkUserTimeout() {
    	chatManager.checkUsersTimeout();
    }
}