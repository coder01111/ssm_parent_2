package cn.lyq.service.impl;

import cn.lyq.dao.OrdersDao;
import cn.lyq.domain.Orders;
import cn.lyq.service.OrdersService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class OrderServiceImpl implements OrdersService {
    @Autowired
    private OrdersDao ordersDao;


    @Override
    public List<Orders> findAll(int page, int size) {
        //参数pageNum 是页码值   参数pageSize 代表是每页显示条数
        //开启分页
        PageHelper.startPage(page,size);
        return ordersDao.findAll(page,size);
    }

    @Override
    public Orders findById(String ordersId) {
        return  ordersDao.findById(ordersId);
    }

    @Override
    public void deleteByIds(String[] ids) {
        for (String id : ids) {
            ordersDao.deleteOrder_traveller(id);
            ordersDao.deleteById(id);
        }
    }
}
