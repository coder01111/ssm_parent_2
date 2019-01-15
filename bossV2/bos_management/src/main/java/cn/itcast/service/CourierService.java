package cn.itcast.service;

import cn.itcast.bos.domain.base.Courier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Query;

public interface CourierService {

    void save(Courier standard);

    Page<Courier> query(Specification<Courier> specification, Pageable pageableq);

    void uptade(Integer id,Character status);
}
