# 认证 API 文档

## 基本信息

- 基础路径: `/`
- 响应格式: JSON
- 认证方式: 
  - `/login`, `/register`, `/register/check-username` 无需认证。
  - 其他所有接口都需要JWT Token认证（通过请求头传递 JWT Token）。
  - 系统不再进行角色权限控制，但前端可根据用户角色引导用户使用适合其角色的功能。
- **警告**: 系统当前配置为存储和比较明文密码，存在严重安全风险。

## 接口列表

### 1. 用户注册

- **接口**: `/register`
- **方法**: `POST`
- **描述**: 注册新用户。服务器会直接存储用户提供的明文密码。
- **请求体**:
  ```json
  {
    "username": "string",     // 用户名，2-20个字符
    "password": "string",     // 明文密码，6-20个字符
    "age": "integer",        // 年龄，0-100
    "gender": "string",      // 性别
    "phone": "string",       // 手机号，11位数字
    "medicalRecord": "string" // 症状描述
  }
  ```
- **响应示例** (成功):
  ```json
  {
    "code": 200,
    "message": "注册成功",
    "data": null
  }
  ```
- **响应示例** (失败 - 参数错误/用户名已存在等):
  ```json
  // 具体错误信息见 message 字段
  {
    "code": 400, 
    "message": "注册失败，用户名可能已存在", // 或其他验证错误信息
    "data": null
  }
  ```

### 2. 用户登录

- **接口**: `/login`
- **方法**: `POST`
- **描述**: 用户或管理员登录。服务器会直接比较用户输入的明文密码和数据库中存储的明文密码。成功后返回用户信息、角色和认证 Token。
- **请求体**:
  ```json
  {
    "username": "string",
    "password": "string" // 用户输入的明文密码
  }
  ```
- **响应示例** (成功 - 普通用户):
  ```json
  {
    "code": 200,
    "message": "登录成功",
    "data": {
      "role": "user",
      "user": {
        "userId": 1,
        "username": "test",
        "age": 25,
        "gender": "男",
        "phone": "13800138000",
        "medicalRecord": "症状描述",
        "createTime": "2024-03-21T10:00:00",
        "updateTime": "2024-03-21T10:00:00",
        "token": "..."
        // eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0IiwiaWF0IjoxNzQ0MDUxMTM5LCJleHAiOjE3NDAwNTQ3Mzl9.QJgKY4jQXzQYyzQXzQYyzQXzQYyzQXzQYyzQXzQYyzQXzQ
      }
    }
  }
  ```
- **响应示例** (成功 - 管理员):
  ```json
  {
    "code": 200,
    "message": "登录成功",
    "data": {
      "role": "admin",
      "user": { // 注意：这里仍然使用 user 字段封装管理员信息
        "admin_id": 1, // Admin 实体的主键名
        "username": "adminuser",
        "password": null // 不返回密码哈希
        "token": "..."
        //eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTc0NDA1MTgyOCwiZXhwIjoxNzQ0MDU1NDI4fQ.PMSfBMmuP0yVEU8lDWV1Y079_DOu93L8JhYdwyY7Wps
      }
    }
  }
  ```
- **响应示例** (失败 - 密码错误或用户不存在):
  ```json
  {
    "code": 400, // 或 401 
    "message": "登录失败", // 更具体的错误信息可能由后端实现决定
    "data": null
  }
  ```

### 3. 检查用户名

- **接口**: `/register/check-username`
- **方法**: `GET`
- **描述**: 检查用户名是否已存在（在用户表和管理员表中同时检查）。
- **参数**:
  - `username`: 要检查的用户名
- **响应示例**:
  ```json
  // 用户名已存在
  {
    "code": 200,
    "message": "success",
    "data": true
  }

  // 用户名可用
  {
    "code": 200,
    "message": "success",
    "data": false
  }
  ```

### 4. 获取当前登录用户信息

