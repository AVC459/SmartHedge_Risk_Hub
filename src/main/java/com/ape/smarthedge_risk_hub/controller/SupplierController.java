package com.ape.smarthedge_risk_hub.controller;

import com.ape.smarthedge_risk_hub.common.ReParam;
import com.ape.smarthedge_risk_hub.common.Result;
import com.ape.smarthedge_risk_hub.pojo.Supplier;
import com.ape.smarthedge_risk_hub.service.ISupplierService;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/supplier")
public class SupplierController {
    @Autowired
    private ISupplierService supplierService;

    /**
     * 添加供应商请求参数
     */
    static class SupplierAddRequest {
        private Supplier supplier;
        private List<Integer> categoryIds;

        public Supplier getSupplier() {
            return supplier;
        }

        public void setSupplier(Supplier supplier) {
            this.supplier = supplier;
        }

        public List<Integer> getCategoryIds() {
            return categoryIds;
        }

        public void setCategoryIds(List<Integer> categoryIds) {
            this.categoryIds = categoryIds;
        }
    }

    /**
     * 查询所有供应商及其关联的经营者信息，支持模糊查询
     */
    @GetMapping("/list")
    public Result<List<Supplier>> list(@RequestParam(value = "keyword", required = false) String keyword) {
        try {
            List<Supplier> result;
            if (keyword != null && !keyword.trim().isEmpty()) {
                result = supplierService.findSupplierByKeyword(keyword.trim());
            } else {
                result = supplierService.findAllSupplierWithOperator();
            }
            return Result.success(result);
        } catch (Exception e) {
            return Result.fail(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询供应商及其关联的经营者信息，支持模糊查询
     */
    @GetMapping("/page")
    public Result<List<Supplier>> selectPage(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        ReParam param = new ReParam();
        param.setKeyword(keyword);
        param.setPage(page);
        param.setSize(size);
        return supplierService.selectPage(param);
    }

    /**
     * 添加供应商
     */
    @PostMapping("/add")
    public Result addSupplier(@RequestBody SupplierAddRequest request) {
        try {
            return supplierService.addSupplier(request.getSupplier(), request.getCategoryIds());
        } catch (Exception e) {
            return Result.fail(500, "添加供应商失败: " + e.getMessage());
        }
    }

    /**
     * 删除供应商
     */
    @DeleteMapping("/delete/{supplierId}")
    public Result deleteSupplier(@PathVariable Integer supplierId) {
        try {
            return supplierService.deleteSupplier(supplierId);
        } catch (Exception e) {
            return Result.fail(500, "删除供应商失败: " + e.getMessage());
        }
    }

    /**
     * 修改供应商
     */
    @PutMapping("/update")
    public Result updateSupplier(@RequestBody SupplierAddRequest request) {
        try {
            return supplierService.updateSupplier(request.getSupplier(), request.getCategoryIds());
        } catch (Exception e) {
            return Result.fail(500, "修改供应商失败: " + e.getMessage());
        }
    }

}