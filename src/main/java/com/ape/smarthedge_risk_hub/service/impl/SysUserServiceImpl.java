package com.ape.smarthedge_risk_hub.service.impl;

import com.ape.smarthedge_risk_hub.mapper.SysUserMapper;
import com.ape.smarthedge_risk_hub.pojo.SysUser;
import com.ape.smarthedge_risk_hub.service.ISysUserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SysUserServiceImpl implements ISysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public SysUser login(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            return null;
        }
        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username.trim());
        SysUser user = sysUserMapper.selectOne(wrapper);
        if (user == null || !password.equals(user.getPassword())) {
            return null;
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new IllegalStateException("该账号已被禁用，请联系管理员");
        }
        return user;
    }

    @Override
    public SysUser register(String username, String password, String nickname) {
        String name = username == null ? "" : username.trim();
        if (name.isEmpty() || password == null || password.isEmpty()) {
            throw new IllegalStateException("请输入用户名和密码");
        }
        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        wrapper.eq("username", name);
        if (sysUserMapper.selectCount(wrapper) > 0) {
            throw new IllegalStateException("用户名已存在，请更换后重试");
        }
        SysUser user = new SysUser();
        user.setUsername(name);
        user.setPassword(password);
        user.setNickname(nickname == null || nickname.trim().isEmpty() ? name : nickname.trim());
        user.setRole("operator");
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        sysUserMapper.insert(user);
        return user;
    }
}
