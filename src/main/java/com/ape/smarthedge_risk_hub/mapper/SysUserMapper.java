package com.ape.smarthedge_risk_hub.mapper;

import com.ape.smarthedge_risk_hub.pojo.SysUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 登录用户 Mapper：继承 BaseMapper，按 username 条件查询即可，无需额外 SQL。
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}
