package cn.lyq.dao;

import cn.lyq.domain.Permission;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface PermissionDao {
    @Select("select * from permission where id in (select permissionId from role_permission where ROLEID=#{roleId})")
    List<Permission> findPermissionById(String roleId);
    /**
     * 查询所有权限
     */
    @Select("select * from permission")
    List<Permission> findAll();
    /**
     * 添加权限的方法
     */
    @Insert("insert into permission(permissionName,url) values(#{permissionName},#{url})")
    void save(Permission permission);
    @Select("select * from permission where id = #{id}")
    Permission findById(String id);
    @Delete("delete from permission where id = #{id}")
    void deletePermission(String id);
    @Delete("delete from role_permission where permissionId = #{id}")
    void deletePermission_role(String id);

}
