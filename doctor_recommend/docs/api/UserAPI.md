# 用户管理 API (User API) - 管理员视角

## 基本信息

- **基础路径**: `/users`
- **认证**: 所有接口需要JWT Token认证（`/users/me`和`/users/me/*`除外）。
- **通用成功响应格式**:
  ```json
  {
    "code": 200,
    "message": "操作成功信息",
    "data": { ... } // 具体数据内容见各接口说明
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
- **警告**: **所有返回 `User` 对象的接口都会包含完整的用户信息，包括密码哈希！在生产环境中这是不安全的。**

---

## 接口列表

### 1. 获取用户列表（分页）

- **路径**: `/users`
- **方法**: `GET`
- **描述**: 获取系统中的用户列表，支持分页和可选的用户名、手机号搜索。
- **认证**: 需要JWT Token认证。
- **请求参数**:
  - `page` (query, long, 可选, 默认 1): 当前页码。
  - `size` (query, long, 可选, 默认 10): 每页显示数量。
  - `username` (query, string, 可选): 根据用户名模糊搜索。
  - `phone` (query, string, 可选): 根据手机号模糊搜索。
- **成功响应 (200 OK)**:
  ```json
  {
    "code": 200,
    "message": "查询成功",
    "data": {
      "records": [ // 当前页的用户列表
        {
          "userId": 1,
          "username": "admin",
          "password": "{bcrypt}$2a$10$...", // 密码哈希
          "phone": "13800138000",
          "age": 30,
          "gender": "男",
          "medicalRecord": "无",
          "createTime": "2023-10-27T10:30:00",
          "updateTime": "2023-10-27T11:00:00"
          // ... 其他 User 实体中的字段
        }
        // ... more users
      ],
      "total": 1,    // 总记录数
      "size": 10,    // 每页数量
      "current": 1,  // 当前页码
      "pages": 1     // 总页数
    }
  }
  ```
- **失败响应**:
  - `401 Unauthorized`: 未认证或 Token 失效。
  - `500 Internal Server Error`: 服务器内部错误。

### 2. 根据ID获取用户信息

- **路径**: `/users/{userId}`
- **方法**: `GET`
- **描述**: 获取指定 ID 用户的所有信息。
- **认证**: 需要JWT Token认证。
- **路径参数**:
  - `userId` (path, long, 必填): 要获取信息的用户 ID。
- **成功响应 (200 OK)**:
  ```json
  {
    "code": 200,
    "message": "查询成功",
    "data": {
      "userId": 1,
      "username": "admin",
      "password": "{bcrypt}$2a$10$...", // 密码哈希
      "phone": "13800138000",
      "age": 30,
      "gender": "男",
      "medicalRecord": "无",
      "createTime": "2023-10-27T10:30:00",
      "updateTime": "2023-10-27T11:00:00"
      // ... 其他 User 实体中的字段
    }
  }
  ```
- **失败响应**:
  - `404 Not Found`: 用户不存在 (`"message": "用户不存在"` 或类似)。
  - `401 Unauthorized`, `500 Internal Server Error`。

### 3. 添加新用户

- **路径**: `/users`
- **方法**: `POST`
- **描述**: 添加一个新的用户。需要在请求体中提供用户名和**明文密码**，以及其他允许设置的字段。
- **认证**: 需要JWT Token认证。
- **请求体**: `application/json`
  ```json
  {
    "username": "newuser",
    "password": "plainPassword123", // **明文密码**
    "phone": "13912345678",
    "age": 25,
    "gender": "女"
    // ... 其他允许管理员在创建时设置的 User 字段
  }
  ```
- **成功响应 (201 Created)**:
  ```json
  {
    "code": 200, // HTTP 状态码是 201, 但 Result.code 仍可能是 200
    "message": "用户添加成功",
    "data": {
      "userId": 101, // 新创建用户的 ID
      "username": "newuser",
      "password": "{bcrypt}$2a$10$...", // 加密后的密码哈希
      "phone": "13912345678",
      "age": 25,
      "gender": "女",
      "medicalRecord": null, // 假设创建时未提供
      "createTime": "2023-10-28T09:00:00",
      "updateTime": "2023-10-28T09:00:00"
      // ... 其他 User 实体中的字段
    }
  }
  ```
- **失败响应**:
  - `400 Bad Request`:
    - 用户名已存在 (`"message": "用户名 'newuser' 已存在"` 或类似)。
    - 缺少必要字段（如用户名、密码）或字段格式错误 (`"message": "密码不能为空"` 或类似)。
  - `401 Unauthorized`, `500 Internal Server Error`。

### 4. 更新用户信息

- **路径**: `/users/{userId}`
- **方法**: `PUT`
- **描述**: 更新指定 ID 用户的信息。**Service 层逻辑会限制可更新的字段**（例如，通常只允许更新手机号、邮箱、角色等，不允许直接更新用户名或密码）。请求体应包含要修改的字段。
- **认证**: 需要JWT Token认证。
- **路径参数**:
  - `userId` (path, long, 必填): 要更新的用户 ID。
- **请求体**: `application/json` (包含要更新的字段)
  ```json
  {
    "phone": "13888888888"
    // "email": "new.email@example.com", // 如果允许更新 email
    // "role": "USER",                 // 如果允许更新 role
    // "age": 31,                      // 其他允许管理员更新的字段...
    // "username": "ignored",          // 会被 Service 忽略
    // "password": "ignored"           // 会被 Service 忽略
  }
  ```
- **成功响应 (200 OK)**:
  ```json
  {
    "code": 200,
    "message": "用户信息更新成功",
    "data": null
  }
  ```
  或者，如果未做任何更改：
  ```json
  {
    "code": 200, // 或其他表示无变化的 code，取决于 Controller 实现
    "message": "更新用户信息失败或未做更改", // 或类似
    "data": null
  }
  ```
- **失败响应**:
  - `404 Not Found`: 用户不存在 (`"message": "用户不存在"` 或 `"message": "更新用户信息失败或未做更改"`，取决于实现)。
  - `400 Bad Request`: 请求体中的字段格式错误（例如手机号格式不对） (`"message": "手机号格式不正确"` 或类似)。
  - `401 Unauthorized`, `500 Internal Server Error`。

### 5. 删除用户

- **路径**: `/users/{userId}`
- **方法**: `DELETE`
- **描述**: 删除指定 ID 的用户。
- **认证**: 需要JWT Token认证。
- **路径参数**:
  - `userId` (path, long, 必填): 要删除的用户 ID。
- **成功响应 (200 OK)**:
  ```json
  {
    "code": 200,
    "message": "用户删除成功",
    "data": null
  }
  ```
- **失败响应**:
  - `404 Not Found`: 用户不存在 (`"message": "删除用户失败，可能用户不存在"` 或类似)。
  - `401 Unauthorized`, `500 Internal Server Error`。

---

## 错误代码

- `200`: 操作成功。
- `201`: 资源创建成功 (用于 POST 添加用户)。
- `400`: Bad Request - 请求参数错误、格式无效、业务逻辑错误（如用户名已存在）。
- `401`: Unauthorized - 未认证或 Token 无效/过期。
- `403`: Forbidden - 已认证但无权限访问（如普通用户尝试访问管理员接口）。
- `404`: Not Found - 请求的资源不存在（如用户 ID 不存在）。
- `500`: Internal Server Error - 服务器内部处理错误。

---

## 重要说明

- **安全性警告**: 重申，当前 API 设计会暴露用户密码哈希，仅适用于内部测试或对安全要求极低的场景。
- **字段控制**: 更新操作 (`PUT /users/{userId}`) 的实际可更新字段由 `UserServiceImpl` 中的 `updateUserByAdmin` 方法逻辑决定，文档中的请求体示例仅供参考。
- **密码管理**: 用户密码的添加通过 `POST /users`（提供明文），修改密码应通过个人中心的接口 (`PUT /profile/me/password`)，管理员接口通常不直接提供修改密码功能。

## 注意事项

1.  **警告**: 系统目前直接存储和比较明文密码，存在严重安全风险。
2.  所有接口都需要管理员权限 (`ROLE_ADMIN`)。
3.  添加用户时，密码由后端统一设置为明文 "123456" 并存储。
4.  更新用户信息接口不允许修改用户名和密码。
5.  获取用户信息接口不返回密码字段。
6. 手机号字段需要满足11位数字的格式要求。
7. 删除用户是物理删除，请谨慎操作。
8. 根据实际需要，可能需要为这些接口添加权限控制（例如，只允许管理员删除用户）。 
8. 根据实际需要，可能需要为这些接口添加权限控制（例如，只允许管理员删除用户）。 

# 用户管理 API 文档

## 基础信息

- 基础路径: `/api/users`
- 响应格式: JSON
- 时间格式: `YYYY-MM-DD`

## 1. 用户查询接口

### 1.1 获取用户列表（分页查询）

- **接口**: `GET /api/users/list`
- **描述**: 获取用户列表，支持多条件筛选和分页
- **认证**: 需要 JWT Token 认证

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认为1 |
| size | Integer | 否 | 每页记录数，默认为10 |
| username | String | 否 | 用户名（支持模糊查询） |
| phone | String | 否 | 手机号（支持模糊查询） |
| minAge | Integer | 否 | 最小年龄 |
| maxAge | Integer | 否 | 最大年龄 |
| gender | String | 否 | 性别（男/女） |

#### 参数说明
- `minAge` 和 `maxAge` 必须为正整数，且 `minAge` 不能大于 `maxAge`
- `gender` 只能是 "男" 或 "女"
- 所有查询条件都是可选的，不传则不作为筛选条件
- 年龄范围：0-150岁

#### 响应示例

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "records": [
            {
                "userId": 1,
                "username": "张三",
                "gender": "男",
                "age": 30,
                "phone": "138****1234",
                "email": "zh****@example.com",
                "createdTime": "2024-01-01 10:00:00",
                "lastLoginTime": "2024-04-19 15:30:00",
                "status": 1
            }
        ],
        "total": 100,
        "size": 10,
        "current": 1,
        "pages": 10
    }
}
```

