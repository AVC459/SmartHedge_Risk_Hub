package com.ape.smarthedge_risk_hub.service;

import com.ape.smarthedge_risk_hub.common.ReParam;
import com.ape.smarthedge_risk_hub.common.Result;
import com.ape.smarthedge_risk_hub.pojo.Exchange;

import java.util.List;

public interface IExchangeService {

    /** 查询全部交易所（走 Redis 缓存） */
    List<Exchange> findAllExchange();

    /** 分页 + 关键词查询 */
    Result<List<Exchange>> selectPage(ReParam param);

    /** 新增交易所 */
    Result<Exchange> addExchange(Exchange exchange);

    /** 修改交易所 */
    Result<Exchange> updateExchange(Exchange exchange);

    /** 删除交易所 */
    Result<String> deleteExchange(Integer exchangeId);
}
