package com.ape.smarthedge_risk_hub.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 行业实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("industry")
public class Industry {
    @TableId(value = "industry_id", type = IdType.AUTO)
    private Integer industryId; // 行业ID
    @TableField("industry_name")
    private String industryName; // 行业名称（如能源化工、黑色金属）
}