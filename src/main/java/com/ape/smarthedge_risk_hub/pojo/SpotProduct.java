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
 * 大宗商品现货信息实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("spot_products")
public class SpotProduct {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id; // 现货ID
    @TableField("spot_name")
    private String spotName; // 现货名称
    @TableField("spot_spec")
    private String spotSpec; // 现货规格
    @TableField("origin_id")
    private Integer originId; // 产地ID（关联origin_dict表）
    @TableField("spot_unit")
    private String spotUnit; // 现货单位
    @TableField("update_time")
    private LocalDateTime updateTime; // 更新时间

    // 关联对象（非数据库字段）：产地
    @TableField(exist = false)
    private OriginDict origin;

}
