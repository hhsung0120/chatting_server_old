package site.heeseong.chatting_server.mapper;

import org.apache.ibatis.annotations.*;
import site.heeseong.chatting_server.model.Event;


@Mapper
public interface ChattingMapper {
	@Insert("insert into chatting(type, programIdx, from_userIdx, to_userIdx, userid, to_userId,  name, message,regiDate) values (#{type}, #{programIdx}, #{from_userIdx}, #{to_userIdx}, #{userId}, #{to_userId}, #{name}, #{msg},utc_timestamp())")
	@Options(useGeneratedKeys=true, keyProperty="idx")//고유값을 받기위해 id > idx 수정 16.10.17
	public long addEvent(Event chatDTO) throws Exception;

	/*@Select("select * from chatting where type in (0,1) and programIdx = #{roomName} order by regidate asc")
	public List<Event> getBeforeAdminMessage(String roomName);
	@Select("select * from chatting where (type in (0,1) and programIdx = #{roomName} and from_userIdx = #{userIdx} and userid = #{userId}) or (type in (0,1) and programIdx= #{roomName} and userid='admin') order by regidate asc")
	public List<Event> getBeforeMessage(@Param("userIdx")int userIdx, @Param("userId")String userId, @Param("roomName")String roomName);
	@Select("select * from chatting where type in (0,1) and programIdx = #{roomName} order by regidate asc")
	public List<Event> getBeforeAllChatMessage(String roomName);
	@Select("select * from chatting where type in (1,3,5,0,30) and programIdx = #{roomName} order by regidate asc")	
	public List<Event> getBeforeApproveMessage(String roomName);
	
	//idx를 리턴 받기 위해 메서드 호출 순서를 아래로 변경.
	//승인모드에서 승인 전 메시지 타입은 3번 이나 호출을 아래로 변경 후 0번으로 insert 되는 경우 발생 
	//원래의 설계대로 3번으로 변경해주기 위한 강제 업데이트 실행
	@Update("update chatting set type = 30 where idx = #{idx}")
	public void updateMessageType(int idx);

	@Update("update chatting set type=#{type} where type = 0 and idx = #{idx}")
	public void insertMessageTypeUpdate(Event chatDTO);
	
	//블랙유저리스트
	@Select("select distinct c1.type, c1.to_userIdx ,c1.programidx , c2.userid , c2.name from chatting c1 join chatting c2" +
			" on c1.to_userIdx = c2.from_userIdx where c1.type = 12 and c1.programidx=#{roomName} and c2.userid is not null " + 
			" and c2.name is not null and c2.userid != '' and c2.name != '' ")
	public List<Event> blackUserList(String roomName);

	
	@Delete("delete from chatting where type = 12 and to_userIdx = #{idx}")
	public int removeBlackUser(int idx);
*/
	
	
	
}
