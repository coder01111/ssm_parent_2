package cn.lyq.web;

import cn.lyq.domain.Role;
import cn.lyq.domain.UserInfo;
import cn.lyq.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @RequestMapping("/findAll.do")
    public ModelAndView findAll() {
        ModelAndView mv = new ModelAndView();
        List<UserInfo> list = userService.findAll();
        mv.addObject("userList", list);
        mv.setViewName("user-list");
        return mv;
    }

    @RequestMapping("/save.do")
    @PreAuthorize("authentication.principal.username == 'tom'")
    public String save(UserInfo userInfo) {
            userService.save(userInfo);
        return "redirect:findAll.do";
    }
    @RequestMapping("/findById.do")
    public ModelAndView findById(String id) {
        UserInfo userInfo = userService.findById(id);
        ModelAndView mv = new ModelAndView();
        mv.addObject("user",userInfo);
        mv.setViewName("user-show");
        return mv;
    }
    @RequestMapping("/findUserByIdAndAllRole.do")
    public ModelAndView findUserByIdAndAllRole(String id) {
        ModelAndView mv = new ModelAndView();
        //根据用户id查询到这个用户
        //UserInfo user = userService.findById(id);
        mv.addObject("userId",id);
        //根据id查询可以添加的角色
        List<Role> roleList = userService.findUserByIdAndAllRole(id);
        mv.addObject("roleList",roleList);
        mv.setViewName("user-role-add");
        return mv;
    }
    @RequestMapping("/addRoleToUser.do")
    public String addRoleToUser(String userId,String[] ids) {
        userService.addRoleToUser(userId,ids);
        return "redirect:findAll.do";
    }
}
