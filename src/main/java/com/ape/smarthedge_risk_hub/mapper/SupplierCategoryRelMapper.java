package com.ape.smarthedge_risk_hub.mapper;

import com.ape.smarthedge_risk_hub.pojo.SupplierCategoryRel;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SupplierCategoryRelMapper extends BaseMapper<SupplierCategoryRel> {

    /**
     * 根据供应商ID查询关联的供货品类ID列表
     */
    @Select("SELECT category_id FROM supplier_category_rel WHERE supplier_id = #{supplierId}")
    List<Integer> findCategoryIdsBySupplierId(@Param("supplierId") Integer supplierId);

}
