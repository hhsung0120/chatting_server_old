package site.heeseong.chatting_server.event_enum;

public enum ChattingRoomType {

    MANY_TO_MANY(0, "MANY_TO_MANY")
    , ONE_TO_MANY(1, "ONE_TO_MANY")
    , APPROVAL(2, "APPROVAL")
    ;

    private int value;
    private String textValue;

    ChattingRoomType(int value, String textValue){
        this.value = value;
        this.textValue = textValue;
    }

    public int getValue(){
        return this.value;
    }
}
