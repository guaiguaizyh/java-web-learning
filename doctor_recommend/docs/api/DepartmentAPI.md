# 科室管理 API 文档

## 基础信息

- **基础路径**: `/departments`
- **响应格式**: JSON
- **认证要求**: 需要JWT Token认证
- **时间格式**: `YYYY-MM-DD HH:mm:ss`

## 1. 科室列表查询

### 1.1 获取科室列表（基础查询）

- **接口**: `GET /departments/list`
- **描述**: 获取科室列表，支持分页、排序和条件筛选
- **认证**: 需要JWT Token认证

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认为1 |
| size | Integer | 否 | 每页记录数，默认为10 |
| departmentName | String | 否 | 科室名称（支持模糊查询） |
| description | String | 否 | 科室描述（支持模糊查询） |
| sortField | String | 否 | 排序字段，支持：departmentId（科室ID）, departmentName（科室名称）, doctorCount（医生数量）, averageRating（平均评分）, expertiseCount（专长数量） |
| sortOrder | String | 否 | 排序方向，支持：asc（升序，默认）, desc（降序） |

#### 响应示例

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "records": [
            {
                "departmentId": 1,
                "departmentName": "内科",
                "description": "主要负责内科疾病的诊断和治疗",
                "doctorCount": 10,
                "reviewCount": 150,
                "expertiseCount": 8,
                "averageRating": 4.5
            }
        ],
        "total": 100,
        "size": 10,
        "current": 1,
        "pages": 10
    }
}
```

### 1.2 高级查询科室列表

- **接口**: `GET /departments/search`
- **描述**: 支持多条件筛选和排序的高级查询接口
- **认证**: 需要JWT Token认证

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认为1 |
| size | Integer | 否 | 每页记录数，默认为10 |
| departmentName | String | 否 | 科室名称（支持模糊查询） |
| description | String | 否 | 科室描述（支持模糊查询） |
| sortField | String | 否 | 排序字段，默认为departmentId |
| sortOrder | String | 否 | 排序方向，默认为asc |

#### 排序字段说明

| 排序字段 | 说明 | 示例 |
|----------|------|------|
| departmentId | 按科室ID排序 | `sortField=departmentId&sortOrder=asc` |
| departmentName | 按科室名称排序 | `sortField=departmentName&sortOrder=desc` |
| doctorCount | 按医生数量排序 | `sortField=doctorCount&sortOrder=desc` |
| averageRating | 按平均评分排序 | `sortField=averageRating&sortOrder=desc` |
| expertiseCount | 按专长数量排序 | `sortField=expertiseCount&sortOrder=desc` |

#### 请求示例

1. 基础分页查询：
```
GET /departments/search?page=1&size=10
```

2. 按科室ID降序排序：
```
GET /departments/search?sortField=departmentId&sortOrder=desc
```

3. 按医生数量降序并筛选科室名称：
```
GET /departments/search?sortField=doctorCount&sortOrder=desc&departmentName=内科
```

#### 响应示例

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "records": [
            {
                "departmentId": 1,
                "departmentName": "内科",
                "description": "主要负责内科疾病的诊断和治疗",
                "doctorCount": 10,
                "reviewCount": 150,
                "expertiseCount": 8,
                "averageRating": 4.5
            }
        ],
        "total": 100,
        "size": 10,
        "current": 1,
        "pages": 10
    }
}
```

#### 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权 |
| 403 | 无权限访问 |
| 500 | 服务器内部错误 |

#### 注意事项

1. 所有查询条件都是可选的，不传则不作为筛选条件
2. 默认按科室ID升序排序
3. 分页参数page从1开始计数
4. 科室名称和描述支持模糊查询
5. 排序方向不区分大小写，asc/ASC 都是合法的
6. 返回的数据会包含科室的统计信息（医生数量、评价数等）

## 2. 科室基础信息管理

### 2.1 获取科室列表
- **接口**: `GET /departments`
- **描述**: 获取科室列表，支持多条件查询和分页
- **权限**: 需要用户登录
- **参数**:
  - `page`: 页码（从1开始，默认：1）
  - `size`: 每页数量（默认：10）
  - `departmentName`: 科室名称（可选，支持模糊查询）
  - `description`: 科室描述（可选，支持模糊查询）
  - `sortField`: 排序字段（可选，可选值：departmentId, departmentName, doctorCount, expertiseCount, averageRating）
  - `sortOrder`: 排序方向（可选，可选值：asc, desc，默认：asc）
- **返回示例**:
```json
{
    "code": 200,
    "msg": "success",
    "data": {
        "total": 100,
        "pages": 10,
        "current": 1,
        "records": [
            {
                "departmentId": 1,
                "departmentName": "内科",
                "description": "内科相关疾病诊治",
                "doctorCount": 16,
                "expertiseCount": 4,
                "reviewCount": 952,
                "averageRating": 4.3
            }
        ]
    }
}
```
- **响应字段说明**:
  - `records`: 科室记录列表
    - `departmentId`: 科室ID
    - `departmentName`: 科室名称
    - `description`: 科室描述
    - `doctorCount`: 科室医生数量
    - `expertiseCount`: 科室关联专长数量
    - `reviewCount`: 评价数量
    - `averageRating`: 平均评分
  - `total`: 总记录数
  - `pages`: 总页数
  - `current`: 当前页码
