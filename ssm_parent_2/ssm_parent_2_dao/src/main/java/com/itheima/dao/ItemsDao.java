package com.itheima.dao;

import com.itheima.domain.Items;
//ÁõÔªÇì£¬ÎÒÊÇÔ¬ÇÚ¿µ
public interface ItemsDao {
    public Items findById(Integer id);
    //
    void save();
}
