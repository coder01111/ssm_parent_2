package cn.lyq.service;

import cn.lyq.domain.Orders;

import java.util.List;

public interface OrdersService {
    //订单业务层
    List<Orders> findAll(int page,int size);

    Orders findById(String ordersId);

    void deleteByIds(String[] ids);
}
