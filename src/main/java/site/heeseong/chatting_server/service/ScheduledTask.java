package site.heeseong.chatting_server.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import site.heeseong.chatting_server.service.ChattingService;

@Component
public class ScheduledTask {

	final private ChattingService chattingService;
    @Autowired
    private ScheduledTask(ChattingService chattingService){
        this.chattingService = chattingService;
    }

    @Scheduled(fixedRate = 60000)
    public void checkUserTimeout() {
        chattingService.checkUsersTimeout();
    }
}