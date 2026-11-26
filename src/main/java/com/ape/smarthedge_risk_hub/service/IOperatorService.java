package com.ape.smarthedge_risk_hub.service;

import com.ape.smarthedge_risk_hub.pojo.Operator;

import java.util.List;

public interface IOperatorService {

    /**
     * 查询所有经营者
     */
    List<Operator> findAllOperators();

}
