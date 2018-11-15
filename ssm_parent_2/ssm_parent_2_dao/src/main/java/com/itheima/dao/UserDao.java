package com.itheima.dao;

import com.itheima.domain.User;

public interface UserDao {
    void findAll();
    //添加查询一个方法
    void  add(User user);
}
