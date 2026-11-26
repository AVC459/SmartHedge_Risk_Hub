package com.ape.smarthedge_risk_hub.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
// 分页参数
public class ReParam {
    private String keyword;
    /**
     * 页数 从1开始
     */
    private Integer page;
    /**
     * 每页显示条数
     */
    private Integer size;

}
