-- 创建库
create database if not exists joj;

-- 切换库
use joj;

# create table if not exists user_problem_status
# (
#     user_id              bigint unsigned not null comment '用户ID',
#     problem_id           bigint unsigned not null comment '题目ID',
#
#     submit_count         int unsigned not null default 0 comment '该题提交次数',
#     is_solved            tinyint      not null default 0 comment '是否解决',
#     first_ac_time        datetime     null comment '首次通过时间',
#     last_submit_time     datetime     null comment '最后提交时间',
#
#     primary key (user_id, problem_id),
#     key idx_problem_id (problem_id)
# ) engine=InnoDB default charset=utf8mb4 comment='用户题目状态表';
#
# create table if not exists contests
# (
#     id                   bigint unsigned not null auto_increment comment '比赛ID',
#
#     title                varchar(256)   not null comment '比赛名称',
#     description          text           null comment '比赛描述',
#
#     start_time           datetime       not null comment '开始时间',
#     end_time             datetime       not null comment '结束时间',
#
#     rule_type            varchar(32)    not null default 'acm' comment '规则：acm/oi',
#     status               tinyint        not null default 0 comment '状态：0未开始 1进行中 2已结束',
#
#     creator_id           bigint unsigned null comment '创建人ID',
#
#     problem_ids          json           not null comment '题目ID列表，第一阶段先这样存',
#     participant_count    int unsigned   not null default 0 comment '参与人数',
#
#     create_time          datetime       not null default current_timestamp,
#     update_time          datetime       not null default current_timestamp on update current_timestamp,
#     is_deleted           tinyint        not null default 0,
#
#     primary key (id),
#     key idx_start_time (start_time),
#     key idx_end_time (end_time),
#     key idx_status (status)
# ) engine=InnoDB default charset=utf8mb4 comment='比赛表';
#
# create table if not exists contest_user
# (
#     id                 bigint unsigned not null auto_increment,
#     contest_id         bigint unsigned not null comment '比赛ID',
#     user_id            bigint unsigned not null comment '用户ID',
#
#     status             tinyint not null default 0 comment '状态：0已报名 1已退赛',
#     create_time        datetime not null default current_timestamp,
#
#     primary key (id),
#     unique key uk_contest_user (contest_id, user_id),
#     key idx_user_id (user_id)
# ) engine=InnoDB default charset=utf8mb4 comment='比赛报名表';
#
#
# create table if not exists course_orders
# (
#     id                   bigint unsigned not null auto_increment comment '订单ID',
#
#     order_no             varchar(64)    not null comment '订单号',
#     user_id              bigint unsigned not null comment '用户ID',
#     course_id            bigint unsigned not null comment '课程ID',
#
#     amount               decimal(10,2)  not null comment '支付金额',
#     status               tinyint        not null default 0 comment '状态：0待支付 1已支付 2已取消 3已退款',
#
#     pay_time             datetime       null comment '支付时间',
#
#     create_time          datetime       not null default current_timestamp,
#     update_time          datetime       not null default current_timestamp on update current_timestamp,
#
#     primary key (id),
#     unique key uk_order_no (order_no),
#     key idx_user_id (user_id),
#     key idx_course_id (course_id),
#     key idx_status (status)
# ) engine=InnoDB default charset=utf8mb4 comment='课程订单表';
#
# create table if not exists user_course
# (
#     id                   bigint unsigned not null auto_increment,
#     user_id              bigint unsigned not null comment '用户ID',
#     course_id            bigint unsigned not null comment '课程ID',
#     order_id             bigint unsigned not null comment '订单ID',
#
#     expire_time          datetime       null comment '过期时间，为空表示永久有效',
#     status               tinyint        not null default 1 comment '状态：1有效 0失效',
#
#     create_time          datetime       not null default current_timestamp,
#
#     primary key (id),
#     unique key uk_user_course (user_id, course_id),
#     key idx_course_id (course_id)
# ) engine=InnoDB default charset=utf8mb4 comment='用户课程表';
#
# create table if not exists chat_message
# (
#     id                   bigint unsigned not null auto_increment comment '消息ID',
#
#     from_user_id         bigint unsigned not null comment '发送者ID',
#     to_user_id           bigint unsigned not null comment '接收者ID',
#
#     content_type         varchar(32)    not null default 'text' comment '类型：text/image/system',
#     content              text           not null comment '消息内容',
#
#     is_read              tinyint        not null default 0 comment '是否已读',
#     create_time          datetime       not null default current_timestamp,
#     is_deleted           tinyint        not null default 0,
#
#     primary key (id),
#     key idx_from_user_id (from_user_id),
#     key idx_to_user_id (to_user_id),
#     key idx_create_time (create_time)
# ) engine=InnoDB default charset=utf8mb4 comment='聊天消息表';
#
# create table if not exists pk_match
# (
#     id                   bigint unsigned not null auto_increment comment 'PK对局ID',
#
#     user_a_id            bigint unsigned not null comment '用户A',
#     user_b_id            bigint unsigned not null comment '用户B',
#
#     problem_id           bigint unsigned not null comment '题目ID',
#
#     winner_user_id       bigint unsigned null comment '获胜者ID',
#     status               tinyint        not null default 0 comment '状态：0匹配中 1进行中 2已结束 3取消',
#
#     start_time           datetime       null comment '开始时间',
#     end_time             datetime       null comment '结束时间',
#
#     create_time          datetime       not null default current_timestamp,
#
#     primary key (id),
#     key idx_user_a_id (user_a_id),
#     key idx_user_b_id (user_b_id),
#     key idx_problem_id (problem_id),
#     key idx_status (status)
# ) engine=InnoDB default charset=utf8mb4 comment='PK对局表';