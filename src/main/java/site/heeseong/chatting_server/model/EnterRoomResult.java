package site.heeseong.chatting_server.model;

public class EnterRoomResult extends ChattingRoom {
	private long internalIdx;
	
	public EnterRoomResult(long internalIdx, ChattingRoom org) {
		super();
		this.internalIdx = internalIdx;
		this.setProgramIdx(org.getProgramIdx());
		this.setName(org.getName());
		this.setDescription(org.getDescription());
		this.setStatus(org.getStatus());
		this.setType(org.getType());
		this.setAdminIdx(org.getAdminIdx());
		this.setUserIdx(org.getUserIdx());
	}
	
	public long getInternalIdx() {
		return internalIdx;
	}
	public void setInternalIdx(long internalIdx) {
		this.internalIdx = internalIdx;
	}
}
