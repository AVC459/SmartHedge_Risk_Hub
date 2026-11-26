package com.ape.smarthedge_risk_hub.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 系统登录用户实体类（对应表 sys_user）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_user")
public class SysUser {
    @TableId(value = "user_id", type = IdType.AUTO)
    private Integer userId; // 用户ID
    @TableField("username")
    private String username; // 登录账号
    @JsonIgnore
    @TableField("password")
    private String password; // 登录密码（不序列化给前端）
    @TableField("nickname")
    private String nickname; // 昵称/姓名
    @TableField("role")
    private String role; // 角色：admin / operator
    @TableField("status")
    private Integer status; // 状态：1=启用，0=禁用
    @TableField("create_time")
    private LocalDateTime createTime; // 创建时间
    @TableField("update_time")
    private LocalDateTime updateTime; // 更新时间
}
