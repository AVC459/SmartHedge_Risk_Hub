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
 * 大宗商品产地实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("origin_dict")
public class OriginDict {
    @TableId(value = "origin_id", type = IdType.AUTO)
    private Integer originId; // 产地ID
    @TableField("origin_name")
    private String originName; // 产地名称
    @TableField("origin_short_name")
    private String originShortName; // 产地简称
    @TableField("create_time")
    private LocalDateTime createTime; // 创建时间

}
