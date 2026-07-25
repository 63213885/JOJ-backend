-- 创建库
create database if not exists joj;

-- 切换库
use joj;

create table if not exists submission
(
    id          bigint unsigned not null auto_increment comment '提交ID',

    user_id     bigint unsigned not null comment '用户ID',
    problem_id  bigint unsigned not null comment '题目ID',
    contest_id  bigint unsigned null comment '比赛ID，非比赛提交为空',

    language    varchar(32)     not null comment '编程语言：cpp/java/python3/go',
    code        text            not null comment '代码',

    status      varchar(32)     not null comment '判题结果Pending/Running/Accepted/Wrong Answer/Time Limit Exceeded/Memory Limit Exceeded/Runtime Error等',

    time_used   int unsigned    null comment '耗时ms',
    memory_used int unsigned    null comment '内存KB',
    score       int             not null default 0 comment '得分',

    submit_time datetime        not null default current_timestamp comment '提交时间',
    update_time datetime        not null default current_timestamp on update current_timestamp,
    is_delete   tinyint         not null default 0,

    primary key (id),
    key idx_user_submit_time (user_id, submit_time),
    key idx_user_problem_submit_time (user_id, problem_id, submit_time),
    key idx_problem_submit_time (problem_id, submit_time),
    key idx_contest_submit_time (contest_id, submit_time)
) engine = InnoDB
  default charset = utf8mb4 comment ='提交记录表';