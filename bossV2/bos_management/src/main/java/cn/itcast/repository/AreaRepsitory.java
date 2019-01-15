package cn.itcast.repository;

import cn.itcast.bos.domain.base.Area;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AreaRepsitory extends JpaRepository<Area,String>,JpaSpecificationExecutor<Area> {
}