- **错误响应**:
```json
{
    "code": 400,
    "msg": "参数错误",
    "data": {
        "field": "minRating",
        "message": "最低评分必须在1-5之间"
    }
}
```

### 2.2 获取科室详情
- **接口**: `GET /departments/{departmentId}`
- **描述**: 获取科室详细信息
- **认证**: 需要JWT Token认证
- **参数**:
  - `departmentId`: 科室ID（路径参数）
- **返回示例**:
```json
{
  "code": 200,
    "msg": "success",
  "data": {
        "id": 1,
        "name": "内科",
        "parentId": 0,
        "description": "科室描述",
        "doctorCount": 10,
        "averageRating": 4.5,
        "reviewCount": 100,
        "subDepartments": [
            {
                "id": 2,
                "name": "心血管内科",
                "doctorCount": 5
            }
        ]
  }
}
```

### 2.3 添加科室
- **接口**: `POST /departments`
- **描述**: 添加新科室
- **权限**: 需要管理员权限
- **请求体**:
```json
{
    "name": "内科",
    "description": "内科相关疾病诊治",
    "status": 1
}
```
- **返回示例**:
```json
{
    "code": 201,
    "msg": "success",
    "data": {
        "id": 1,
        "name": "内科",
        "description": "内科相关疾病诊治",
        "status": 1,
        "createTime": "2024-03-15 10:00:00",
        "updateTime": "2024-03-15 10:00:00"
    }
}
```

### 2.4 更新科室信息
- **接口**: `PUT /departments/{departmentId}`
- **描述**: 更新科室信息
- **权限**: 需要管理员权限
- **参数**:
  - `departmentId`: 科室ID（路径参数）
- **请求体**:
```json
{
    "name": "内科",
    "description": "内科相关疾病诊治",
    "status": 1
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

### 2.5 删除科室
- **接口**: `DELETE /departments/{departmentId}`
- **描述**: 删除科室
- **认证**: 需要JWT Token认证（建议ADMIN角色使用）
- **参数**:
  - `departmentId`: 科室ID（路径参数）
- **返回示例**:
```json
{
    "code": 200,
    "msg": "删除成功",
    "data": null
}
```

## 3. 科室医生管理

### 3.1 获取科室医生列表
- **接口**: `GET /departments/{departmentId}/doctors`
- **描述**: 获取科室下的医生列表
- **认证**: 需要JWT Token认证
- **参数**:
  - `departmentId`: 科室ID（路径参数）
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
                "id": 1,
                "name": "张医生",
                "title": "主任医师",
                "rating": 4.8,
                "reviewCount": 100,
                "expertises": ["心血管", "高血压"]
            }
        ],
        "total": 10,
        "size": 10,
        "current": 1
    }
}
```

### 3.2 添加医生到科室
- **接口**: `POST /departments/{departmentId}/doctors`
- **描述**: 将医生添加到科室
- **认证**: 需要JWT Token认证（建议ADMIN角色使用）
- **参数**:
  - `departmentId`: 科室ID（路径参数）
- **请求体**:
```json
{
    "doctorIds": [1, 2]
}
```
- **返回示例**:
```json
{
    "code": 201,
    "msg": "添加成功",
    "data": null
}
```

### 3.3 从科室移除医生
- **接口**: `DELETE /departments/{departmentId}/doctors/{doctorId}`
- **描述**: 从科室移除医生
- **认证**: 需要JWT Token认证（建议ADMIN角色使用）
- **参数**:
  - `departmentId`: 科室ID（路径参数）
  - `doctorId`: 医生ID（路径参数）
- **返回示例**:
```json
{
  "code": 200,
    "msg": "移除成功",
  "data": null
}
```

## 4. 科室评价统计

### 4.1 获取科室评价统计
- **接口**: `GET /departments/{departmentId}/stats`
- **描述**: 获取科室的评价统计信息
- **权限**: 需要用户登录
- **参数**:
  - `departmentId`: 科室ID（路径参数）
- **返回示例**:
```json
{
    "code": 200,
    "msg": "success",
    "data": {
        "totalReviews": 100,
        "averageRating": 4.5,
        "highRatingCount": 85,
        "satisfactionRate": 85.0
    }
}
```

### 4.2 获取科室评分趋势
- **接口**: `GET /departments/{departmentId}/rating/trend`
- **描述**: 获取科室评分趋势
- **权限**: 需要用户登录
- **参数**:
  - `departmentId`: 科室ID（路径参数）
  - `months`: 查询最近几个月的数据（默认12个月）
- **返回示例**:
```json
{
  "code": 200,
    "msg": "success",
  "data": [
    {
            "month": "2024-03",
            "rating": 4.5
        }
  ]
}
```

## 5. 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 201 | 创建成功 |
| 400 | 请求参数错误 |
| 401 | 未授权 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

## 6. 注意事项

1. 科室评分范围为1-5分，保留一位小数
2. 科室评分更新会触发相关统计数据的更新
3. 所有时间参数格式必须为 `YYYY-MM-DD`
4. 科室名称长度限制为50字符
5. 科室描述长度限制为500字符 