#### 响应字段说明

| 字段名 | 类型 | 说明 |
|--------|------|------|
| userId | Integer | 用户ID |
| username | String | 用户名 |
| gender | String | 性别（男/女） |
| age | Integer | 年龄 |
| phone | String | 手机号（脱敏） |
| email | String | 邮箱（脱敏） |
| createdTime | String | 创建时间 |
| lastLoginTime | String | 最后登录时间 |
| status | Integer | 用户状态（1:正常, 0:禁用） |

#### 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权 |
| 403 | 无权限访问 |
| 500 | 服务器内部错误 |

#### 错误响应示例

```json
{
    "code": 400,
    "message": "参数错误：最小年龄不能大于最大年龄",
    "data": null
}
```

#### 请求示例

1. 基础分页查询：
```
GET /api/users/list?page=1&size=10
```

2. 按用户名模糊查询：
```
GET /api/users/list?page=1&size=10&username=张
```

3. 按年龄范围查询：
```
GET /api/users/list?page=1&size=10&minAge=20&maxAge=30
```

4. 组合条件查询：
```
GET /api/users/list?page=1&size=10&username=张&gender=男&minAge=20&maxAge=30
```

#### 注意事项

1. 返回的用户信息中，手机号和邮箱会进行脱敏处理
2. 分页参数 `page` 从1开始计数
3. 查询结果按创建时间倒序排序
4. 年龄范围查询时，两个边界值都是包含的
5. 用户名和手机号的模糊查询支持部分匹配 