package com.pyg.web;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/login")
public class LoginController {
    @RequestMapping("/username")
    public Map<String, Object> username() {
        //获取登陆页面上登陆的用户名并存入 Map中
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        //创建一个用来存取用户名的Map
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("loginName",name);
        return map;
    }
}
