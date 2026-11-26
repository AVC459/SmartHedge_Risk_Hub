package com.ape.smarthedge_risk_hub.service.impl;

import com.ape.smarthedge_risk_hub.cache.HotDataCache;
import com.ape.smarthedge_risk_hub.common.ReParam;
import com.ape.smarthedge_risk_hub.common.Result;
import com.ape.smarthedge_risk_hub.mapper.TradingVarietyMapper;
import com.ape.smarthedge_risk_hub.pojo.TradingVariety;
import com.ape.smarthedge_risk_hub.service.ITradingVarietyService;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class TradingVarietyServiceImp implements ITradingVarietyService {

    /** 品种全量列表缓存（含交易所、行业关联信息） */
    private static final String CACHE_KEY = "master:tradingvariety:list";
    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    @Autowired
    private TradingVarietyMapper tradingVarietyMapper;

    @Autowired
    private HotDataCache hotDataCache;

    @Override
    public List<TradingVariety> findAllTradingVarietyAndRelations() {
        // 先查 Redis，未命中再查库
        List<TradingVariety> cached = hotDataCache.get(CACHE_KEY, new TypeReference<List<TradingVariety>>() {});
        if (cached != null) {
            return cached;
        }
        List<TradingVariety> list = tradingVarietyMapper.findAllTradingVarietyAndRelations();
        hotDataCache.put(CACHE_KEY, list, CACHE_TTL);
        return list;
    }
    
    @Override
    public List<TradingVariety> findTradingVarietyByKeyword(String keyword) {
        return tradingVarietyMapper.findTradingVarietyByKeyword(keyword);
    }

    @Override
    public Result<List<TradingVariety>> selectPage(ReParam param) {
        try {
            //获取所有交易品种及其关联信息
            List<TradingVariety> allTradingVariety;
            if (param.getKeyword() != null && !param.getKeyword().trim().isEmpty()) {
                //如果有关键词，使用带模糊查询的方法
                allTradingVariety = tradingVarietyMapper.findTradingVarietyByKeyword(param.getKeyword().trim());
            } else {
                //如果没有关键词，使用全查方法
                allTradingVariety = tradingVarietyMapper.findAllTradingVarietyAndRelations();
            }
            // 计算分页参数
            int currentPage = param.getPage() != null ? param.getPage() : 1;
            int pageSize = param.getSize() != null ? param.getSize() : 10;
            int startIndex = (currentPage - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, allTradingVariety.size());
            // 进行分页
            List<TradingVariety> pageTradingVariety;
            if (startIndex >= allTradingVariety.size()) {
                pageTradingVariety = new ArrayList<>();
            } else {
                pageTradingVariety = allTradingVariety.subList(startIndex, endIndex);
            }
            // 返回数据和总数
            return Result.success(pageTradingVariety, allTradingVariety.size());
        } catch (Exception e) {
            return Result.fail(500, "查询失败: " + e.getMessage());
        }
    }
}