package com.itheima.dao;

import com.itheima.domain.Items;

//��Ԫ�죬����Ԭ�ڿ�
public interface ItemsDao {
    public Items findById(Integer id);

    //
    void save();
}
