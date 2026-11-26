package com.ape.smarthedge_risk_hub.controller;

import com.ape.smarthedge_risk_hub.common.ReParam;
import com.ape.smarthedge_risk_hub.common.Result;
import com.ape.smarthedge_risk_hub.pojo.Exchange;
import com.ape.smarthedge_risk_hub.service.IExchangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 交易所模块：基于 MyBatis-Plus 的 CRUD + 分页查询。
 *
 * <ul>
 *   <li>GET    /api/exchange/list          —— 全部交易所（Redis 缓存）</li>
 *   <li>GET    /api/exchange/page          —— 分页 + 关键词</li>
 *   <li>POST   /api/exchange/save          —— 新增</li>
 *   <li>PUT    /api/exchange/update        —— 修改</li>
 *   <li>DELETE /api/exchange/delete/{id}   —— 删除</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/exchange")
public class ExchangeController {

    @Autowired
    private IExchangeService exchangeService;

    /** 全部交易所 */
    @GetMapping("/list")
    public Result<List<Exchange>> list() {
        try {
            return Result.success(exchangeService.findAllExchange());
        } catch (Exception e) {
            return Result.fail(500, "查询失败: " + e.getMessage());
        }
    }

    /** 分页查询 */
    @GetMapping("/page")
    public Result<List<Exchange>> selectPage(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        ReParam param = new ReParam();
        param.setKeyword(keyword);
        param.setPage(page);
        param.setSize(size);
        return exchangeService.selectPage(param);
    }

    /** 新增 */
    @PostMapping("/save")
    public Result<Exchange> save(@RequestBody Exchange exchange) {
        return exchangeService.addExchange(exchange);
    }

    /** 修改 */
    @PutMapping("/update")
    public Result<Exchange> update(@RequestBody Exchange exchange) {
        return exchangeService.updateExchange(exchange);
    }

    /** 删除 */
    @DeleteMapping("/delete/{exchangeId}")
    public Result<String> delete(@PathVariable Integer exchangeId) {
        return exchangeService.deleteExchange(exchangeId);
    }
}
