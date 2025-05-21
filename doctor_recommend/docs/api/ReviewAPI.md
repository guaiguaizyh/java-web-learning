# 医生评论接口文档

## 1. 评论查询接口

### 1.1 获取指定医生的评论列表

**接口**：`GET /reviews/doctor/{doctorId}`

**描述**：获取指定医生的评论列表（分页）

**认证**：需要JWT Token

**参数**：
- `doctorId`：医生ID（路径参数）
- `page`：页码（默认：1）
- `size`：每页大小（默认：10）

**返回示例**：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "reviewId": 123,
        "doctorId": 456,
        "userId": 789,
        "rating": 4.5,
        "comment": "医生态度很好，诊断准确",
        "createdAt": "2023-10-15 14:30:00",
        "isAnonymous": true,
        "username": "匿名用户" // 如果是匿名评论，则显示"匿名用户"
      },
      // 更多评论...
    ],
    "total": 25,
    "size": 10,
    "current": 1,
    "pages": 3
  }
}
```

### 1.2 获取所有评论列表（管理员）

**接口**：`GET /reviews`

**描述**：获取所有评论的分页列表（管理员权限）

**认证**：需要管理员JWT Token

**参数**：
- `page`：页码（默认：1）
- `size`：每页大小（默认：10）

**返回示例**：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "reviewId": 123,
        "doctorId": 456,
        "userId": 789,
        "rating": 4.5,
        "comment": "医生态度很好，诊断准确",
        "createdAt": "2023-10-15 14:30:00",
        "isAnonymous": true,
        "username": "user123",
        "doctorName": "张医生",
        "departmentName": "内科"
      },
      // 更多评论...
    ],
    "total": 100,
    "size": 10,
    "current": 1,
    "pages": 10
  }
}
```

### 1.3 评论高级搜索

**接口**：`GET /reviews/search`

**描述**：根据多条件组合查询评论

**认证**：需要JWT Token

**参数**：
- `doctorId`：医生ID（可选）
- `userId`：用户ID（可选）
- `minRating`：最低评分（可选，范围1-5）
- `maxRating`：最高评分（可选，范围1-5）
- `keywords`：评论内容关键词（可选）
- `startDate`：开始日期（可选，格式：yyyy-MM-dd）
- `endDate`：结束日期（可选，格式：yyyy-MM-dd）
- `page`：页码（默认：1）
- `size`：每页大小（默认：10）

**返回示例**：与获取评论列表相同

### 1.4 评论管理模块 - 高级查询

**接口**：`GET /reviews/manage`

**描述**：管理员使用的评论高级查询功能

**认证**：需要管理员JWT Token

**参数**：
- `doctorName`：医生姓名，模糊查询（可选）
- `userName`：用户姓名，模糊查询（可选）
- `departmentId`：科室ID（可选）
- `minRating`：最低评分（可选，范围1-5）
- `maxRating`：最高评分（可选，范围1-5）
- `page`：页码（默认：1）
- `size`：每页大小（默认：10）

**返回示例**：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "reviewId": 123,
        "doctorId": 456,
        "userId": 789,
        "rating": 4.5,
        "comment": "医生态度很好，诊断准确",
        "createdAt": "2023-10-15 14:30:00",
        "isAnonymous": false,
        "username": "user123",
        "doctorName": "张医生",
        "departmentId": 10,
        "departmentName": "内科"
      },
      // 更多评论...
    ],
    "total": 50,
    "size": 10,
    "current": 1,
    "pages": 5
  }
}
```

## 2. 评论管理接口

### 2.1 添加医生评论

**接口**：`POST /reviews`

**描述**：添加一条医生评论

**认证**：需要JWT Token

**请求体**：
```json
{
  "doctorId": 456,
  "rating": 4.5,
  "comment": "医生态度很好，诊断准确",
  "isAnonymous": true
}
```

**参数说明**：
- `doctorId`：医生ID（必填）
- `rating`：评分，范围1-5（必填）
- `comment`：评论内容（必填）
- `isAnonymous`：是否匿名（必填）

**返回示例**：
```json
{
  "code": 200,
  "message": "评论提交成功",
  "data": null
}
```

### 2.2 删除评论

**接口**：`DELETE /reviews/{reviewId}`

**描述**：删除一条评论（用户只能删除自己的评论，管理员可以删除任何评论）

**认证**：需要JWT Token

**参数**：
- `reviewId`：评论ID（路径参数）

**返回示例**：
```json
{
  "code": 200,
  "message": "评论删除成功",
  "data": null
}
```

## 3. 错误码

| 错误码 | 说明 |
| ------ | ---- |
| 400 | 请求参数错误 |
| 401 | 未授权或Token无效 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

## 4. 注意事项

1. 所有需要认证的API请求头中必须包含有效的JWT Token：`Authorization: Bearer {token}`
2. 评分范围必须在1-5之间，可以是小数，精确到小数点后一位
3. 当用户删除自己的评论时，系统会自动更新对应医生的平均评分 