package com.ape.smarthedge_risk_hub.service.impl;

import com.ape.smarthedge_risk_hub.mapper.OperatorMapper;
import com.ape.smarthedge_risk_hub.pojo.Operator;
import com.ape.smarthedge_risk_hub.service.IOperatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OperatorServiceImp implements IOperatorService {

    @Autowired
    private OperatorMapper operatorMapper;

    @Override
    public List<Operator> findAllOperators() {
        return operatorMapper.selectList(null);
    }

}
