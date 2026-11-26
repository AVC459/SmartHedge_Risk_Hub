package com.ape.smarthedge_risk_hub.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 交易所实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("exchange")
public class Exchange {
    @TableId(value = "exchange_id", type = IdType.AUTO)
    private Integer exchangeId; // 交易所ID
    @TableField("exchange_name")
    private String exchangeName; // 交易所名称
}