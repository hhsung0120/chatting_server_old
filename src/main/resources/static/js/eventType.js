var eventType = {
	NORMAL_MSG: 0,
	DIRECT_MSG: 1,
	ADMIN_MSG: 2,
	REQ_APPROVAL_MSG: 3,
	WAIT_APPROVAL_MSG: 4,
	APPROVED_MSG: 5,
	REJECTED_MSG: 6,
	BLOCKED_MSG: 7,
	CREATE_CHATROOM: 10,
	REMOVE_CHATROOM: 11, //마지막에 나간 사람
	ADD_BLACKLIST: 12,
	REMOVE_BLACKLIST: 13,
	LIST_BLACKLIST: 14,
	ENTER_USER: 20, //접속
	LEAVE_USER: 21 //나감
	// 30 내가 추가  채팅 승인하면 30 됨 5번과 동시에 db 인설트 되며, 필요에 의해서 30만들었음
	// false는  비정상 종료, true 버튼 종료
};

var ChatRoomType = {
	MANYTOMANY : 0, //다대다
	ONETOMANY : 1, //일대다 관리자 : 사용자
	APPROVAL : 2 //승인 되어야 전송 됨
}






















