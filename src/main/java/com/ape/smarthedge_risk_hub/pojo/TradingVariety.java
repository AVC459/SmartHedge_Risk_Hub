package com.ape.smarthedge_risk_hub.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 交易品种实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("trading_variety")
public class TradingVariety {
    @TableId(value = "variety_id", type = IdType.AUTO)
    private Integer varietyId; // 交易品种唯一标识
    @TableField("variety_name")
    private String varietyName; // 交易品种名称（如工业硅、碳酸锂）
    @TableField("variety_code")
    private String varietyCode; // 交易品种代码（如SI、LC）
    @TableField("exchange_id")
    private Integer exchangeId; // 所属交易所ID（关联exchange表）
    @TableField("industry_id")
    private Integer industryId; // 所属行业ID（关联industry表）
    @TableField(exist = false)
    private Exchange exchange; // 关联的交易所对象
    @TableField(exist = false)
    private Industry industry; // 关联的行业对象
}