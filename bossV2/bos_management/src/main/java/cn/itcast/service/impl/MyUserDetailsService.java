package cn.itcast.service.impl;

import cn.itcast.bos.domain.security.Role;
import cn.itcast.bos.domain.security.SecurityUser;
import cn.itcast.bos.domain.security.User;
import cn.itcast.repository.UserRepostory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class  MyUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepostory userRepostory;


    /**
     * 重写里面的loadUserByUsername
     * @param username
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //--------------------认证账号
        User user = userRepostory.findByUsername(username);
//        System.out.println(user.getRoles());
        System.out.println(user.getPassword());
        if (user == null) {
            throw new UsernameNotFoundException("账号不存在");
        }
        return new SecurityUser(user);
    }

        public static List<SimpleGrantedAuthority> getAuthority(List<Role> roles) {
            //-------------------开始授权
//        for (Role role : menus) {
//            System.out.println(role.getRolename()
            List<SimpleGrantedAuthority> grantedAuthorities = new ArrayList<>();
            for (Role menu : roles) {

                //此处将权限信息添加到 GrantedAuthority 对象中，在后面进行全权限验证时会使用GrantedAuthority 对象。
                grantedAuthorities.add(     new SimpleGrantedAuthority("ROLE_"+menu.getRolename()));
            }
            return grantedAuthorities;
        }






}
