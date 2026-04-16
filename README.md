# JOJ-backend
一个OJ

## Auth

### 发送验证码

`POST` `/auth/send-code`

### 注册

`POST` `/auth/register`

### 登录

`POST` `/auth/login`

### 退出登录

`POST` `/auth/logout`

### 获取当前登录用户信息

`GET` `/auth/me`

### 重置密码

`POST` `/auth/password/reset`

## Profile

### 查看用户主页

`GET` `/profile/{account}`

### 个人信息

`GET` `/profile/info`

### 更新个人信息

`PUT` `/profile/info`

### 更新头像

`PATCH` `/profile/avatar`
