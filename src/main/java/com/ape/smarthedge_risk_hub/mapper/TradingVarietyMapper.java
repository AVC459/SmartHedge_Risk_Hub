package com.ape.smarthedge_risk_hub.mapper;

import com.ape.smarthedge_risk_hub.pojo.TradingVariety;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TradingVarietyMapper extends BaseMapper<TradingVariety> {

    @Select("select tv.*, e.exchange_id as e_exchange_id, e.exchange_name, i.industry_id as i_industry_id, i.industry_name " +
            "from trading_variety tv " +
            "left join exchange e on tv.exchange_id = e.exchange_id " +
            "left join industry i on tv.industry_id = i.industry_id")
    @Results(id = "tradingVarietyMap", value = {
            @Result(column = "variety_id", property = "varietyId"),
            @Result(column = "variety_name", property = "varietyName"),
            @Result(column = "variety_code", property = "varietyCode"),
            @Result(column = "exchange_id", property = "exchangeId"),
            @Result(column = "industry_id", property = "industryId"),
            @Result(column = "e_exchange_id", property = "exchange.exchangeId"),
            @Result(column = "exchange_name", property = "exchange.exchangeName"),
            @Result(column = "i_industry_id", property = "industry.industryId"),
            @Result(column = "industry_name", property = "industry.industryName")
    })
    List<TradingVariety> findAllTradingVarietyAndRelations();
    
    @Select("select tv.*, e.exchange_id as e_exchange_id, e.exchange_name, i.industry_id as i_industry_id, i.industry_name " +
            "from trading_variety tv " +
            "left join exchange e on tv.exchange_id = e.exchange_id " +
            "left join industry i on tv.industry_id = i.industry_id " +
            "where tv.variety_name like concat('%', #{keyword}, '%') " +
            "or tv.variety_code like concat('%', #{keyword}, '%') " +
            "or e.exchange_name like concat('%', #{keyword}, '%') " +
            "or i.industry_name like concat('%', #{keyword}, '%')")
    @ResultMap("tradingVarietyMap")
    List<TradingVariety> findTradingVarietyByKeyword(@Param("keyword") String keyword);

    // 继承BaseMapper后，分页查询可以直接使用selectPage方法，无需额外定义

}
