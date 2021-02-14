package site.heeseong.chatting_server.event_enum;

public enum EventType {

    NORMAL_MSG(0, "NORMAL_MSG")
    , DIRECT_MSG(1, "DIRECT_MSG")
    , ADMIN_MSG(2, "ADMIN_MSG")
    , REQ_APPROVAL_MSG(3, "REQ_APPROVAL_MSG")
    , WAIT_APPROVAL_MSG(4, "WAIT_APPROVAL_MSG")
    , APPROVED_MSG(5, "APPROVED_MSG")
    , REJECTED_MSG(6, "REJECTED_MSG")
    , BLOCKED_MSG(7, "BLOCKED_MSG")

    , CREATE_CHATROOM(10, "CREATE_CHATROOM")
    , REMOVE_CHATROOM(11, "REMOVE_CHATROOM")
    , ADD_BLACKLIST(12, "ADD_BLACKLIST")
    , REMOVE_BLACKLIST(13, "REMOVE_BLACKLIST")
    , LIST_BLACKLIST(14, "REMOVE_BLACKLIST")
    , UPDATE_CHATROOM(14, "UPDATE_CHATROOM")

    , ENTER_USER(20, "ENTER_USER")
    , LEAVE_USER(21, "LEAVE_USER")
    ;

    private int value = 0;
    private String textValue = "";

    EventType(int value){
        this.value = value;
    }

    EventType(int value, String textValue){
        this.value = value;
        this.textValue = textValue;
    }

    public int getValue(){
        return this.value;
    }
}
