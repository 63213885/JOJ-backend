-- 创建库
create database if not exists joj;

-- 切换库
use joj;

create table if not exists user_stats
(
    user_id         bigint unsigned not null comment '用户ID',

    submit_count    int unsigned    not null default 0 comment '提交总数',
    accepted_count  int unsigned    not null default 0 comment '通过总数',
    solved_count    int unsigned    not null default 0 comment '解决题目数',

    follower_count  int unsigned    not null default 0 comment '粉丝数',
    following_count int unsigned    not null default 0 comment '关注数',

    rating          int unsigned    not null default 1500 comment '当前Rating',

    course_count    int unsigned    not null default 0 comment '已购课程数',

    pk_count        int unsigned    not null default 0 comment 'PK总场次',
    pk_win_count    int unsigned    not null default 0 comment 'PK胜场',

    contest_count   int unsigned    not null default 0 comment '参赛次数',

    create_time     datetime        not null default current_timestamp,
    update_time     datetime        not null default current_timestamp on update current_timestamp,

    primary key (user_id)
) engine = InnoDB
  default charset = utf8mb4 comment ='用户统计表';
