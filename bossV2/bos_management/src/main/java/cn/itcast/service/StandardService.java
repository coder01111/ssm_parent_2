package cn.itcast.service;

import cn.itcast.bos.domain.base.Standard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface StandardService  {

    void save(Standard standard);

    Page<Standard> query(Specification<Standard> specification, Pageable pageableq);

    void delete(Integer id);

    List<Standard> findAll();
}
