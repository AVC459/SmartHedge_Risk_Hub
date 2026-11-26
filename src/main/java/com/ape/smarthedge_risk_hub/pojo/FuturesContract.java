package com.ape.smarthedge_risk_hub.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 期货合约实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("futures_contract")
public class FuturesContract {
    @TableId(value = "contract_id", type = IdType.AUTO)
    private Integer contractId; // 期货合约唯一标识
    @TableField("contract_code")
    private String contractCode; // 合约代码（页面核心字段：如C2603、RB2610）
    @TableField("contract_name")
    private String contractName; // 合约名称（页面核心字段：如玉米2603、螺纹钢2610）
    @TableField("last_trading_day")
    private LocalDate lastTradingDay; // 最后交易日（页面核心字段）
    @TableField("variety_id")
    private Integer varietyId; // 关联交易品种ID（关联trading_variety表，核心外键）
    @TableField("update_method")
    private String updateMethod; // 数据更新方式（页面字段）
    @TableField("update_time")
    private LocalDateTime updateTime; // 数据更新时间（页面字段）
    @TableField("create_time")
    private LocalDateTime createTime; // 表记录创建时间（后台维护）

    // 关联对象
    @TableField(exist = false)
    private TradingVariety tradingVariety; // 交易品种
    @TableField(exist = false)
    private Industry industry; // 所属行业
    @TableField(exist = false)
    private Exchange exchange; // 交易所

}