package cn.lyq.dao;

import cn.lyq.domain.Member;
import org.apache.ibatis.annotations.Select;

public interface MemberDao {
    @Select("select * from member where id = #{id}")
    Member findMemberById(String id);
}
