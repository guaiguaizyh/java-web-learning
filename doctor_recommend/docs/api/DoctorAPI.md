# 医生管理 API 文档

## 基础信息

- 基础路径: `/doctors`
- 响应格式: JSON
- 时间格式: `YYYY-MM-DD`

## 1. 医生基础信息管理

### 1.1 获取医生列表
- **接口**: `GET /doctors`
- **描述**: 获取医生列表（分页）
- **认证**: 需要JWT Token认证
- **参数**:
  - `page`: 页码（查询参数，默认：1）
  - `size`: 每页大小（查询参数，默认：10）
  - `name`: 医生姓名（可选，模糊搜索）
  - `departmentId`: 科室ID（可选）
  - `positionsId`: 职称ID（可选）
  - `gender`: 性别（可选）
  - `sortField`: 排序字段（可选，值：doctorId, name, averageRating, ratingCount, workYears，默认：doctorId）
  - `sortOrder`: 排序方向（可选，值：asc, desc，默认：asc）
- **返回示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "doctorId": 1,
        "name": "张三",
        "gender": "男",
        "age": 35,
        "title": "主任医师",
        "departmentId": 1,
        "departmentName": "心内科",
        "avatarUrl": "http://example.com/avatar.jpg",
        "averageRating": 4.8,
        "ratingCount": 120,
        "expertiseList": [
          {
            "expertiseId": 1,
            "expertiseName": "心血管介入",
            "proficiency": 0.9
          },
          {
            "expertiseId": 2,
            "expertiseName": "心脏起搏器植入",
            "proficiency": 0.8
          }
        ]
      }
    ],
    "total": 100,
    "size": 10,
    "current": 1
  }
}
```

### 1.2 获取医生详情
- **接口**: `GET /doctors/{doctorId}`
- **描述**: 获取医生详细信息
- **认证**: 需要JWT Token认证
- **参数**:
  - `doctorId`: 医生ID（路径参数）
- **返回示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "doctorId": 1,
    "name": "张三",
    "gender": "男",
    "age": 35,
    "title": "主任医师",
    "departmentId": 1,
    "departmentName": "心内科",
    "avatarUrl": "http://example.com/avatar.jpg",
    "averageRating": 4.8,
    "ratingCount": 120,
    "expertiseList": [
      {
        "expertiseId": 1,
        "expertiseName": "心血管介入",
        "proficiency": 0.9
      },
      {
        "expertiseId": 2,
        "expertiseName": "心脏起搏器植入",
        "proficiency": 0.8
      }
    ]
  }
}
```

### 1.3 添加医生
- **接口**: `POST /doctors`
- **描述**: 添加新医生
- **认证**: 需要JWT Token认证（建议ADMIN角色使用）
- **请求体**:
```json
{
  "name": "张三",
  "gender": "男",
  "age": 35,
  "title": "主任医师",
  "departmentId": 1,
  "avatarUrl": "http://example.com/avatar.jpg",
  "expertiseList": [
    {
      "expertiseId": 1,
      "proficiency": 0.9
    },
    {
      "expertiseId": 2,
      "proficiency": 0.8
    }
  ]
}
```
- **返回示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "doctorId": 1
  }
}
```

### 1.4 更新医生信息
- **接口**: `PUT /doctors/{doctorId}`
- **描述**: 更新医生信息，支持部分字段更新。如果要取消医生的科室关联，可以将 departmentId 设置为 null
- **认证**: 需要JWT Token认证（建议ADMIN角色使用）
- **参数**:
  - `doctorId`: 医生ID（路径参数）
- **请求体**: 
```json
{
  "name": "张三",        // 可选，医生姓名
  "gender": "男",        // 可选，性别
  "age": 35,            // 可选，年龄
  "title": "主任医师",   // 可选，职称
  "departmentId": 1,    // 可选，科室ID。设置为 null 表示取消科室关联
  "avatarUrl": "http://example.com/avatar.jpg"  // 可选，头像URL
}
```
- **返回示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": true
}
```
- **特别说明**:
  1. 所有字段都是可选的，未提供的字段将保持原值不变
  2. 要取消医生的科室关联，只需将 departmentId 设置为 null：
  ```json
  {
    "departmentId": null
  }
  ```
  3. 取消科室关联不会影响医生的其他信息和专长关联

### 1.5 删除医生
- **接口**: `DELETE /doctors/{doctorId}`
- **描述**: 删除医生
- **认证**: 需要JWT Token认证（建议ADMIN角色使用）
- **参数**:
  - `doctorId`: 医生ID（路径参数）
- **返回示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": true
}
```

## 2. 医生科室关联管理

### 2.1 更新医生科室关联
- **接口**: `PUT /doctors/{doctorId}/department`
- **描述**: 更新医生的科室关联（设置或取消）
- **认证**: 需要JWT Token认证（建议ADMIN角色使用）
- **参数**:
  - `doctorId`: 医生ID（路径参数）
- **请求体**:
```json
{
  "departmentId": null  // 设置为null表示取消科室关联，或设置为具体的科室ID表示关联到新科室
}
```
- **返回示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": true
}
```
- **错误情况**:
  - 如果医生有未完成的评价或预约，不允许取消科室关联
  - 如果医生有专长关联，需要先取消专长关联
  - 如果新科室不存在，返回404错误

## 3. 医生评分管理

### 3.1 获取医生评分统计
- **接口**: `GET /doctors/{doctorId}/ratings`
- **描述**: 获取医生的评分统计信息
- **认证**: 需要JWT Token认证
- **参数**:
  - `doctorId`: 医生ID（路径参数）
- **返回示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "averageRating": 4.8,
    "ratingCount": 120,
    "ratingDistribution": {
      "5": 80,
      "4": 30,
      "3": 8,
      "2": 1,
      "1": 1
    }
  }
}
```

## 5. 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

## 6. 注意事项

1. 医生姓名不能为空
2. 医生必须属于一个有效的科室（除非显式取消科室关联）
3. 医生的专长必须属于其所在科室
4. 专长熟练度必须在0.0-1.0之间
5. 删除医生时会同时删除相关的评价和专长关联
6. 更新医生信息时，如果不提供某些字段，这些字段将保持不变
7. 取消医生科室关联前，需要先：
   - 确保没有未完成的评价或预约
   - 取消所有专长关联
   - 处理相关的医生排班

注意：医生专长相关接口已迁移至 ExpertiseAPI，请查阅 ExpertiseAPI.md 文档中的 "医生专长管理" 章节