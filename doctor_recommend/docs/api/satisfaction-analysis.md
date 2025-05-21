# 满意度分析接口文档

## 基础信息

- 基础路径: `/satisfaction`
- 返回格式: 所有接口返回统一的 JSON 格式
```json
{
    "code": 200,      // 状态码：200成功，非200失败
    "msg": "success", // 状态信息
    "data": {}        // 返回数据
}
```

## 医生相关接口

### 获取医生评价统计

- **接口**: `GET /satisfaction/doctor/{doctorId}`
- **描述**: 获取指定医生的评价统计信息
- **参数**:
  - `doctorId`: 医生ID (路径参数)
- **返回示例**:
```json
{
    "code": 200,
    "msg": "success",
    "data": {
        "doctor_id": 1,
        "doctor_name": "张医生",
        "department_id": 1,
        "totalReviews": 100,
        "averageRating": 4.5,
        "highRatingCount": 85,
        "satisfactionRate": 85.0
    }
}
```

### 获取医生评价趋势

- **接口**: `GET /satisfaction/doctor/{doctorId}/trend`
- **描述**: 获取指定医生在特定时间段内的评价趋势
- **参数**:
  - `doctorId`: 医生ID (路径参数)
  - `startDate`: 开始日期，格式：YYYY-MM-DD
  - `endDate`: 结束日期，格式：YYYY-MM-DD
  - `periodType`: 统计周期类型 (day/week/month)
- **返回示例**:
```json
{
    "code": 200,
    "msg": "success",
    "data": [
        {
            "date": "2024-01",
            "reviewCount": 30,
            "averageRating": 4.5,
            "highRatingCount": 25,
            "satisfactionRate": 83.3
        }
    ]
}
```

## 科室相关接口

### 获取科室评价统计

- **接口**: `GET /satisfaction/department/{departmentId}`
- **描述**: 获取指定科室的评价统计信息
- **参数**:
  - `departmentId`: 科室ID (路径参数)
- **返回示例**:
```json
{
    "code": 200,
    "msg": "success",
    "data": {
        "department_id": 1,
        "department_name": "内科",
        "totalReviews": 500,
        "averageRating": 4.3,
        "highRatingCount": 400,
        "satisfactionRate": 80.0
    }
}
```

### 获取科室评分分布

- **接口**: `GET /satisfaction/department/{departmentId}/distribution`
- **描述**: 获取指定科室的评分分布情况
- **参数**:
  - `departmentId`: 科室ID (路径参数)
- **返回示例**:
```json
{
    "code": 200,
    "msg": "success",
    "data": [
        {
            "rating": 5,
            "count": 200,
            "percentage": 40.0
        },
        {
            "rating": 4,
            "count": 200,
            "percentage": 40.0
        }
    ]
}
```

### 获取科室评价趋势

- **接口**: `GET /satisfaction/department/{departmentId}/trend`
- **描述**: 获取指定科室在特定时间段内的评价趋势
- **参数**:
  - `departmentId`: 科室ID (路径参数)
  - `startDate`: 开始日期，格式：YYYY-MM-DD
  - `endDate`: 结束日期，格式：YYYY-MM-DD
  - `periodType`: 统计周期类型 (day/week/month)
- **返回示例**:
```json
{
    "code": 200,
    "msg": "success",
    "data": [
        {
            "date": "2024-01",
            "reviewCount": 150,
            "averageRating": 4.3,
            "highRatingCount": 120,
            "satisfactionRate": 80.0
        }
    ]
}
```

### 获取科室医生评分排名

- **接口**: `GET /satisfaction/department/{departmentId}/doctors`
- **描述**: 获取指定科室内医生的评分排名
- **参数**:
  - `departmentId`: 科室ID (路径参数)
- **返回示例**:
```json
{
    "code": 200,
    "msg": "success",
    "data": [
        {
            "doctor_id": 1,
            "doctor_name": "张医生",
            "reviewCount": 100,
            "averageRating": 4.8,
            "highRatingCount": 90,
            "satisfactionRate": 90.0
        }
    ]
}
```

## 全局统计接口

### 获取评分最高的医生

- **接口**: `GET /satisfaction/top-doctors`
- **描述**: 获取评分最高的医生列表
- **参数**:
  - `limit`: 返回数量，默认10 (可选)
- **返回示例**:
```json
{
    "code": 200,
    "msg": "success",
    "data": [
        {
            "doctor_id": 1,
            "doctor_name": "张医生",
            "department_id": 1,
            "department_name": "内科",
            "reviewCount": 100,
            "averageRating": 4.8,
            "highRatingCount": 90,
            "satisfactionRate": 90.0
        }
    ]
}
```

### 获取评分分布

- **接口**: `GET /satisfaction/rating/distribution`
- **描述**: 获取所有评分的分布情况
- **参数**: 无
- **返回示例**:
```json
{
    "code": 200,
    "msg": "success",
    "data": [
        {
            "rating": 5,
            "count": 1000,
            "percentage": 40.0
        }
    ]
}
```

### 获取评分趋势

- **接口**: `GET /satisfaction/rating/trend`
- **描述**: 获取特定时间段内的评分趋势
- **参数**:
  - `startDate`: 开始日期，格式：YYYY-MM-DD
  - `endDate`: 结束日期，格式：YYYY-MM-DD
  - `periodType`: 统计周期类型 (day/week/month)
- **返回示例**:
```json
{
    "code": 200,
    "msg": "success",
    "data": [
        {
            "date": "2024-01",
            "reviewCount": 300,
            "averageRating": 4.5,
            "highRatingCount": 250,
            "satisfactionRate": 83.3
        }
    ]
}
```

### 获取时间段评价统计

- **接口**: `GET /satisfaction/stats`
- **描述**: 获取特定时间段内的评价统计信息
- **参数**:
  - `startDate`: 开始日期，格式：YYYY-MM-DD
  - `endDate`: 结束日期，格式：YYYY-MM-DD
- **返回示例**:
```json
{
    "code": 200,
    "msg": "success",
    "data": {
        "totalReviews": 1000,
        "averageRating": 4.5,
        "highRatingCount": 800,
        "satisfactionRate": 80.0
    }
}
```

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权 |
| 403 | 禁止访问 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

## 注意事项

1. 所有日期参数格式必须为：YYYY-MM-DD
2. periodType 参数只能是 day、week 或 month
3. 返回的统计数据中：
   - averageRating: 保留1位小数
   - satisfactionRate: 保留1位小数，单位为百分比
   - 满意度定义为评分大于等于4分
4. 获取评分最高的医生时，默认只返回至少有5条评价的医生 