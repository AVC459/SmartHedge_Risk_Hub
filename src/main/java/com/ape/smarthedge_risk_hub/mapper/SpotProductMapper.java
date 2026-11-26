package com.ape.smarthedge_risk_hub.mapper;

import com.ape.smarthedge_risk_hub.pojo.SpotProduct;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SpotProductMapper extends BaseMapper<SpotProduct>{

    @Select("select * from spot_products")
    @Results(id = "spotProductMap", value = {
            @Result(property = "id",column = "id"),
            @Result(property= "spotName", column = "spot_name"),
            @Result(property= "spotSpec", column = "spot_spec"),
            @Result(property= "originId", column = "origin_id"),
            @Result(property= "spotUnit", column = "spot_unit"),
            @Result(property= "updateTime", column = "update_time"),
            @Result(column="originId", property="origin",
                    one=@One(select="com.ape.smarthedge_risk_hub.mapper.OriginDictMapper.findById"))
    })
    List<SpotProduct> findAllSpotProductAndOriginDict();

    @Select("select * from spot_products where spot_name like concat('%', #{keyword}, '%') or spot_spec like concat('%', #{keyword}, '%')")
    @ResultMap("spotProductMap")
    List<SpotProduct> findSpotProductByKeyword(@Param("keyword") String keyword);

//    @Insert("insert into spot_products(spot_name, spot_spec, origin_id, spot_unit, update_time) values (#{spotName}, #{spotSpec}, #{originId}, #{spotUnit}, #{updateTime})")
//    int insertSpotProduct(SpotProduct spotProduct);

    // 继承BaseMapper后，分页查询可以直接使用selectPage方法，无需额外定义

}
