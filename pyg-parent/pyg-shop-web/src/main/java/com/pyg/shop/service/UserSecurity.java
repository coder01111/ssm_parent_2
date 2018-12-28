package com.pyg.shop.service;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.pinyougou.pojo.TbSeller;
import com.pyg.service.SellerService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建一个认证类用来判断用户名和密码是否正确
 */

public class UserSecurity implements UserDetailsService {
    //引入sellerService的实现
    private SellerService sellerService;

    public SellerService getSellerService() {
        return sellerService;
    }

    public void setSellerService(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //角色列表
        List<GrantedAuthority> grantedAuths = new ArrayList<GrantedAuthority>();
        //添加角色
        grantedAuths.add(new SimpleGrantedAuthority("ROLE_SELLER"));
        //判断用户名查询出来的数据是否为空
        if (StringUtils.isEmpty(username)){
            //如果为空
            return new User("error","error",grantedAuths);
        }
        TbSeller seller = sellerService.findOne(username);
        if(seller!=null&& "1".equals(seller.getStatus())) {//审核通过的
            //添加角色
            grantedAuths.add(new SimpleGrantedAuthority("ROLE_SELLER"));
            //返回的用户信息（验证之前，这里面得到的是正确的，用来和用户输入的进行匹配）
            return new User(username, seller.getPassword(), grantedAuths);
        }
        return new User("error_"+username, "error", grantedAuths);


    }
}
