package com.ape.smarthedge_risk_hub.service;

import com.ape.smarthedge_risk_hub.pojo.OriginDict;

import java.util.List;

public interface IOriginDictService {

    /**
     * 查询全部产地字典
     */
    List<OriginDict> findAllOriginDict();

}
