package com.ape.smarthedge_risk_hub.service.impl;

import com.ape.smarthedge_risk_hub.common.ReParam;
import com.ape.smarthedge_risk_hub.common.Result;
import com.ape.smarthedge_risk_hub.mapper.SupplierMapper;
import com.ape.smarthedge_risk_hub.mapper.SupplierCategoryRelMapper;
import com.ape.smarthedge_risk_hub.pojo.Supplier;
import com.ape.smarthedge_risk_hub.pojo.SupplierCategoryRel;
import com.ape.smarthedge_risk_hub.service.ISupplierService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SupplierServiceImp implements ISupplierService {
    @Autowired
    private SupplierMapper supplierMapper;
    @Autowired
    private SupplierCategoryRelMapper supplierCategoryRelMapper;

    @Override
    public List<Supplier> findAllSupplierWithOperator() {
        return supplierMapper.findAllSupplierWithOperator();
    }

    @Override
    public List<Supplier> findSupplierByKeyword(String keyword) {
        return supplierMapper.findSupplierWithKeyword(keyword);
    }

    @Override
    public Result selectPage(ReParam param) {
        try {
            // 获取所有供应商及其关联的经营者信息
            List<Supplier> allSuppliers;
            if (param.getKeyword() != null && !param.getKeyword().trim().isEmpty()) {
                // 如果有关键词，使用带模糊查询的方法
                allSuppliers = supplierMapper.findSupplierWithKeyword(param.getKeyword().trim());
            } else {
                // 如果没有关键词，使用全查方法
                allSuppliers = supplierMapper.findAllSupplierWithOperator();
            }
            // 计算分页参数
            int currentPage = param.getPage() != null ? param.getPage() : 1;
            int pageSize = param.getSize() != null ? param.getSize() : 10;
            int startIndex = (currentPage - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, allSuppliers.size());
            // 进行分页
            List<Supplier> pageSuppliers;
            if (startIndex >= allSuppliers.size()) {
                pageSuppliers = new ArrayList<>();
            } else {
                pageSuppliers = allSuppliers.subList(startIndex, endIndex);
            }
            // 返回数据和总数
            return Result.success(pageSuppliers, allSuppliers.size());
        } catch (Exception e) {
            return Result.fail(500, "查询失败: " + e.getMessage());
        }
    }

    @Override
    public Result addSupplier(Supplier supplier, List<Integer> categoryIds) {
        try {
            // 设置默认值
            supplier.setStatus(1); // 默认为启用状态
            supplier.setCreateTime(LocalDateTime.now());
            supplier.setUpdateTime(LocalDateTime.now());
            // 保存供应商信息
            supplierMapper.insert(supplier);
            // 保存供应商与供货品类的关联关系
            if (categoryIds != null && !categoryIds.isEmpty()) {
                for (Integer categoryId : categoryIds) {
                    SupplierCategoryRel rel = new SupplierCategoryRel();
                    rel.setSupplierId(supplier.getSupplierId());
                    rel.setCategoryId(categoryId);
                    supplierCategoryRelMapper.insert(rel);
                }
            }
            return Result.success("添加供应商成功");
        } catch (Exception e) {
            return Result.fail(500, "添加供应商失败: " + e.getMessage());
        }
    }

    @Override
    public Result deleteSupplier(Integer supplierId) {
        try {
            // 先删除供应商与供货品类的关联关系
            QueryWrapper<SupplierCategoryRel> relWrapper = new QueryWrapper<>();
            relWrapper.eq("supplier_id", supplierId);
            supplierCategoryRelMapper.delete(relWrapper);
            // 再删除供应商信息
            supplierMapper.deleteById(supplierId);
            return Result.success("删除供应商成功");
        } catch (Exception e) {
            return Result.fail(500, "删除供应商失败: " + e.getMessage());
        }
    }

    @Override
    public Result updateSupplier(Supplier supplier, List<Integer> categoryIds) {
        try {
            // 更新供应商信息
            supplier.setUpdateTime(LocalDateTime.now());
            supplierMapper.updateById(supplier);
            // 删除原有的供应商与供货品类的关联关系
            QueryWrapper<SupplierCategoryRel> relWrapper = new QueryWrapper<>();
            relWrapper.eq("supplier_id", supplier.getSupplierId());
            supplierCategoryRelMapper.delete(relWrapper);
            // 重新创建供应商与供货品类的关联关系
            if (categoryIds != null && !categoryIds.isEmpty()) {
                for (Integer categoryId : categoryIds) {
                    SupplierCategoryRel rel = new SupplierCategoryRel();
                    rel.setSupplierId(supplier.getSupplierId());
                    rel.setCategoryId(categoryId);
                    supplierCategoryRelMapper.insert(rel);
                }
            }
            return Result.success("修改供应商成功");
        } catch (Exception e) {
            return Result.fail(500, "修改供应商失败: " + e.getMessage());
        }
    }

}