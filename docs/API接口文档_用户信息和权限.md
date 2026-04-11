# 项目 API 接口总览（v1）

## 公共约定

- 基础路径：`/api`（版本前缀：`/api/v1`）
- 内容类型：`application/json`（上传头像为 `multipart/form-data`）
- 编码：`UTF-8`
- 鉴权：公开接口无需 `Authorization`；受保护接口需 `Authorization: Bearer <access_token>`
- 错误：统一返回 JSON 错误结构（`code/message/path/timestamp`）

## 认证与账号

### 1. 发送验证码

- 方法：`POST`
- 路径：`/api/auth/send-code`
- 请求体：

```json
{
  "scene": "REGISTER|LOGIN|RESET_PASSWORD",
  "identifierType": "PHONE|EMAIL|USERNAME",
  "identifier": "13800138000"
}
```

- 响应：`object`
    - `identifier` `string` 标识值
    - `scene` `string` 验证码场景
    - `expireSeconds` `int` 过期秒数
- 示例：
  `curl -X POST "https://host/api/auth/send-code" -H "Content-Type: application/json" -d '{"scene":"REGISTER","identifierType":"PHONE","identifier":"13800138000"}'`

### 2. 注册

- 方法：`POST`
- 路径：`/api/auth/register`
- 请求体：

```json
{
  "identifierType": "PHONE",
  "identifier": "13800138000",
  "code": "123456",
  "password": "StrongP@ssw0rd",
  "nickname": "新用户"
}
```

- 响应：`object`
    - `id` `long` 用户ID
    - `username` `string` 用户名
    - `nickname` `string` 昵称
    - `createdAt` `string` 创建时间（ISO）
- 示例：
  `curl -X POST "https://host/api/auth/register" -H "Content-Type: application/json" -d '{"identifierType":"PHONE","identifier":"13800138000","code":"123456","password":"StrongP@ssw0rd","nickname":"新用户"}'`

### 3. 登录

- 方法：`POST`
- 路径：`/api/auth/login`
- 请求体：验证码登录或密码登录（二选一）

```json
{
  "identifierType": "USERNAME",
  "identifier": "alice",
  "password": "StrongP@ss"
}
```

- 响应：`object`
    - `accessToken` `string` 访问令牌
    - `refreshToken` `string` 刷新令牌
    - `tokenType` `string` 令牌类型（`Bearer`）
    - `expiresIn` `int` 访问令牌有效秒数
- 示例：
  `curl -X POST "https://host/api/auth/login" -H "Content-Type: application/json" -d '{"identifierType":"USERNAME","identifier":"alice","password":"StrongP@ss"}'`

### 4. 刷新 Token

- 方法：`POST`
- 路径：`/api/auth/token/refresh`
- 请求体：`{"refreshToken":"<token>"}`
- 响应：`object`
    - `accessToken` `string`
    - `refreshToken` `string`
    - `tokenType` `string`
    - `expiresIn` `int`
- 示例：
  `curl -X POST "https://host/api/auth/token/refresh" -H "Content-Type: application/json" -d '{"refreshToken":"<token>"}'`

### 5. 注销

- 方法：`POST`
- 路径：`/api/auth/logout`
- 鉴权：需要
- 请求体：通常包含 `refreshToken`
- 响应：`204 No Content`
- 示例：
  `curl -X POST "https://host/api/auth/logout" -H "Authorization: Bearer <access>" -H "Content-Type: application/json" -d '{"refreshToken":"<refresh>"}'`

### 6. 当前用户信息

- 方法：`GET`
- 路径：`/api/auth/me`
- 鉴权：需要
- 响应：`object`
    - `id` `long`
    - `username` `string`
    - `nickname` `string`
    - `roles` `string[]`
    - `createdAt` `string`
- 示例：`curl "https://host/api/auth/me" -H "Authorization: Bearer <access>"`

## 用户资料

### 1. 更新资料

- 方法：`PATCH`
- 路径：`/api/v1/profile`
- 鉴权：需要
- 请求体：可选字段（`nickname/bio/gender/birthday/zgId/school/tagJson`）
- 响应：`object`
    - `id` `long`
    - `nickname` `string`
    - `avatar` `string`
    - `bio` `string`
    - `zgId` `string`
    - `gender` `string`
    - `birthday` `string`
    - `school` `string`
    - `phone` `string`
    - `email` `string`
    - `tagJson` `string`
- 示例：
  `curl -X PATCH "https://host/api/v1/profile" -H "Authorization: Bearer <access>" -H "Content-Type: application/json" -d '{"nickname":"新的昵称"}'`

### 2. 上传头像

- 方法：`POST`
- 路径：`/api/v1/profile/avatar`
- 鉴权：需要
- 内容类型：`multipart/form-data`（字段名 `file`）
- 响应：`object`
    - `id` `long`
    - `nickname` `string`
    - `avatar` `string`
    - `bio` `string`
    - `zgId` `string`
    - `gender` `string`
    - `birthday` `string`
    - `school` `string`
    - `phone` `string`
    - `email` `string`
    - `tagJson` `string`
- 示例：`curl -X POST "https://host/api/v1/profile/avatar" -H "Authorization: Bearer <access>" -F file=@avatar.png`