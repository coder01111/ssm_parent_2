package cn.itcast.component;

import cn.itcast.bos.domain.security.SecurityUser;
import cn.itcast.service.impl.MyUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Component
public class MyAuthenticationProvider implements AuthenticationProvider {
    /**
     * 注入我们自己定义的用户信息获取对象
     */
    @Autowired
    private MyUserDetailsService userDetailService;
    //加密

    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String userName = authentication.getName();// 这个获取表单输入中返回的用户名;
        System.out.println(userName);
        String password1 =(String) authentication.getCredentials();
        // 这个是表单中输入的密码；
        System.out.println(password1);
//        String password = bCryptPasswordEncoder.encode(password1);
        // 这里构建来判断用户是否存在和密码是否正确
        SecurityUser user = (SecurityUser) userDetailService.loadUserByUsername(userName);// 这里调用我们的自己写的获取用户的方法；
        if (user == null) {
            throw new BadCredentialsException("用户名不存在");
        }
        if (!user.getPassword().equals(password1)) {
            throw new BadCredentialsException("密码不正确");
        }
//        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        // 构建返回的用户登录成功的token
        return new UsernamePasswordAuthenticationToken(userName, password1, null);

    }

    @Override
    public boolean supports(Class<?> aClass) {
        return true;
    }
}
