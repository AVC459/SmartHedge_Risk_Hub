package com.ape.smarthedge_risk_hub.service.impl;

import com.ape.smarthedge_risk_hub.mapper.SupplyCategoryMapper;
import com.ape.smarthedge_risk_hub.pojo.SupplyCategory;
import com.ape.smarthedge_risk_hub.service.ISupplyCategoryService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SupplyCategoryServiceImp implements ISupplyCategoryService {

    @Autowired
    private SupplyCategoryMapper supplyCategoryMapper;

    @Override
    public List<SupplyCategory> findAllSupplyCategories() {
        return supplyCategoryMapper.selectList(null);
    }

    @Override
    public List<SupplyCategory> findSupplyCategoriesByKeyword(String keyword) {
        QueryWrapper<SupplyCategory> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("category_name", keyword);
        return supplyCategoryMapper.selectList(queryWrapper);
    }

}
