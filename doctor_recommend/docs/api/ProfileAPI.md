# 个人中心 API (Profile API)

## 基本信息

- **基础路径**: `/profile`
- **认证**: 所有接口需要JWT Token认证
- **通用成功响应格式**:
  ```json
  {
    "code": 200,
    "message": "操作成功信息",
    "data": { ... } // 具体数据内容见各接口说明，可能为 null
  }
  ```
- **通用失败响应格式**:
  ```json
  {
    "code": 非200状态码 (例如 400, 401, 403, 404, 500),
    "message": "错误信息描述",
    "data": null
  }
  ```
- **警告**: **返回 `User` 对象的接口会包含完整的用户信息，包括密码哈希！在生产环境中这是不安全的。**

## 接口列表

### 1. 获取当前登录用户信息

- **路径**: `/profile/me`
- **方法**: `GET`
- **描述**: 获取当前已认证用户的所有信息。
- **认证**: 需要JWT Token认证
- **成功响应 (200 OK)**:
  ```json
  {
    "code": 200,
    "message": "查询成功",
    "data": {
      "userId": 12,
      "username": "currentUser",
      "password": "{bcrypt}$2a$10$...", // 密码哈希
      "phone": "13712345678",
      "age": 28,
      "gender": "男",
      "medicalRecord": "近期健康",
      "createTime": "2023-11-01T14:00:00",
      "updateTime": "2024-01-15T09:30:00"
      // ... 其他 User 实体中的字段
    }
  }
  ```
- **失败响应**:
  - `401 Unauthorized`: 未认证或 Token 失效。
  - `404 Not Found`: 当前认证用户在数据库中找不到（理论上不应发生）。
  - `500 Internal Server Error`: 服务器内部错误。

### 2. 更新当前用户信息

- **路径**: `/profile/me`
- **方法**: `PUT`
- **描述**: 当前已认证用户更新自己的个人信息。**Service 层逻辑会限制可更新的字段**（例如，通常允许更新手机号、年龄、性别、病历等，不允许直接更新用户名、密码、角色）。请求体应包含要修改的字段。
- **认证**: 需要JWT Token认证
- **请求体**: `application/json` (包含要更新的字段)
  ```json
  {
    // "userId": 12, // 可选，会被忽略或校验
    "phone": "13787654321",
    "age": 29,
    "gender": "男",
    "medicalRecord": "更新了健康信息"
    // "username": "ignored", // 会被 Service 忽略
    // "password": "ignored"  // 会被 Service 忽略
  }
  ```
- **成功响应 (200 OK)**:
  ```json
  {
    "code": 200,
    "message": "个人信息更新成功",
    "data": null
  }
  ```
  或者，如果未做任何更改：
  ```json
  {
    "code": 200, // 或其他表示无变化的 code
    "message": "更新个人信息失败或未作更改", // 或类似
    "data": null
  }
  ```
- **失败响应**:
  - `400 Bad Request`: 请求体中的字段格式错误（例如手机号格式不对） (`"message": "手机号格式不正确"` 或类似)。
  - `401 Unauthorized`: 未认证或 Token 失效。
  - `403 Forbidden`: 尝试更新非本人的信息（虽然 Controller 层已阻止，但 Service 可能再次校验）。
  - `500 Internal Server Error`: 服务器内部错误。

### 3. 修改当前用户密码

- **路径**: `/profile/me/password`
- **方法**: `PUT`
- **描述**: 当前已认证用户修改自己的密码。
- **认证**: 需要JWT Token认证
- **请求体**: `application/json`
  ```json
  {
    "oldPassword": "currentPlainPassword", // 当前明文密码
    "newPassword": "newPlainPassword123"   // 新明文密码
  }
  ```
- **成功响应 (200 OK)**:
  ```json
  {
    "code": 200,
    "message": "密码修改成功",
    "data": null
  }
  ```
- **失败响应**:
  - `400 Bad Request`:
    - 旧密码不正确 (`"message": "旧密码不正确"`)。
    - 新旧密码不能为空 (`"message": "旧密码和新密码都不能为空"`)。
    - 新旧密码相同 (`"message": "新旧密码不能相同"`)。
    - 请求体缺少 `oldPassword` 或 `newPassword` (`"message": "请求体必须包含 oldPassword 和 newPassword"`)。
  - `401 Unauthorized`: 未认证或 Token 失效。
  - `500 Internal Server Error`: 服务器内部错误。

### 4. 注销当前用户账号

- **路径**: `/profile/me`
- **方法**: `DELETE`
- **描述**: 当前已认证用户删除自己的账号（物理删除）。
- **认证**: 需要JWT Token认证
- **成功响应 (200 OK)**:
  ```json
  {
    "code": 200,
    "message": "账号已成功注销",
    "data": null
  }
  ```
- **失败响应**:
  - `404 Not Found`: 用户不存在（可能已被删除） (`"message": "注销账号失败，可能用户已被删除"` 或类似)。
  - `401 Unauthorized`: 未认证或 Token 失效。
  - `500 Internal Server Error`: 服务器内部错误。

## 错误代码

- `200`: 操作成功。
- `400`: Bad Request - 请求参数错误、格式无效、业务逻辑错误（如旧密码错误）。
- `401`: Unauthorized - 未认证或 Token 无效/过期。
- `403`: Forbidden - （在此 Controller 中一般不直接返回，但 Service 层可能抛出 SecurityException）。
- `404`: Not Found - 请求的资源不存在（理论上在此 Controller 中不常见，除非用户在认证后被立即删除）。
- `500`: Internal Server Error - 服务器内部处理错误。

## 重要说明

- **安全性警告**: 获取用户信息接口 (`GET /profile/me`) 会暴露用户密码哈希。
- **字段控制**: 更新个人信息 (`PUT /profile/me`) 的实际可更新字段由 `UserServiceImpl` 中的 `updateUserSelf` 方法逻辑决定。
- **密码**: 修改密码接口需要提供当前密码和新密码的**明文**。
- **注销**: 删除账号是物理删除，操作不可逆。 