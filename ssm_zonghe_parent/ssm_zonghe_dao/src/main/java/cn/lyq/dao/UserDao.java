package cn.lyq.dao;

import cn.lyq.domain.Role;
import cn.lyq.domain.UserInfo;
import org.apache.ibatis.annotations.*;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.util.List;

public interface UserDao {
    /**
     * 根据用户名查询所有的用户
     */
    @Select("select * from users where username = #{username}")
    @Results({
            @Result(property = "id" ,id =true, column = "id"),
            @Result(property = "roles",column = "id",many = @Many(select = "cn.lyq.dao.RoleDao.findRoleById"))
    })
    UserInfo findUserByUserName(String username);
    /**
     * 查询的所有用户
     */
    @Select("select * from users")
    List<UserInfo> findAll();
    /**
     * 添加用户
     */
    @Insert("insert into users (email,username,password,phoneNum,status) values(#{email},#{username},#{password},#{phoneNum},#{status})")
    void save(UserInfo userInfo);
    /**
     * 查看用户详情的方法根据id
     */
    @Select("select * from users where id = #{id}")
    @Results(value = {
            @Result(property = "id",id =true,column = "id"),
            @Result(property ="roles",column = "id",many = @Many(select = "cn.lyq.dao.RoleDao.findRoleById"))

    })
    UserInfo findById(String userId);

    /**
     * 查询用户还可以添加的其他角色
     * @param userId
     * @return
     */
    @Select("select * from role where id not in (select roleId from USERS_ROLE where userId = #{userId})")
    List<Role> findUserByIdAndAllRole(String userId);

    /**
     * 给这个用户添加其他的角色
     * @param userId
     * @param id
     */
    @Insert("insert into USERS_ROLE (USERID,ROLEID) values(#{USERID},#{ROLEID})")
    void addRoleToUser(@Param("USERID")String userId, @Param("ROLEID") String id);
}
