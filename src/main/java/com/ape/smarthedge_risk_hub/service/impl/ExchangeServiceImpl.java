package com.ape.smarthedge_risk_hub.service.impl;

import com.ape.smarthedge_risk_hub.cache.HotDataCache;
import com.ape.smarthedge_risk_hub.common.ReParam;
import com.ape.smarthedge_risk_hub.common.Result;
import com.ape.smarthedge_risk_hub.mapper.ExchangeMapper;
import com.ape.smarthedge_risk_hub.pojo.Exchange;
import com.ape.smarthedge_risk_hub.service.IExchangeService;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 交易所模块：CRUD + 分页，全量列表走 Redis 缓存。
 */
@Service
public class ExchangeServiceImpl implements IExchangeService {

    /** 缓存 key 与过期时间 */
    private static final String CACHE_KEY = "master:exchange:list";
    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    @Autowired
    private ExchangeMapper exchangeMapper;

    @Autowired
    private HotDataCache hotDataCache;

    @Override
    public List<Exchange> findAllExchange() {
        // 1. 先查缓存
        List<Exchange> cached = hotDataCache.get(CACHE_KEY, new TypeReference<List<Exchange>>() {});
        if (cached != null) {
            return cached;
        }
        // 2. 缓存未命中，查数据库并回写
        List<Exchange> list = exchangeMapper.selectList(null);
        hotDataCache.put(CACHE_KEY, list, CACHE_TTL);
        return list;
    }

    @Override
    public Result<List<Exchange>> selectPage(ReParam param) {
        try {
            List<Exchange> all = findAllExchange();

            // 关键词过滤
            String keyword = param.getKeyword();
            if (keyword != null && !keyword.trim().isEmpty()) {
                String kw = keyword.trim();
                List<Exchange> filtered = new ArrayList<>();
                for (Exchange e : all) {
                    if (e.getExchangeName() != null && e.getExchangeName().contains(kw)) {
                        filtered.add(e);
                    }
                }
                all = filtered;
            }

            // 内存分页（与项目其他模块保持一致）
            int page = param.getPage() != null ? param.getPage() : 1;
            int size = param.getSize() != null ? param.getSize() : 10;
            int start = (page - 1) * size;
            int end = Math.min(start + size, all.size());
            List<Exchange> rows = start >= all.size() ? new ArrayList<>() : all.subList(start, end);

            return Result.success(rows, all.size());
        } catch (Exception e) {
            return Result.fail(500, "查询失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Exchange> addExchange(Exchange exchange) {
        try {
            if (exchange == null || exchange.getExchangeName() == null
                    || exchange.getExchangeName().trim().isEmpty()) {
                return Result.fail(400, "交易所名称不能为空");
            }
            exchange.setExchangeName(exchange.getExchangeName().trim());
            exchangeMapper.insert(exchange);
            hotDataCache.evict(CACHE_KEY);
            return Result.success(exchange);
        } catch (Exception e) {
            return Result.fail(500, "新增失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Exchange> updateExchange(Exchange exchange) {
        try {
            if (exchange == null || exchange.getExchangeId() == null) {
                return Result.fail(400, "交易所ID不能为空");
            }
            if (exchange.getExchangeName() == null || exchange.getExchangeName().trim().isEmpty()) {
                return Result.fail(400, "交易所名称不能为空");
            }
            exchange.setExchangeName(exchange.getExchangeName().trim());
            int rows = exchangeMapper.updateById(exchange);
            if (rows == 0) {
                return Result.fail(404, "交易所不存在");
            }
            hotDataCache.evict(CACHE_KEY);
            return Result.success(exchange);
        } catch (Exception e) {
            return Result.fail(500, "修改失败: " + e.getMessage());
        }
    }

    @Override
    public Result<String> deleteExchange(Integer exchangeId) {
        try {
            if (exchangeId == null) {
                return Result.fail(400, "交易所ID不能为空");
            }
            int rows = exchangeMapper.deleteById(exchangeId);
            if (rows == 0) {
                return Result.fail(404, "交易所不存在");
            }
            hotDataCache.evict(CACHE_KEY);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.fail(500, "删除失败: " + e.getMessage());
        }
    }
}
