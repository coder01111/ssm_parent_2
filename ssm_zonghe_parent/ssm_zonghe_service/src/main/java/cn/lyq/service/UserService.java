package cn.lyq.service;

import cn.lyq.domain.Role;
import cn.lyq.domain.UserInfo;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService  extends UserDetailsService{
    List<UserInfo> findAll();
    void save(UserInfo userInfo);
    UserInfo findById(String id);

    List<Role> findUserByIdAndAllRole(String userId);

    void addRoleToUser(String userId, String[] ids);
}