- **路径**: `/users/info` (或 `/auth/info`)
- **方法**: `GET`
- **描述**: 获取当前认证用户的信息。
- **请求头**: 需要有效的 `Authorization` Token (例如 `Bearer <token>`)
- **成功响应 (200 OK)**:

```json
{
  "code": 200,
  "message": "查询成功",
  "data": { // <-- 返回 UserVO 结构
    "userId": 123,
    "username": "currentuser",
    "age": 30,
    "gender": "男", // <-- 显示性别标签
    "phone": "13912345678",
    "medicalRecord": "高血压病史"
    // "avatarUrl": "http://example.com/avatar.jpg" // 如果 UserVO 包含此字段
  }
}
```

- **失败响应**: (参考通用失败响应)
- **错误代码**: 401 (未认证或Token无效), 404 (用户数据异常), 500

### 5. 用户修改自己的信息

- **路径**: `/users/self` (或 `/auth/updateSelf`)
- **方法**: `PUT`
- **描述**: 当前登录用户修改自己的非敏感信息。
- **请求头**: 需要有效的 `Authorization` Token。
- **请求体 (application/json)**: (包含允许用户修改的字段)

```json
{
  "age": 31,
  "gender": "0", // <-- 传递性别编码 ('0'男, '1'女)
  "phone": "13987654321",
  "medicalRecord": "高血压病史，控制良好"
  // "avatarUrl": "http://example.com/new_avatar.jpg"
}
```
*注意: 只允许修改部分字段。`gender` 字段应传递有效的性别编码。*

- **成功响应 (200 OK)**:

```json
{
  "code": 200,
  "message": "信息更新成功",
  "data": null
}
```

- **失败响应**: (参考通用失败响应)
- **错误代码**: 400 (参数校验失败，无效编码), 401 (未认证), 500

### 6. 用户修改密码

- **接口**: `/users/password`
- **方法**: `PUT`
- **描述**: 当前登录用户修改自己的密码。
- **请求头**: 需要有效的 `Authorization` Token。
- **请求体**:
  ```json
  {
    "oldPassword": "string",
    "newPassword": "string"
  }
  ```
- **成功响应**:
  ```json
  {
    "code": 200,
    "message": "密码更新成功",
    "data": null
  }
  ```
- **失败响应**: (参考通用失败响应)
- **错误代码**: 400 (参数校验失败，旧密码错误), 401 (未认证), 500

## 错误码说明

- 200: 成功
- 400: 请求参数错误 / 业务校验失败 (如用户名已存在、密码错误)
- 401: 未授权 (访问受保护接口但未提供有效认证信息)
- 403: 禁止访问 (权限不足)
- 500: 服务器内部错误

## 注意事项

1.  **警告**: 系统目前直接存储和比较明文密码，存在严重安全风险。
2.  登录时直接比较明文密码。
3.  除 `/login`, `/register`, `/register/check-username` 外，所有其他接口都需要进行身份认证 (通过 JWT Token)。
4.  登录成功后，响应中包含角色信息和 JWT Token。

## 注意事项

1. 用户名长度必须在 2-20 个字符之间
2. 密码长度必须在 6-20 个字符之间
3. 年龄必须在 0-100 之间
4. 手机号必须是 11 位数字，以 1 开头
5. 创建时间和更新时间由系统自动生成

## 重要说明

- **认证**: 大部分接口需要有效的认证 Token。
- **参数校验**: 所有接口都会对传入参数进行校验。
- **密码安全**: 注册和修改密码时，前端应进行基本校验，后端必须进行加密存储。
- **性别 (Gender)**: 
    - 获取用户信息 (`/users/info`) 时，`gender` 字段返回的是**中文显示值** (例如 "男")。
    - 注册或更新用户信息时，`gender` 字段需要传递**性别编码** ("0" 表示男性, "1" 表示女性)。
- **权限控制**: 确保用户只能修改自己的信息，管理员接口有相应的权限校验。 