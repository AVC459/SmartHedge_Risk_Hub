package com.ape.smarthedge_risk_hub.mapper;

import com.ape.smarthedge_risk_hub.pojo.OriginDict;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OriginDictMapper extends BaseMapper<OriginDict> {

    /**
     * 按主键查询产地（现货商品的关联查询使用：SpotProductMapper 中 @One 引用了本方法）
     */
    @Select("select * from origin_dict where origin_id = #{originId}")
    @Results(id = "originDictMap", value = {
            @Result(property = "originId", column = "origin_id"),
            @Result(property = "originName", column = "origin_name"),
            @Result(property = "originShortName", column = "origin_short_name"),
            @Result(property = "createTime", column = "create_time")
    })
    OriginDict findById(@Param("originId") Integer originId);

}
