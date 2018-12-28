package cn.lyq.service.impl;

import cn.lyq.dao.UserDao;
import cn.lyq.domain.Role;
import cn.lyq.domain.UserInfo;
import cn.lyq.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 实现继承了UserDetailService的类间接实现了这个接口，三层架构
 * 必须实现这个类，才能进行用户的验证和来自表单的数据进行验证
 */
@Service("UserService")
public class UserServiceImpl implements UserService {
    @Autowired
    private UserDao userDao;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
       /* spring-security框架会自动去获取表单提交的数据并且调用这个方法
      去进行和我们返回的UserDetails的实现类里面的用户名和密码进行比较*/
        UserInfo userInfo = null;
        try {
            //可能查询出来为空的，捕获异常有助于解决Bug
            userInfo = userDao.findUserByUserName(username);
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<Role> roles = userInfo.getRoles();
        User user = new User(userInfo.getUsername(),  userInfo.getPassword(), userInfo.getStatus() == 0 ? false : true, true, true, true, getAuthority(roles));
        return user;
    }

    public List<SimpleGrantedAuthority> getAuthority(List<Role> roles) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (Role role : roles) {
            authorities.add(new SimpleGrantedAuthority(role.getRoleName()));
        }
        return authorities;
    }

    @Override
    public List<UserInfo> findAll() {
        return userDao.findAll();
    }

    @Override
    public void save(UserInfo userInfo) {
        userInfo.setPassword(passwordEncoder.encode(userInfo.getPassword()));
        userDao.save(userInfo);
    }

    @Override
    public UserInfo findById(String id) {
        return userDao.findById(id);
    }

    @Override
    public List<Role> findUserByIdAndAllRole(String userId) {

        return userDao.findUserByIdAndAllRole(userId);
    }

    @Override
    public void addRoleToUser(String userId, String[] ids) {
        for (String id : ids) {
            userDao.addRoleToUser(userId,id);
        }
    }
}
