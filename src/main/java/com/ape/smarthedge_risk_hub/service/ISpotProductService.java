package com.ape.smarthedge_risk_hub.service;

import com.ape.smarthedge_risk_hub.common.ReParam;
import com.ape.smarthedge_risk_hub.common.Result;
import com.ape.smarthedge_risk_hub.pojo.SpotProduct;

import java.util.List;

public interface ISpotProductService {
    List<SpotProduct> findAllSpotProductAndOriginDict();
    List<SpotProduct> findSpotProductByKeyword(String keyword);
    int insertSpotProduct(SpotProduct spotProduct);
    Result<List<SpotProduct>> selectPage(ReParam param);
}
