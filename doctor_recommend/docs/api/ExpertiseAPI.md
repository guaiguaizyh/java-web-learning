# 专长管理 API 文档

## 基础信息

- 基础路径: `/expertises`
- 响应格式: JSON
- 时间格式: `YYYY-MM-DD`

## 1. 专长基础信息管理

### 1.1 获取专长列表
- **接口**: `GET /expertises`
- **描述**: 获取专长列表（分页）
- **认证**: 需要JWT Token认证
- **参数**:
  - `page`: 页码（查询参数，默认：1）
  - `size`: 每页大小（查询参数，默认：10）
  - `expertiseName`: 专长名称（可选，模糊搜索）
  - `departmentId`: 科室ID（可选）
  - `symptomKeyword`: 症状关键词（可选，模糊搜索）
  - `sortBy`: 排序方式（可选，默认：DEFAULT）
    - `DEFAULT`: 按创建时间降序
    - `PROFICIENCY`: 按医生平均熟练度降序
    - `RELEVANCE`: 按症状相关性降序（需要配合symptomKeyword使用）
- **返回示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "expertiseId": 1,
        "expertiseName": "心血管介入",
        "departmentId": 10,
        "departmentName": "心内科",
        "avgProficiency": 0.85,    // 当sortBy=PROFICIENCY时返回
        "relevanceScore": 0.95     // 当sortBy=RELEVANCE且提供symptomKeyword时返回
      }
    ],
    "total": 100,
    "size": 10,
    "current": 1
  }
}
```

### 1.2 获取专长详情
- **接口**: `GET /expertises/{expertiseId}`
- **描述**: 获取专长详细信息
- **认证**: 需要JWT Token认证
- **参数**:
  - `expertiseId`: 专长ID（路径参数）
- **返回示例**:
```json
{
  "code": 200,
    "msg": "success",
  "data": {
    "expertiseId": 1,
    "expertiseName": "心血管介入",
    "departmentId": 10,
    "departmentName": "心内科" 
  }
}
```

### 1.3 添加专长
- **接口**: `POST /expertises`
- **描述**: 添加新专长
- **认证**: 需要JWT Token认证（建议ADMIN角色使用）
- **请求体**:
  ```json
  {
    "expertiseName": "心血管介入",
    "departmentId": 10
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

### 1.4 更新专长信息
- **接口**: `PUT /expertises/{expertiseId}`
- **描述**: 更新专长信息
- **认证**: 需要JWT Token认证（建议ADMIN角色使用）
- **参数**:
  - `expertiseId`: 专长ID（路径参数）
- **请求体**: 同1.3
- **返回示例**:
```json
{
  "code": 200,
    "msg": "success",
    "data": true
}
```

### 1.5 删除专长
- **接口**: `DELETE /expertises/{expertiseId}`
- **描述**: 删除专长
- **认证**: 需要JWT Token认证（建议ADMIN角色使用）
- **参数**:
  - `expertiseId`: 专长ID（路径参数）
- **返回示例**:
```json
{
  "code": 200,
    "msg": "success",
    "data": true
}
```

## 2. 医生专长管理

### 2.1 获取医生的专长列表
- **接口**: `GET /expertises/doctor/{doctorId}`
- **描述**: 获取指定医生的专长列表（包含熟练度）
- **认证**: 需要JWT Token认证
- **参数**:
  - `doctorId`: 医生ID（路径参数）
- **返回示例**:
```json
{
  "code": 200,
    "msg": "success",
  "data": [
    {
      "expertiseId": 1,
      "expertiseName": "心血管介入",
      "departmentId": 10,
      "departmentName": "心内科",
      "proficiency": 0.8
    }
  ]
}
```

### 2.2 更新医生的专长
- **接口**: `PUT /expertises/doctor/{doctorId}`
- **描述**: 更新医生的专长列表和熟练度
- **认证**: 需要JWT Token认证
- **参数**:
  - `doctorId`: 医生ID（路径参数）
- **请求体**:
```json
[
  {
    "expertiseId": 1,
    "proficiency": 0.8
  },
  {
    "expertiseId": 2,
    "proficiency": 0.6
  }
]
```
- **请求体说明**:
  - `expertiseId`: 专长ID
  - `proficiency`: 熟练度（0.0-1.0之间的小数）
- **返回示例**:
```json
{
  "code": 200,
    "msg": "success",
    "data": true
}
```
- **错误情况**:
  - 熟练度不在0-1范围内
  - 专长不存在
  - 专长不属于医生所在科室
  - 医生不存在

### 2.3 获取科室的专长列表
- **接口**: `GET /expertises/department/{departmentId}`
- **描述**: 获取指定科室的所有专长列表
- **认证**: 需要JWT Token认证
- **参数**:
  - `departmentId`: 科室ID（路径参数）
- **返回示例**:
```json
{
  "code": 200,
    "msg": "success",
  "data": [
    {
      "expertiseId": 1,
      "expertiseName": "心血管介入",
      "departmentId": 10,
      "departmentName": "心内科"
    }
  ]
}
```

### 2.4 获取拥有特定专长的医生列表
- **接口**: `GET /expertises/{expertiseId}/doctors`
- **描述**: 获取拥有指定专长的医生列表（分页）
- **认证**: 需要JWT Token认证
- **参数**:
  - `expertiseId`: 专长ID（路径参数）
  - `page`: 页码（查询参数，默认：1）
  - `size`: 每页大小（查询参数，默认：10）
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
        "positionsId": 1,
        "positionsName": "主任医师",
        "departmentId": 10,
        "departmentName": "心内科",
        "avatarUrl": "http://example.com/avatar.jpg",
        "averageRating": 4.8,
        "ratingCount": 120,
        "workYears": 15,
        "expertiseIds": "1,2,3",
        "expertiseNames": "心血管介入,心脏起搏器植入,冠心病",
        "doctorExpertises": [
          {
            "doctorId": 1,
            "expertiseId": 1,
            "expertiseName": "心血管介入",
            "proficiency": 0.9
          },
          {
            "doctorId": 1,
            "expertiseId": 2,
            "expertiseName": "心脏起搏器植入",
            "proficiency": 0.8
          },
          {
            "doctorId": 1,
            "expertiseId": 3,
            "expertiseName": "冠心病",
            "proficiency": 0.7
          }
        ]
      }
    ],
    "total": 5,
    "size": 10,
    "current": 1
  }
}
```

## 3. 专长症状关联管理

### 3.1 获取专长关联的症状列表
- **接口**: `GET /expertises/{expertiseId}/symptoms`
- **描述**: 获取专长关联的症状列表
- **认证**: 需要JWT Token认证
- **参数**:
  - `expertiseId`: 专长ID（路径参数）
- **返回示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "symptomId": 1,
      "keyword": "胸痛",
      "relevanceScore": 0.9
    }
  ]
}
```

### 3.2 更新专长关联的症状
- **接口**: `POST /expertises/{expertiseId}/symptoms`
- **描述**: 更新专长关联的症状及其相关度
- **认证**: 需要JWT Token认证（建议ADMIN角色使用）
- **参数**:
  - `expertiseId`: 专长ID（路径参数）
- **请求体**:
```json
{
  "symptomIds": [1, 2, 3],
  "relevanceScores": [0.9, 0.8, 0.7]
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

## 4. 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

## 5. 注意事项

1. 专长名称在同一科室下必须唯一
2. 医生只能选择其所在科室的专长
3. 删除专长时需要确保没有医生关联该专长
4. 添加和更新专长时，必须指定有效的科室ID
5. 更新医生专长时，所有专长必须属于医生所在科室 