package com.ape.smarthedge_risk_hub.controller;

import com.ape.smarthedge_risk_hub.common.Result;
import com.ape.smarthedge_risk_hub.pojo.SysUser;
import com.ape.smarthedge_risk_hub.service.ISysUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 登录鉴权接口（Session 会话）：
 * <ul>
 *   <li>POST /api/user/login    —— 登录，成功后写入 Session</li>
 *   <li>POST /api/user/register —— 注册新账号</li>
 *   <li>POST /api/user/logout   —— 登出，销毁 Session</li>
 *   <li>GET  /api/user/current  —— 当前登录用户，未登录返回 401</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    /** Session 中保存登录用户的键 */
    public static final String SESSION_USER = "LOGIN_USER";

    @Autowired
    private ISysUserService sysUserService;

    /** 登录 */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String username = request == null ? null : request.getUsername();
        String password = request == null ? null : request.getPassword();
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            return Result.fail(400, "请输入用户名和密码");
        }
        try {
            SysUser user = sysUserService.login(username, password);
            if (user == null) {
                return Result.fail(401, "用户名或密码错误");
            }
            httpRequest.getSession(true).setAttribute(SESSION_USER, user);
            return Result.success(toVO(user));
        } catch (IllegalStateException e) {
            return Result.fail(403, e.getMessage());
        } catch (Exception e) {
            return Result.fail(500, "登录失败：" + e.getMessage());
        }
    }

    /** 注册新账号 */
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody RegisterRequest request) {
        String username = request == null ? null : request.getUsername();
        String password = request == null ? null : request.getPassword();
        String nickname = request == null ? null : request.getNickname();
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            return Result.fail(400, "请输入用户名和密码");
        }
        if (password.length() < 6) {
            return Result.fail(400, "密码长度至少 6 位");
        }
        try {
            SysUser user = sysUserService.register(username, password, nickname);
            return Result.success(toVO(user));
        } catch (IllegalStateException e) {
            return Result.fail(409, e.getMessage());
        } catch (Exception e) {
            return Result.fail(500, "注册失败：" + e.getMessage());
        }
    }

    /** 登出 */
    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return Result.success("已退出登录");
    }

    /** 当前登录用户（前端路由守卫用） */
    @GetMapping("/current")
    public Result<Map<String, Object>> current(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        SysUser user = session == null ? null : (SysUser) session.getAttribute(SESSION_USER);
        if (user == null) {
            return Result.fail(401, "未登录");
        }
        return Result.success(toVO(user));
    }

    /** 只把安全字段返回给前端，密码永不外传 */
    private Map<String, Object> toVO(SysUser user) {
        Map<String, Object> vo = new HashMap<>();
        vo.put("userId", user.getUserId());
        vo.put("username", user.getUsername());
        vo.put("nickname", user.getNickname());
        vo.put("role", user.getRole());
        return vo;
    }

    /** 注册请求体 */
    static class RegisterRequest {
        private String username;
        private String password;
        private String nickname;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }
    }

    /** 登录请求体 */
    static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
