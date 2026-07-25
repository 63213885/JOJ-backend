-- 创建库
create database if not exists joj;

-- 切换库
use joj;

create table if not exists course
(
    id             bigint unsigned not null auto_increment comment '课程ID',
    creator_id     bigint unsigned not null comment '创建者ID',

    sort           int             not null default 0 comment '排序',

    title          varchar(256)    not null comment '课程标题',
    cover_url      varchar(512)    null comment '封面',
    description    text            null comment '课程描述',

    price          decimal(10, 2)  not null default 0.00 comment '价格',
    original_price decimal(10, 2)  not null default 0.00 comment '原价',
    sale_count     int unsigned    not null default 0 comment '销量',

    status         tinyint         not null default 0 comment '状态：0下架 1上架',

    create_time    datetime        not null default current_timestamp,
    update_time    datetime        not null default current_timestamp on update current_timestamp,
    is_deleted     tinyint         not null default 0,

    primary key (id),
    key idx_creator_id (creator_id),
    key idx_status_sort (status, sort),
    key idx_sort (sort)
) engine = InnoDB
  default charset = utf8mb4 comment ='课程表';
