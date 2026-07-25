-- 创建库
create database if not exists joj;

-- 切换库
use joj;

create table if not exists media_upload_part
(
    id          bigint unsigned not null auto_increment comment '分片ID',

    task_id     bigint unsigned not null comment '上传任务ID',
    upload_id   varchar(128)    not null comment 'MinIO分片上传ID',

    part_number int unsigned    not null comment '分片编号，从1开始',
    etag        varchar(255)    not null comment 'MinIO返回的ETag',
    part_size   bigint unsigned not null comment '分片大小，单位字节',

    create_time datetime        not null default current_timestamp comment '创建时间',
    update_time datetime        not null default current_timestamp on update current_timestamp comment '更新时间',

    primary key (id),
    unique key uk_task_part (task_id, part_number),
    key idx_upload_id (upload_id)
) engine = InnoDB
  default charset = utf8mb4 comment = '大文件上传分片表';