-- 创建库
create database if not exists joj;

-- 切换库
use joj;

-- 用户表
create table if not exists user
(
    id              bigint unsigned                       not null auto_increment comment 'id',

    -- 登录 权限
    account         varchar(64)                           not null comment '账号',
    password_hash   varchar(255)                          not null comment '密码',
    phone           varchar(32)                           null comment '手机号',
    email           varchar(128)                          null comment '邮箱',

    role            varchar(32) default 'user'            not null comment '用户角色：user/admin',
    status          tinyint                               not null default 0 comment '状态：0正常 1封禁',

    -- 个人信息
    avatar_url      varchar(512)                          null comment '用户头像',
    bio             varchar(512)                          null comment '个人简介',
    school          varchar(128)                          null comment '学校名称',

    last_login_time datetime                              null comment '最后登录时间',
    last_login_ip   varchar(64)                           null comment '最后登录IP',

    create_time     datetime    default current_timestamp not null,
    update_time     datetime    default current_timestamp not null on update current_timestamp,
    is_delete       tinyint     default 0                 not null comment '是否删除',

    primary key (id),
    unique key uk_user_account (account),
    unique key uk_user_phone (phone),
    unique key uk_user_email (email),
    key idx_status (status)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci comment ='用户';