-- 创建库
create database if not exists joj;

-- 切换库
use joj;

create table if not exists problem
(
    id             bigint unsigned not null auto_increment comment '题目ID',

    title          varchar(256)    not null comment '标题',
    content        mediumtext      not null comment '题面',

    input_desc     text            null comment '输入描述',
    output_desc    text            null comment '输出描述',

    samples        json            null comment '样例',

    time_limit     int unsigned    not null default 1000 comment '时间限制ms',
    memory_limit   int unsigned    not null default 256 comment '内存限制MB',

    submit_count   int unsigned    not null default 0 comment '提交数',
    accepted_count int unsigned    not null default 0 comment '通过数',

    tags           json            null comment '标签，先简单逗号分隔',
    source         json            null comment '题目来源/出处',

    creator_id     bigint unsigned null comment '创建人ID',

    status         tinyint         not null default 0 comment '状态：0隐藏 1公开',
    create_time    datetime        not null default current_timestamp,
    update_time    datetime        not null default current_timestamp on update current_timestamp,
    is_delete      tinyint         not null default 0,

    primary key (id),
    key idx_creator_id (creator_id),
    key idx_status (status)
) engine = InnoDB
  default charset = utf8mb4 comment ='题目表';