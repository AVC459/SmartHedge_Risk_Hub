package com.ape.smarthedge_risk_hub.mapper;

import com.ape.smarthedge_risk_hub.pojo.Supplier;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SupplierMapper extends BaseMapper<Supplier> {

    /**
     * 查询所有供应商及其关联的经营者信息
     */
    @Select("SELECT s.*, o.operator_name FROM supplier s LEFT JOIN operator o ON s.operator_id = o.operator_id")
    @Results(id = "supplierMap", value = {
            @Result(property = "supplierId", column = "supplier_id"),
            @Result(property = "supplierName", column = "supplier_name"),
            @Result(property = "contactPhone", column = "contact_phone"),
            @Result(property = "address", column = "address"),
            @Result(property = "creditCode", column = "credit_code"),
            @Result(property = "operatorId", column = "operator_id"),
            @Result(property = "score", column = "score"),
            @Result(property = "status", column = "status"),
            @Result(property = "updateTime", column = "update_time"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "operatorName", column = "operator_name")
    })
    List<Supplier> findAllSupplierWithOperator();

    /**
     * 根据条件模糊查询供应商及其关联的经营者信息
     */
    @Select("SELECT DISTINCT s.*, o.operator_name FROM supplier s " +
            "LEFT JOIN operator o ON s.operator_id = o.operator_id " +
            "LEFT JOIN supplier_category_rel scr ON s.supplier_id = scr.supplier_id " +
            "LEFT JOIN supply_category sc ON scr.category_id = sc.category_id " +
            "WHERE s.supplier_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR s.credit_code LIKE CONCAT('%', #{keyword}, '%') " +
            "OR sc.category_name LIKE CONCAT('%', #{keyword}, '%')")
    @Results(id = "supplierMapWithKeyword", value = {
            @Result(property = "supplierId", column = "supplier_id"),
            @Result(property = "supplierName", column = "supplier_name"),
            @Result(property = "contactPhone", column = "contact_phone"),
            @Result(property = "address", column = "address"),
            @Result(property = "creditCode", column = "credit_code"),
            @Result(property = "operatorId", column = "operator_id"),
            @Result(property = "score", column = "score"),
            @Result(property = "status", column = "status"),
            @Result(property = "updateTime", column = "update_time"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "operatorName", column = "operator_name")
    })
    List<Supplier> findSupplierWithKeyword(String keyword);

}
