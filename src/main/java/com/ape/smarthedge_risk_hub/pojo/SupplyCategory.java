package com.ape.smarthedge_risk_hub.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 供货品类实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("supply_category")
public class SupplyCategory {
    @TableId(value = "category_id", type = IdType.AUTO)
    private Integer categoryId; // 供货品类唯一标识
    @TableField("category_name")
    private String categoryName; // 品类名称（如玉米、螺纹钢）
    @TableField("specifications")
    private String specifications; // 规格参数（如品种、等级、水分）
    @TableField("origin")
    private String origin; // 产地/交割地
    @TableField("unit")
    private String unit; // 计量单位（如吨）
    @TableField("create_time")
    private LocalDateTime createTime; // 创建时间
}