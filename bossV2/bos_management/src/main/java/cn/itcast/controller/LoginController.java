package cn.itcast.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class LoginController {
    @RequestMapping("/")
    public String index() {
        return  "index";
    }

    @RequestMapping("/login-error")
    public String hello() {
        return "login-error";
    }

    @RequestMapping("/login")
    public String showLogin() {
        return "login";
    }



}
