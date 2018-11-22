package cn.lyq.service.impl;

import cn.lyq.dao.RoleDao;
import cn.lyq.domain.Permission;
import cn.lyq.domain.Role;
import cn.lyq.service.RoleService;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {
    @Autowired
    private RoleDao roleDao;

    @Override
    public List<Role> findAll(int page,int size) {
        PageHelper.startPage(page, size);
        return roleDao.findAll(page,size);
    }

    @Override
    public void save(Role role) {
        roleDao.save(role);
    }

    @Override
    public Role findById(String id) {
        return roleDao.findById(id);
    }

    @Override
    public List<Permission> findOtherPermissions(String roleId) {
        return roleDao.findOtherPermissions(roleId);
    }

    @Override
    public void addPermissionToRole(String roleId, String[] permissionIds) {
        //遍历数组
        for (String permissionId : permissionIds) {
            roleDao.addPermissionToRole(roleId,permissionId);
        }
    }

    @Override
    public void deleteRole(String roleId) {
        roleDao.deleteUser_role(roleId);
        roleDao.deleteRole_permission(roleId);
        roleDao.deleteRole(roleId);
    }
}
