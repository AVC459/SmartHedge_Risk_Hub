package com.ape.smarthedge_risk_hub.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 供应商-供货品类关联实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("supplier_category_rel")
public class SupplierCategoryRel {
    @TableId(value = "rel_id", type = IdType.AUTO)
    private Integer relId; // 关联记录唯一标识
    @TableField("supplier_id")
    private Integer supplierId; // 供应商ID（关联supplier表）
    @TableField("category_id")
    private Integer categoryId; // 供货品类ID（关联supply_category表）
}