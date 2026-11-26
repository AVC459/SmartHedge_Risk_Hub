package com.ape.smarthedge_risk_hub.service.impl;

import com.ape.smarthedge_risk_hub.common.ReParam;
import com.ape.smarthedge_risk_hub.common.Result;
import com.ape.smarthedge_risk_hub.mapper.FuturesContractMapper;
import com.ape.smarthedge_risk_hub.mapper.TradingVarietyMapper;
import com.ape.smarthedge_risk_hub.pojo.FuturesContract;
import com.ape.smarthedge_risk_hub.pojo.TradingVariety;
import com.ape.smarthedge_risk_hub.service.IFuturesContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class FuturesContractServiceImp implements IFuturesContractService {
    @Autowired
    private FuturesContractMapper futuresContractMapper;
    @Autowired
    private TradingVarietyMapper tradingVarietyMapper;

    public List<FuturesContract> findAllFuturesContractAndRelations(){
        return futuresContractMapper.findAllFuturesContractAndRelations();
    }
    
    public List<FuturesContract> findFuturesContractByKeyword(String keyword){
        return futuresContractMapper.findFuturesContractByKeyword(keyword);
    }
    public int insertFuturesContract(FuturesContract futuresContract){
        //设置更新时间为当前时间
        futuresContract.setUpdateTime(LocalDateTime.now());
        return futuresContractMapper.insert(futuresContract);
    }
    @Override
    public Result<List<FuturesContract>> selectPage(ReParam param) {
        try {
            // 获取所有期货合约及其关联的交易品种、所属行业、交易所信息
            List<FuturesContract> allFuturesContracts;
            if (param.getKeyword() != null && !param.getKeyword().trim().isEmpty()) {
                // 如果有关键词，使用带模糊查询的方法
                allFuturesContracts = futuresContractMapper.findFuturesContractByKeyword(param.getKeyword().trim());
            } else {
                // 如果没有关键词，使用全查方法
                allFuturesContracts = futuresContractMapper.findAllFuturesContractAndRelations();
            }
            
            // 计算分页参数
            int currentPage = param.getPage() != null ? param.getPage() : 1;
            int pageSize = param.getSize() != null ? param.getSize() : 10;
            int startIndex = (currentPage - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, allFuturesContracts.size());
            
            // 进行分页
            List<FuturesContract> pageFuturesContracts;
            if (startIndex >= allFuturesContracts.size()) {
                pageFuturesContracts = new ArrayList<>();
            } else {
                pageFuturesContracts = allFuturesContracts.subList(startIndex, endIndex);
            }
            
            // 返回数据和总数
            return Result.success(pageFuturesContracts, allFuturesContracts.size());
        } catch (Exception e) {
            return Result.fail(500, "查询失败: " + e.getMessage());
        }
    }

    @Override
    public Result<List<TradingVariety>> getTradingVarieties(String keyword) {
        try {
            List<TradingVariety> tradingVarieties;
            if (keyword != null && !keyword.trim().isEmpty()) {
                tradingVarieties = tradingVarietyMapper.findTradingVarietyByKeyword(keyword.trim());
            } else {
                tradingVarieties = tradingVarietyMapper.findAllTradingVarietyAndRelations();
            }
            return Result.success(tradingVarieties);
        } catch (Exception e) {
            return Result.fail(500, "获取交易品种失败: " + e.getMessage());
        }
    }

    @Override
    public Result addFuturesContract(FuturesContract futuresContract) {
        try {
            // 设置更新时间为当前时间
            futuresContract.setUpdateTime(LocalDateTime.now());
            // 插入期货合约
            int result = futuresContractMapper.insert(futuresContract);
            if (result > 0) {
                return Result.success(futuresContract);
            } else {
                return Result.fail(500, "添加失败");
            }
        } catch (Exception e) {
            return Result.fail(500, "添加失败: " + e.getMessage());
        }
    }

    @Override
    public Result<FuturesContract> getFuturesContractById(Integer contractId) {
        try {
            // 先获取所有期货合约及其关联信息
            List<FuturesContract> allContracts = findAllFuturesContractAndRelations();
            // 查找指定 ID 的合约
            for (FuturesContract contract : allContracts) {
                if (contract.getContractId().equals(contractId)) {
                    return Result.success(contract);
                }
            }
            return Result.fail(404, "期货合约不存在");
        } catch (Exception e) {
            return Result.fail(500, "查询失败: " + e.getMessage());
        }
    }

    @Override
    public Result updateFuturesContract(FuturesContract futuresContract) {
        try {
            // 设置更新时间为当前时间
            futuresContract.setUpdateTime(LocalDateTime.now());
            // 更新期货合约
            int result = futuresContractMapper.updateById(futuresContract);
            if (result > 0) {
                return Result.success(futuresContract);
            } else {
                return Result.fail(404, "期货合约不存在或无变更");
            }
        } catch (Exception e) {
            return Result.fail(500, "更新失败: " + e.getMessage());
        }
    }

    @Override
    public Result deleteFuturesContract(Integer contractId) {
        try {
            // 删除期货合约
            int result = futuresContractMapper.deleteById(contractId);
            if (result > 0) {
                return Result.success(null);
            } else {
                return Result.fail(404, "期货合约不存在");
            }
        } catch (Exception e) {
            return Result.fail(500, "删除失败: " + e.getMessage());
        }
    }
}