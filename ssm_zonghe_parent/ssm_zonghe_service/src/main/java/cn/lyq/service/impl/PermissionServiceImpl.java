package cn.lyq.service.impl;

import cn.lyq.dao.PermissionDao;
import cn.lyq.domain.Permission;
import cn.lyq.domain.Role;
import cn.lyq.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class PermissionServiceImpl implements PermissionService {
    @Autowired
    private PermissionDao permissionDao;
    @Override
    public List<Permission> findAll() {
        return permissionDao.findAll();
    }

    @Override
    public void save(Permission permission) {
        permissionDao.save(permission);
    }

    @Override
    public Permission findById(String id) {
        return permissionDao.findById(id);

    }

    @Override
    public void deletePermission(String id) {
        permissionDao.deletePermission_role(id);
        permissionDao.deletePermission(id);
    }
}
