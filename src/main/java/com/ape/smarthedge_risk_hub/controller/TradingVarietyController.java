package com.ape.smarthedge_risk_hub.controller;

import com.ape.smarthedge_risk_hub.common.ReParam;
import com.ape.smarthedge_risk_hub.common.Result;
import com.ape.smarthedge_risk_hub.pojo.TradingVariety;
import com.ape.smarthedge_risk_hub.service.ITradingVarietyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tradingvariety")
public class TradingVarietyController {
    @Autowired
    private ITradingVarietyService tradingVarietyService;
    /**
     * 查询所有交易品种及其关联的供应商信息
     */
    @GetMapping("/list")
    public Result<List<TradingVariety>> list(@RequestParam(value = "keyword", required = false) String keyword) {
        try {
            List<TradingVariety> result;
            if (keyword != null && !keyword.trim().isEmpty()) {
                result = tradingVarietyService.findTradingVarietyByKeyword(keyword.trim());
            } else {
                result = tradingVarietyService.findAllTradingVarietyAndRelations();
            }
            return Result.success(result);
        } catch (Exception e) {
            return Result.fail(500, "查询失败: " + e.getMessage());
        }
    }
    /**
     * 分页查询交易品种及其关联的供应商信息
     */
    @GetMapping("/page")
    public Result<List<TradingVariety>> selectPage(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        ReParam param = new ReParam();
        param.setKeyword(keyword);
        param.setPage(page);
        param.setSize(size);
        return tradingVarietyService.selectPage(param);
    }
}
