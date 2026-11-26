package com.ape.smarthedge_risk_hub.mapper;

import com.ape.smarthedge_risk_hub.pojo.FuturesContract;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FuturesContractMapper extends BaseMapper<FuturesContract> {

    @Select("select fc.*, tv.variety_id as tv_variety_id, tv.variety_name, tv.variety_code, tv.exchange_id, tv.industry_id, " +
            "e.exchange_id as e_exchange_id, e.exchange_name, " +
            "i.industry_id as i_industry_id, i.industry_name " +
            "from futures_contract fc " +
            "left join trading_variety tv on fc.variety_id = tv.variety_id " +
            "left join exchange e on tv.exchange_id = e.exchange_id " +
            "left join industry i on tv.industry_id = i.industry_id")
    @Results(id = "futuresContractMap", value = {
            @Result(property = "contractId", column = "contract_id"),
            @Result(property = "contractCode", column = "contract_code"),
            @Result(property = "contractName", column = "contract_name"),
            @Result(property = "lastTradingDay", column = "last_trading_day"),
            @Result(property = "varietyId", column = "variety_id"),
            @Result(property = "updateMethod", column = "update_method"),
            @Result(property = "updateTime", column = "update_time"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "tradingVariety.varietyId", column = "tv_variety_id"),
            @Result(property = "tradingVariety.varietyName", column = "variety_name"),
            @Result(property = "tradingVariety.varietyCode", column = "variety_code"),
            @Result(property = "tradingVariety.exchangeId", column = "exchange_id"),
            @Result(property = "tradingVariety.industryId", column = "industry_id"),
            @Result(property = "industry.industryId", column = "i_industry_id"),
            @Result(property = "industry.industryName", column = "industry_name"),
            @Result(property = "exchange.exchangeId", column = "e_exchange_id"),
            @Result(property = "exchange.exchangeName", column = "exchange_name")
    })
    List<FuturesContract> findAllFuturesContractAndRelations();

    @Select("select fc.*, tv.variety_id as tv_variety_id, tv.variety_name, tv.variety_code, tv.exchange_id, tv.industry_id, " +
            "e.exchange_id as e_exchange_id, e.exchange_name, " +
            "i.industry_id as i_industry_id, i.industry_name " +
            "from futures_contract fc " +
            "left join trading_variety tv on fc.variety_id = tv.variety_id " +
            "left join exchange e on tv.exchange_id = e.exchange_id " +
            "left join industry i on tv.industry_id = i.industry_id " +
            "where fc.contract_code like concat('%', #{keyword}, '%') or fc.contract_name like concat('%', #{keyword}, '%')")
    @ResultMap("futuresContractMap")
    List<FuturesContract> findFuturesContractByKeyword(@Param("keyword") String keyword);

    // 继承BaseMapper后，分页查询可以直接使用selectPage方法，无需额外定义

}