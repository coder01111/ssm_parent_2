package cn.itcast.repository;

import cn.itcast.bos.domain.base.Courier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;

/**
 * 收费标准的respostoty接口
 */
public interface CourierRespostroy extends JpaRepository<Courier, Integer>, JpaSpecificationExecutor<Courier> {
    //指定参数位置
    @Query(value = "update  Courier  set  deltag = ?2 where id= ?1" )
    @Modifying
    public void updateDel(Integer id,Character status);


}
