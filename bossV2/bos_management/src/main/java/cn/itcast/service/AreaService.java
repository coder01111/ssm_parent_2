package cn.itcast.service;

import cn.itcast.bos.domain.base.Area;
import cn.itcast.bos.domain.base.Courier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface AreaService {


    void importBatch(List<Area> areas);
    Page<Area> query(Specification<Area> specification, Pageable pageableq);

    void save(Area area);

    void delete(String[] idArray);
}
