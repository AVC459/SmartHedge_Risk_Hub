package com.ape.smarthedge_risk_hub.service;

import com.ape.smarthedge_risk_hub.common.ReParam;
import com.ape.smarthedge_risk_hub.common.Result;
import com.ape.smarthedge_risk_hub.pojo.FuturesContract;
import com.ape.smarthedge_risk_hub.pojo.TradingVariety;

import java.util.List;

public interface IFuturesContractService {
    List<FuturesContract> findAllFuturesContractAndRelations();
    List<FuturesContract> findFuturesContractByKeyword(String keyword);
    Result<List<FuturesContract>> selectPage(ReParam param);
    Result<List<TradingVariety>> getTradingVarieties(String keyword);
    Result addFuturesContract(FuturesContract futuresContract);
    Result<FuturesContract> getFuturesContractById(Integer contractId);
    Result updateFuturesContract(FuturesContract futuresContract);
    Result deleteFuturesContract(Integer contractId);
}