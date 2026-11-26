package com.ape.smarthedge_risk_hub.controller;

import com.ape.smarthedge_risk_hub.common.Result;
import com.ape.smarthedge_risk_hub.pojo.SupplyCategory;
import com.ape.smarthedge_risk_hub.service.ISupplyCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/supplyCategory")
public class SupplyCategoryController {
    @Autowired
    private ISupplyCategoryService supplyCategoryService;

    /**
     * 查询所有供货品类
     */
    @GetMapping("/list")
    public Result<List<SupplyCategory>> list() {
        try {
            List<SupplyCategory> categories = supplyCategoryService.findAllSupplyCategories();
            return Result.success(categories);
        } catch (Exception e) {
            return Result.fail(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据关键词模糊查询供货品类
     */
    @GetMapping("/search")
    public Result<List<SupplyCategory>> search(@RequestParam(value = "keyword", required = false) String keyword) {
        try {
            List<SupplyCategory> categories;
            if (keyword != null && !keyword.trim().isEmpty()) {
                categories = supplyCategoryService.findSupplyCategoriesByKeyword(keyword.trim());
            } else {
                categories = supplyCategoryService.findAllSupplyCategories();
            }
            return Result.success(categories);
        } catch (Exception e) {
            return Result.fail(500, "查询失败: " + e.getMessage());
        }
    }

}
