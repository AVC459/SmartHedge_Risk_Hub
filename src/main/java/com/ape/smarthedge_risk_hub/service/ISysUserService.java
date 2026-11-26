package com.ape.smarthedge_risk_hub.service;

import com.ape.smarthedge_risk_hub.pojo.SysUser;

public interface ISysUserService {

    /**
     * 校验账号密码。
     *
     * @return 校验通过返回用户对象；账号不存在或密码错误返回 null；
     *         账号被禁用抛出 IllegalStateException
     */
    SysUser login(String username, String password);

    /**
     * 注册新账号。
     *
     * @return 注册成功返回新用户；用户名已存在抛出 IllegalStateException
     */
    SysUser register(String username, String password, String nickname);
}
