package com.pinyougou.entityGroup;

import com.pinyougou.pojo.TbSpecification;
import com.pinyougou.pojo.TbSpecificationOption;

import java.io.Serializable;
import java.util.List;

/**
 * 定义一个组合实体类用来接收提交的保存的规格品牌和规格选项两个类的封装
 * 因为是封装到一个包含规格品牌和选项的json对象
 * 注意必须实现序列化接口因为要在服务器上传输
 */
public class Specification implements Serializable{
    private TbSpecification specification;
    private List<TbSpecificationOption> specificationOptionList;

    public TbSpecification getSpecification() {
        return specification;
    }

    public void setSpecification(TbSpecification specification) {
        this.specification = specification;
    }

    public List<TbSpecificationOption> getSpecificationOptionList() {
        return specificationOptionList;
    }

    public void setSpecificationOptionList(List<TbSpecificationOption> specificationOptionList) {
        this.specificationOptionList = specificationOptionList;
    }
}
