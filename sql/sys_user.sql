-- =============================================================
-- SmartHedge_Risk_Hub —— 登录用户表
-- 数据库：smarthedge_risk_hub
-- 说明：执行本脚本即可启用登录功能；默认账号 admin / 123456
-- =============================================================

USE `smarthedge_risk_hub`;

CREATE TABLE IF NOT EXISTS `sys_user` (
  `user_id`     INT          NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username`    VARCHAR(50)  NOT NULL                COMMENT '登录账号',
  `password`    VARCHAR(100) NOT NULL                COMMENT '登录密码',
  `nickname`    VARCHAR(50)           DEFAULT NULL   COMMENT '昵称/姓名',
  `role`        VARCHAR(30)           DEFAULT 'admin' COMMENT '角色：admin=管理员，operator=业务员',
  `status`      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态：1=启用，0=禁用',
  `create_time` DATETIME              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_sys_user_username` (`username`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '系统登录用户表';

-- 初始账号：admin / 123456（重复执行不会重复插入）
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `role`, `status`)
SELECT 'admin', '123456', '系统管理员', 'admin', 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_user` WHERE `username` = 'admin');
