-- 创建库
create database if not exists joj;

-- 切换库
use joj;

create table if not exists course_lesson
(
    id            bigint unsigned  not null auto_increment comment '课时ID',

    course_id     bigint unsigned  not null comment '课程ID',

    title         varchar(256)     not null comment '课时标题',
    description   varchar(1024)    null comment '课时介绍',

    video_file_id bigint unsigned  null comment '视频文件ID',
    duration      int unsigned     not null default 0 comment '视频时长，单位秒',

    problem_items json             null comment '配套题目列表',

    sort          int              not null default 0 comment '排序',
    status        tinyint unsigned not null default 0 comment '状态：0隐藏 1显示',

    create_time   datetime         not null default current_timestamp comment '创建时间',
    update_time   datetime         not null default current_timestamp on update current_timestamp comment '更新时间',
    is_deleted    tinyint unsigned not null default 0 comment '是否删除',

    primary key (id),
    key idx_course_id (course_id),
    key idx_course_sort (course_id, sort),
    key idx_video_file_id (video_file_id)
) engine = InnoDB
  default charset = utf8mb4 comment = '课程课时表';