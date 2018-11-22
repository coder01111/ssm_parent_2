package cn.lyq.dao;

import cn.lyq.domain.Permission;
import cn.lyq.domain.Role;
import org.apache.ibatis.annotations.*;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.util.List;

public interface RoleDao {
    /**
     * 根据用户的id查询角色
     *
     * @param userId
     * @return
     */
    @Select("select * from role where id in (select ROLEID from USERS_ROLE where USERID=#{userId})")
    @Results({
            @Result(property = "id", id = true, column = "id"),
            @Result(property = "permissions", column = "id", many = @Many(select = "cn.lyq.dao.PermissionDao.findPermissionById"))

    })
    List<Role> findRoleById(String userId);

    /**
     * 查询所有角色
     */
    @Select("select * from role")
    List<Role> findAll(int page, int size);

    /**
     * 添加角色的方法
     */
    @Insert("insert into role(roleName,roleDesc) values(#{roleName},#{roleDesc})")
    void save(Role role);

    @Select("select * from role where id = #{id}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "permissions", column = "id", many = @Many(select = "cn.lyq.dao.PermissionDao.findPermissionById"))
    })
    Role findById(String id);

    @Select("select * from permission where id not in (select permissionId from  role_permission  where roleId = #{roleId})")
    List<Permission> findOtherPermissions(String roleId);

    @Select("insert into role_permission(roleId,permissionId) values(#{roleId},#{permissionId})")
    void addPermissionToRole(@Param("roleId") String roleId, @Param("permissionId") String permissionId);

    @Delete("delete from role where id = #{roleId}")
    void deleteRole(String roleId);

    @Delete("delete from role_permission where roleId = #{roleId}")
    void deleteRole_permission(String roleId);

    @Delete("delete from users_role  where roleId = #{roleId}")
    void deleteUser_role(String roleId);
}

