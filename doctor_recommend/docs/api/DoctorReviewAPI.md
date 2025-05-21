# 医生评价接口文档

## 1. 评价管理接口

### 1.1 创建评价
- **接口**: `POST /reviews`
- **描述**: 创建新的医生评价
- **认证**: 需要JWT Token认证
- **请求体**:
```json
{
    "doctorId": 1,
    "rating": 5,
    "comment": "医生很专业，服务态度很好",
    "isAnonymous": false
}
```
- **返回示例**:
```json
{
    "code": 200,
    "msg": "success",
    "data": {
        "reviewId": 1
    }
}
```

### 1.2 获取评价详情
- **接口**: `GET /reviews/{reviewId}`
- **描述**: 获取指定评价的详细信息
- **认证**: 需要JWT Token认证
- **参数**:
  - `reviewId`: 评价ID（路径参数）
- **返回示例**:
```json
{
    "code": 200,
    "msg": "success",
    "data": {
        "reviewId": 1,
        "doctorId": 1,
        "userId": 1,
        "rating": 5,
        "comment": "医生很好",
        "createdAt": "2024-03-20 10:00:00",
        "isAnonymous": false,
        "username": "张三",
        "userAvatar": "avatar.jpg"
    }
}
```

### 1.3 获取医生评价列表
- **接口**: `GET /reviews/doctor/{doctorId}`
- **描述**: 获取指定医生的评价列表
- **认证**: 需要JWT Token认证
- **参数**:
  - `doctorId`: 医生ID（路径参数）
  - `page`: 页码（默认1）
  - `size`: 每页数量（默认10）
- **返回示例**:
```json
{
    "code": 200,
    "msg": "success",
    "data": {
        "records": [
            {
                "reviewId": 1,
                "doctorId": 1,
                "userId": 1,
                "rating": 5,
                "comment": "医生很好",
                "createdAt": "2024-03-20 10:00:00",
                "isAnonymous": false,
                "username": "张三",
                "userAvatar": "avatar.jpg"
            }
        ],
        "total": 1000,
        "size": 10,
        "current": 1
    }
}
```

### 1.4 删除评价
- **接口**: `DELETE /reviews/{reviewId}`
- **描述**: 删除指定评价
- **认证**: 需要JWT Token认证（建议ADMIN角色或评价作者使用）
- **参数**:
  - `reviewId`: 评价ID（路径参数）
- **返回示例**:
```json
{
    "code": 200,
    "msg": "删除成功",
    "data": null
}
```

### 1.5 更新评价
- **接口**: `PUT /reviews/{reviewId}`
- **描述**: 更新指定评价
- **认证**: 需要JWT Token认证（建议评价作者使用）
- **参数**:
  - `reviewId`: 评价ID（路径参数）
- **请求体**:
```json
{
    "rating": 4,
    "comment": "更新后的评价内容",
    "isAnonymous": false
}
```
- **返回示例**:
```json
{
    "code": 200,
    "msg": "更新成功",
    "data": null
}
```

## 2. 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 201 | 创建成功 |
| 400 | 请求参数错误 |
| 401 | 未授权 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

## 3. 注意事项

1. 评分范围为1-5分
2. 同一用户对同一医生只能评价一次
3. 评价可以匿名
4. 管理员可以查看和删除所有评价
5. 普通用户只能修改和删除自己的评价 