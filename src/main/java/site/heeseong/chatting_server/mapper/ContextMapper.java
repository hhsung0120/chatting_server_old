package site.heeseong.chatting_server.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import site.heeseong.chatting_server.model.ContextDTO;

@Mapper
public interface ContextMapper {
	@Insert("insert into context(type, content) values (#{type}, #{content})  ON DUPLICATE KEY UPDATE content=#{content}")
	public void setContext(ContextDTO context) throws Exception;
	@Select("select * from  context where type='ROOM'")
	public ContextDTO getRoomContext() throws Exception;
	@Select("select * from  context where type='USER'")
	public ContextDTO getUserContext() throws Exception;
}
