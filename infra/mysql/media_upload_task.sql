-- 创建库
create database if not exists joj;

-- 切换库
use joj;

create table if not exists media_upload_task
(
    id                bigint unsigned  not null auto_increment comment '上传任务ID',

    upload_id         varchar(128)     not null comment 'MinIO分片上传ID',
    bucket_name       varchar(64)      not null comment 'MinIO桶名称',
    object_name       varchar(512)     not null comment 'MinIO对象路径',

    original_filename varchar(255)     not null comment '原始文件名',
    content_type      varchar(128)     null comment 'MIME类型',

    md5               char(32)         not null comment '文件MD5值，由前端计算',
    file_size         bigint unsigned  not null comment '文件大小，单位字节',

    chunk_size        bigint unsigned  not null comment '分片大小，单位字节',
    chunk_count       int unsigned     not null comment '分片总数',

    access_type       tinyint unsigned not null default 0 comment '访问类型：0私有 1公开',
    creator_id        bigint unsigned  null comment '上传人ID',

    status            tinyint unsigned not null default 0 comment '状态：0上传中 1已完成 2已取消 3失败',

    media_file_id     bigint unsigned  null comment '完成后生成的文件ID',

    create_time       datetime         not null default current_timestamp comment '创建时间',
    update_time       datetime         not null default current_timestamp on update current_timestamp comment '更新时间',
    is_deleted        tinyint unsigned not null default 0 comment '是否删除',

    primary key (id),

    unique key uk_upload_id (upload_id),
    unique key uk_bucket_object (bucket_name, object_name),

    key idx_md5_size (md5, file_size),
    key idx_creator_id (creator_id),
    key idx_status (status)
) engine = InnoDB
  default charset = utf8mb4 comment = '大文件上传任务表';