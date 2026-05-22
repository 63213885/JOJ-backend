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


create table if not exists user_follow (
    id                  bigint unsigned         not null auto_increment,
    from_user_id        bigint unsigned         not null                                comment '关注者ID',
    to_user_id          bigint unsigned         not null                                comment '被关注者ID',

    rel_status          tinyint                 default 1 not null                      comment '关系是否存在',

    create_time         datetime                not null default current_timestamp,
    update_time         datetime                not null default current_timestamp on update current_timestamp,

    primary key (id),
    unique key uk_user_follow (from_user_id, to_user_id),
    key idx_from (from_user_id, to_user_id, rel_status),
    key idx_to (to_user_id, from_user_id, rel_status)
) engine=InnoDB default charset=utf8mb4 comment='用户关注关系表';

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

create table if not exists submission
(
    id          bigint unsigned not null auto_increment comment '提交ID',

    user_id        bigint unsigned not null comment '用户ID',
    problem_id     bigint unsigned not null comment '题目ID',
    contest_id     bigint unsigned null comment '比赛ID，非比赛提交为空',

    language    varchar(32)     not null comment '编程语言：cpp/java/python3/go',
    code        text            not null comment '代码',

    status      varchar(32)     not null comment '判题结果Pending/Running/Accepted/Wrong Answer/Time Limit Exceeded/Memory Limit Exceeded/Runtime Error等',

    time_used      int unsigned    null comment '耗时ms',
    memory_used    int unsigned    null comment '内存KB',
    score          int             not null default 0 comment '得分',

    submit_time    datetime        not null default current_timestamp comment '提交时间',
    update_time datetime        not null default current_timestamp on update current_timestamp,
    is_delete   tinyint         not null default 0,

    primary key (id),
    key idx_user_submit_time (user_id, submit_time),
    key idx_user_problem_submit_time (user_id, problem_id, submit_time),
    key idx_problem_submit_time (problem_id, submit_time),
    key idx_contest_submit_time (contest_id, submit_time)
) engine = InnoDB
  default charset = utf8mb4 comment ='提交记录表';


create table if not exists media_file
(
    id                bigint unsigned not null auto_increment comment '文件ID',

    original_filename varchar(255)    not null comment '原始文件名',
    content_type      varchar(128)    null comment 'MIME类型',

    md5               char(32)        not null comment '文件MD5值',
    file_size         bigint unsigned not null default 0 comment '文件大小，单位字节',

    bucket_name       varchar(64)     not null comment 'MinIO桶名称',
    object_name       varchar(512)    not null comment 'MinIO对象路径',

    access_type       tinyint unsigned not null default 0 comment '访问类型：0私有 1公开',
    creator_id        bigint unsigned null comment '上传人ID',

    create_time       datetime        not null default current_timestamp comment '创建时间',
    update_time       datetime        not null default current_timestamp on update current_timestamp comment '更新时间',
    is_deleted        tinyint unsigned not null default 0 comment '是否删除',

    primary key (id),

    unique key uk_bucket_object (bucket_name, object_name),
    key idx_md5_size (md5, file_size),
    key idx_uploader_id (creator_id),
    key idx_bucket_name (bucket_name)
) engine = InnoDB default charset = utf8mb4 comment = '文件表';


create table if not exists courses
(
    id                   bigint unsigned not null auto_increment comment '课程ID',
    creator_id           bigint unsigned not null comment '创建者ID',

    sort                 int             not null default 0 comment '排序',

    title                varchar(256)   not null comment '课程标题',
    cover_url            varchar(512)   null comment '封面',
    description          text           null comment '课程描述',

    price                decimal(10,2)  not null default 0.00 comment '价格',
    original_price       decimal(10,2)  not null default 0.00 comment '原价',
    sale_count           int unsigned   not null default 0 comment '销量',

    status               tinyint        not null default 0 comment '状态：0下架 1上架',

    create_time          datetime       not null default current_timestamp,
    update_time          datetime       not null default current_timestamp on update current_timestamp,
    is_delete            tinyint        not null default 0,

    primary key (id),
    key idx_status (status),
    key idx_sort (sort)
) engine=InnoDB default charset=utf8mb4 comment='课程表';


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