package com.ape.smarthedge_risk_hub.service.impl;

import com.ape.smarthedge_risk_hub.common.ReParam;
import com.ape.smarthedge_risk_hub.common.Result;
import com.ape.smarthedge_risk_hub.mapper.SpotProductMapper;
import com.ape.smarthedge_risk_hub.pojo.SpotProduct;
import com.ape.smarthedge_risk_hub.service.ISpotProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SpotProductServiceImp implements ISpotProductService {
    @Autowired
    private SpotProductMapper spotProductMapper;

    public List<SpotProduct> findAllSpotProductAndOriginDict(){
        return spotProductMapper.findAllSpotProductAndOriginDict();
    }
    
    public List<SpotProduct> findSpotProductByKeyword(String keyword){
        return spotProductMapper.findSpotProductByKeyword(keyword);
    }
    public int insertSpotProduct(SpotProduct spotProduct){
        //设置更新时间为当前时间
        spotProduct.setUpdateTime(LocalDateTime.now());
        //return spotProductMapper.insertSpotProduct(spotProduct);
        return spotProductMapper.insert(spotProduct);
    }
    @Override
    public Result<List<SpotProduct>> selectPage(ReParam param) {
        try {
            // 获取所有现货商品及其关联的产地信息
            List<SpotProduct> allSpotProducts;
            if (param.getKeyword() != null && !param.getKeyword().trim().isEmpty()) {
                // 如果有关键词，使用带模糊查询的方法
                allSpotProducts = spotProductMapper.findSpotProductByKeyword(param.getKeyword().trim());
            } else {
                // 如果没有关键词，使用全查方法
                allSpotProducts = spotProductMapper.findAllSpotProductAndOriginDict();
            }
            
            // 计算分页参数
            int currentPage = param.getPage() != null ? param.getPage() : 1;
            int pageSize = param.getSize() != null ? param.getSize() : 10;
            int startIndex = (currentPage - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, allSpotProducts.size());
            
            // 进行分页
            List<SpotProduct> pageSpotProducts;
            if (startIndex >= allSpotProducts.size()) {
                pageSpotProducts = new ArrayList<>();
            } else {
                pageSpotProducts = allSpotProducts.subList(startIndex, endIndex);
            }
            
            // 返回数据和总数
            return Result.success(pageSpotProducts, allSpotProducts.size());
        } catch (Exception e) {
            return Result.fail(500, "查询失败: " + e.getMessage());
        }
    }
}
