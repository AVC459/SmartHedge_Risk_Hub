package com.ape.smarthedge_risk_hub.service;

import com.ape.smarthedge_risk_hub.common.ReParam;
import com.ape.smarthedge_risk_hub.common.Result;
import com.ape.smarthedge_risk_hub.pojo.TradingVariety;

import java.util.List;

public interface ITradingVarietyService {

    List<TradingVariety> findAllTradingVarietyAndRelations();
    
    List<TradingVariety> findTradingVarietyByKeyword(String keyword);

    Result<List<TradingVariety>> selectPage(ReParam param);

}