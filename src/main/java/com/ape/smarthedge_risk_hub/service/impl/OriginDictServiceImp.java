package com.ape.smarthedge_risk_hub.service.impl;

import com.ape.smarthedge_risk_hub.mapper.OriginDictMapper;
import com.ape.smarthedge_risk_hub.pojo.OriginDict;
import com.ape.smarthedge_risk_hub.service.IOriginDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OriginDictServiceImp implements IOriginDictService {

    @Autowired
    private OriginDictMapper originDictMapper;

    @Override
    public List<OriginDict> findAllOriginDict() {
        return originDictMapper.selectList(null);
    }

}
