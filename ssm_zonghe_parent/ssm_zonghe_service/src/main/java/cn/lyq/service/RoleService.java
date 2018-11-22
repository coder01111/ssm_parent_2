package cn.lyq.service;

import cn.lyq.domain.Permission;
import cn.lyq.domain.Role;

import java.util.List;

public interface RoleService {
    List<Role> findAll(int page,int size);
    void save(Role role);

    Role findById(String id);

    List<Permission> findOtherPermissions(String roleId);

    void addPermissionToRole(String roleId, String[] permissionIds);

    void deleteRole(String roleId);

}
