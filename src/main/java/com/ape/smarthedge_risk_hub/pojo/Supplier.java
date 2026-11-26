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
 * 供应商实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("supplier")
public class Supplier {
    @TableId(value = "supplier_id", type = IdType.AUTO)
    private Integer supplierId; // 供应商唯一标识
    @TableField("supplier_name")
    private String supplierName; // 供应商名称
    @TableField("contact_phone")
    private String contactPhone; // 联系电话
    @TableField("address")
    private String address; // 供应商地址
    @TableField("credit_code")
    private String creditCode; // 统一信用代码
    @TableField("operator_id")
    private Integer operatorId; // 所属经营者ID（关联operator表）
    @TableField(exist = false)
    private String operatorName; // 经营者名称（非数据库字段）
    @TableField("score")
    private Integer score; // 供应商评分
    @TableField("status")
    private Integer status; // 供应商状态：1=启用，0=禁用
    @TableField("update_time")
    private LocalDateTime updateTime; // 状态更新时间
    @TableField("create_time")
    private LocalDateTime createTime; // 创建时间
}