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
 * 经营者实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("operator")
public class Operator {
    @TableId(value = "operator_id", type = IdType.AUTO)
    private Integer operatorId; // 经营者唯一标识
    @TableField("operator_name")
    private String operatorName; // 经营者名称
    @TableField("status")
    private Integer status; // 状态：1=启用，0=禁用，2=已删除
    @TableField("create_time")
    private LocalDateTime createTime; // 创建时间
    @TableField("update_time")
    private LocalDateTime updateTime; // 更新时间
}