package cn.lyq.service;

import cn.lyq.domain.Permission;
import cn.lyq.domain.Role;

import java.util.List;

public interface PermissionService {
    List<Permission> findAll();
    void save(Permission permission);

    Permission findById(String id);

    void deletePermission(String id);

}
