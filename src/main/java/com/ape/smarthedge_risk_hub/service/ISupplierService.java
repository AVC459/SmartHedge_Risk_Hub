package com.ape.smarthedge_risk_hub.service;

import com.ape.smarthedge_risk_hub.common.ReParam;
import com.ape.smarthedge_risk_hub.common.Result;
import com.ape.smarthedge_risk_hub.pojo.Supplier;

import java.util.List;

public interface ISupplierService {

    /**
     * 查询所有供应商及其关联的经营者信息
     */
    List<Supplier> findAllSupplierWithOperator();
    
    /**
     * 根据条件模糊查询供应商及其关联的经营者信息
     */
    List<Supplier> findSupplierByKeyword(String keyword);

    /**
     * 分页查询供应商（支持模糊查询）
     */
    Result selectPage(ReParam param);
    
    /**
     * 添加供应商
     */
    Result addSupplier(Supplier supplier, List<Integer> categoryIds);
    
    /**
     * 删除供应商
     */
    Result deleteSupplier(Integer supplierId);
    
    /**
     * 修改供应商
     */
    Result updateSupplier(Supplier supplier, List<Integer> categoryIds);

}