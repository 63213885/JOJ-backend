-- 创建库
create database if not exists joj;

-- 切换库
use joj;

create table if not exists media_file
(
    id                bigint unsigned  not null auto_increment comment '文件ID',

    original_filename varchar(255)     not null comment '原始文件名',
    content_type      varchar(128)     null comment 'MIME类型',

    md5               char(32)         not null comment '文件MD5值',
    file_size         bigint unsigned  not null default 0 comment '文件大小，单位字节',

    bucket_name       varchar(64)      not null comment 'MinIO桶名称',
    object_name       varchar(512)     not null comment 'MinIO对象路径',

    access_type       tinyint unsigned not null default 0 comment '访问类型：0私有 1公开',
    creator_id        bigint unsigned  null comment '上传人ID',

    transcode_status  tinyint unsigned not null default 0 comment '转码状态：0无需转码 1未转码 2转码中 3已转码 4转码失败',
    hls_prefix        varchar(512)     null comment 'HLS目录前缀',
    encrypt_type      tinyint unsigned not null default 0 comment '加密类型：0无 1HLS_AES_128_KEY混淆',
    encrypt_key       varbinary(16)    null comment 'HLS AES-128真实密钥',
    encrypt_iv        varchar(32)      null comment 'HLS AES-128 IV',

    create_time       datetime         not null default current_timestamp comment '创建时间',
    update_time       datetime         not null default current_timestamp on update current_timestamp comment '更新时间',
    is_deleted        tinyint unsigned not null default 0 comment '是否删除',

    primary key (id),

    unique key uk_bucket_object (bucket_name, object_name),
    key idx_md5_size (md5, file_size),
    key idx_uploader_id (creator_id),
    key idx_bucket_name (bucket_name)
) engine = InnoDB
  default charset = utf8mb4 comment = '文件表';