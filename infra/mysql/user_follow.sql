-- 创建库
create database if not exists joj;

-- 切换库
use joj;

create table if not exists user_follow
(
    id           bigint unsigned not null auto_increment,
    from_user_id bigint unsigned not null comment '关注者ID',
    to_user_id   bigint unsigned not null comment '被关注者ID',

    rel_status   tinyint                  default 1 not null comment '关系是否存在',

    create_time  datetime        not null default current_timestamp,
    update_time  datetime        not null default current_timestamp on update current_timestamp,

    primary key (id),
    unique key uk_user_follow (from_user_id, to_user_id),
    key idx_from (from_user_id, to_user_id, rel_status),
    key idx_to (to_user_id, from_user_id, rel_status)
) engine = InnoDB
  default charset = utf8mb4 comment ='用户关注关系表';