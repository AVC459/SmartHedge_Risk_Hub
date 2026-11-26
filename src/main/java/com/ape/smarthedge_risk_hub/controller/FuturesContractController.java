package com.ape.smarthedge_risk_hub.controller;

import com.ape.smarthedge_risk_hub.common.ReParam;
import com.ape.smarthedge_risk_hub.common.Result;
import com.ape.smarthedge_risk_hub.pojo.FuturesContract;
import com.ape.smarthedge_risk_hub.pojo.TradingVariety;
import com.ape.smarthedge_risk_hub.service.IFuturesContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/futuresContract")
public class FuturesContractController {
    @Autowired
    private IFuturesContractService futuresContractService;

    /**
     * 查询所有期货合约及其关联的交易品种、所属行业、交易所信息，支持模糊查询
     */
    @GetMapping("/list")
    public Result<List<FuturesContract>> list(@RequestParam(value = "keyword", required = false) String keyword) {
        try {
            List<FuturesContract> result;
            if (keyword != null && !keyword.trim().isEmpty()) {
                result = futuresContractService.findFuturesContractByKeyword(keyword.trim());
            } else {
                result = futuresContractService.findAllFuturesContractAndRelations();
            }
            return Result.success(result);
        } catch (Exception e) {
            return Result.fail(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询期货合约及其关联的交易品种、所属行业、交易所信息，支持模糊查询
     */
    @GetMapping("/page")
    public Result<List<FuturesContract>> selectPage(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        ReParam param = new ReParam();
        param.setKeyword(keyword);
        param.setPage(page);
        param.setSize(size);
        return futuresContractService.selectPage(param);
    }

    /**
     * 获取交易品种列表，支持模糊查询
     */
    @GetMapping("/tradingVarieties")
    public Result<List<TradingVariety>> getTradingVarieties(@RequestParam(value = "keyword", required = false) String keyword) {
        return futuresContractService.getTradingVarieties(keyword);
    }

    /**
     * 添加期货合约
     */
    @PostMapping("/add")
    public Result addFuturesContract(@RequestBody FuturesContract futuresContract) {
        return futuresContractService.addFuturesContract(futuresContract);
    }

    /**
     * 获取单个期货合约详情（用于编辑时回填数据）
     */
    @GetMapping("/getById")
    public Result<FuturesContract> getById(@RequestParam("contractId") Integer contractId) {
        return futuresContractService.getFuturesContractById(contractId);
    }

    /**
     * 更新期货合约
     */
    @PutMapping("/update")
    public Result updateFuturesContract(@RequestBody FuturesContract futuresContract) {
        return futuresContractService.updateFuturesContract(futuresContract);
    }

    /**
     * 删除期货合约
     */
    @DeleteMapping("/delete/{contractId}")
    public Result deleteFuturesContract(@PathVariable Integer contractId) {
        return futuresContractService.deleteFuturesContract(contractId);
    }
}