package com.ape.smarthedge_risk_hub.service;

import com.ape.smarthedge_risk_hub.pojo.SupplyCategory;

import java.util.List;

public interface ISupplyCategoryService {

    /**
     * 查询所有供货品类
     */
    List<SupplyCategory> findAllSupplyCategories();

    /**
     * 根据关键词模糊查询供货品类
     */
    List<SupplyCategory> findSupplyCategoriesByKeyword(String keyword);

}
