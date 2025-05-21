# 用户个人中心 API (User Profile API)

## 基本信息

- **基础路径**: `/users`
- **认证**: 所有接口需要用户登录并携带有效的 JWT Token
- **通用响应格式**:
  ```json
  {
    "code": 200,          // 状态码：200成功，其他表示失败
    "message": "string",  // 响应消息
    "data": object        // 响应数据，可能为null
  }
  ```

## 接口列表

### 1. 获取个人信息

- **路径**: `/users/me`
- **方法**: `GET`
- **描述**: 获取当前登录用户的个人信息
- **认证**: 需要JWT Token认证
- **请求头**:
  ```
  Authorization: Bearer {token}
  ```
- **成功响应 (200 OK)**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "userId": 1,
      "username": "zhangsan",
      "age": 30,
      "gender": "男",
      "phone": "13800138000",
      "medicalRecord": "无特殊病史",
      "avatarUrl": "http://example.com/avatar.jpg"
    }
  }
  ```
- **失败响应**:
  - `401 Unauthorized`: Token无效或已过期
  - `500 Internal Server Error`: 服务器内部错误

### 2. 更新个人信息

- **路径**: `/users/me`
- **方法**: `PUT`
- **描述**: 更新当前登录用户的个人信息
- **认证**: 需要JWT Token认证
- **请求头**:
  ```
  Authorization: Bearer {token}
  ```
- **请求参数**:
  ```json
  {
    "age": 31,                    // 选填，0-150
    "gender": "男",               // 选填
    "phone": "13800138001",      // 选填，11位手机号
    "medicalRecord": "高血压史"   // 选填
  }
  ```
- **成功响应 (200 OK)**:
  ```json
  {
    "code": 200,
    "message": "更新成功",
    "data": null
  }
  ```
- **失败响应**:
  - `400 Bad Request`: 参数验证失败
    ```json
    {
      "code": 400,
      "message": "手机号格式不正确",
      "data": null
    }
    ```
  - `401 Unauthorized`: Token无效或已过期
  - `500 Internal Server Error`: 服务器内部错误

### 3. 修改密码

- **路径**: `/users/password`
- **方法**: `PUT`
- **描述**: 修改当前登录用户的密码
- **认证**: 需要JWT Token认证
- **请求头**:
  ```
  Authorization: Bearer {token}
  ```
- **请求参数**:
  ```json
  {
    "oldPassword": "string",  // 必填，原密码
    "newPassword": "string"   // 必填，新密码（6-20字符）
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
    ```json
    {
      "code": 400,
      "message": "原密码错误",  // 或其他验证错误信息
      "data": null
    }
    ```
  - `401 Unauthorized`: Token无效或已过期
  - `500 Internal Server Error`: 服务器内部错误

## 字段说明

### UserVO（用户视图对象）

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |
| username | String | 是 | 用户名（2-20字符） |
| age | Integer | 否 | 年龄（0-150） |
| gender | String | 否 | 性别 |
| phone | String | 否 | 手机号（11位） |
| medicalRecord | String | 否 | 病历信息 |
| avatarUrl | String | 否 | 头像URL |

## 数据验证规则

1. **手机号**:
   - 格式：11位数字
   - 正则表达式：`^1[3-9]\\d{9}$`

2. **年龄**:
   - 范围：0-150
   - 类型：整数

3. **密码**:
   - 长度：6-20个字符
   - 不能与原密码相同（修改密码时）

## 注意事项

1. 所有接口都需要在请求头中携带有效的 JWT Token
2. 用户只能查看和修改自己的信息
3. 某些字段（如用户名）在创建后不能修改
4. 密码在传输和存储时都经过加密处理
5. 返回的用户信息中不包含敏感信息（如密码）
6. 所有时间相关的字段使用 ISO-8601 格式 