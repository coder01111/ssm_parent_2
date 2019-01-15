package cn.itcast.repository;

import cn.itcast.bos.domain.base.Standard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * 收费标准的respostoty接口
 */
public interface StandardRespostroy extends JpaRepository<Standard,Integer> ,JpaSpecificationExecutor<Standard>{



}
