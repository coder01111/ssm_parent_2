package cn.lyq.dao;


import cn.lyq.domain.Traveller;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface TravellerDao {
    @Select("select * from traveller where id in (select travellerId from ORDER_TRAVELLER where ORDERID=#{orderId})")
    List<Traveller> findTravellerById(String orderId);
}
