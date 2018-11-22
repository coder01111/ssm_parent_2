package cn.lyq.dao;

import cn.lyq.domain.Orders;
import cn.lyq.domain.Product;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Service;

import java.util.List;

public interface OrdersDao {
    //查询所有的方法
    @Select("select * from orders")
    //一个产品对应多个订单，一个订单对应一个产品
    @Results({
            @Result(property = "product" ,column = "productId",one = @One(select = "cn.lyq.dao.ProductDao.findProductById"),javaType = Product.class)
    })
    public List<Orders> findAll(int page,int size);
    @Select("select * from orders where id = #{id}")
    @Results(
            {
                    @Result(property ="id" ,column = "id"),
                    @Result(property = "product",column = "productId",one = @One(select = "cn.lyq.dao.ProductDao.findProductById")),
                    @Result(property = "member" ,column ="memberId", one = @One(select = "cn.lyq.dao.MemberDao.findMemberById")),
                    @Result(property = "travellers" ,column = "id",many = @Many(select = "cn.lyq.dao.TravellerDao.findTravellerById"))
            }
    )
    Orders findById(String ordersId);
    @Delete("delete from orders where id = #{id}")
    void deleteById(String id);
    @Delete("delete from ORDER_TRAVELLER where ORDERID = #{id}")
    void deleteOrder_traveller(String id);
}
