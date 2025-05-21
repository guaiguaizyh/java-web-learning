# 医生职称管理 API

## 基本信息
- **基础路径**: `/positions`
- **请求格式**: JSON
- **响应格式**: JSON
- **描述**: 提供医生职称的增删改查管理功能

## 接口列表

### 获取所有职称信息
- **接口**: `GET /positions`
- **描述**: 获取系统中所有的医生职称信息
- **权限要求**: 所有用户可访问

**请求参数**: 无

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "positionsId": 1,
      "positionsName": "主任医师",
      "description": "具有丰富临床经验和较高学术水平的高级职称",
      "sortOrder": 1,
      "status": 1,
      "createTime": "2023-05-10T10:00:00",
      "updateTime": "2023-05-10T10:00:00"
    },
    {
      "positionsId": 2,
      "positionsName": "副主任医师",
      "description": "具有较丰富临床经验的高级职称",
      "sortOrder": 2,
      "status": 1,
      "createTime": "2023-05-10T10:00:00",
      "updateTime": "2023-05-10T10:00:00"
    },
    {
      "positionsId": 3,
      "positionsName": "主治医师",
      "description": "具有独立处理常见疾病能力的中级职称",
      "sortOrder": 3,
      "status": 1,
      "createTime": "2023-05-10T10:00:00",
      "updateTime": "2023-05-10T10:00:00"
    }
  ]
}
```

**错误响应**:
```json
{
  "code": 500,
  "message": "获取职称列表失败：数据库连接错误",
  "data": null
}
```

### 获取职称分布统计
- **接口**: `GET /positions/distribution`
- **描述**: 获取各职称下医生的数量分布统计
- **权限要求**: 所有用户可访问

**请求参数**: 无

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "positionsId": 1,
      "positionsName": "主任医师",
      "doctorCount": 5
    },
    {
      "positionsId": 2,
      "positionsName": "副主任医师",
      "doctorCount": 8
    },
    {
      "positionsId": 3,
      "positionsName": "主治医师",
      "doctorCount": 12
    },
    {
      "positionsId": 4,
      "positionsName": "住院医师",
      "doctorCount": 20
    }
  ]
}
```

**错误响应**:
```json
{
  "code": 500,
  "message": "获取职称分布统计失败：数据库连接错误",
  "data": null
}
```

### 获取职称详情
- **接口**: `GET /positions/{positionsId}`
- **描述**: 根据ID获取特定职称的详细信息
- **权限要求**: 所有用户可访问

**路径参数**:
| 参数名称 | 类型 | 是否必需 | 描述 |
|---------|------|---------|------|
| positionsId | Integer | 是 | 职称ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "positionsId": 1,
    "positionsName": "主任医师",
    "description": "具有丰富临床经验和较高学术水平的高级职称",
    "sortOrder": 1,
    "status": 1,
    "createTime": "2023-05-10T10:00:00",
    "updateTime": "2023-05-10T10:00:00"
  }
}
```

**错误响应**:
```json
{
  "code": 404,
  "message": "未找到指定职称",
  "data": null
}
```

### 添加职称
- **接口**: `POST /positions`
- **描述**: 添加新的医生职称
- **权限要求**: 管理员权限

**请求体**:
```json
{
  "positionsName": "住院医师",
  "description": "初级医师职称，具备基本临床实践能力",
  "sortOrder": 4,
  "status": 1
}
```

**请求参数说明**:
| 参数名称 | 类型 | 是否必需 | 描述 |
|---------|------|---------|------|
| positionsName | String | 是 | 职称名称 |
| description | String | 否 | 职称描述 |
| sortOrder | Integer | 否 | 排序顺序（值越小排序越靠前） |
| status | Integer | 否 | 状态（1:启用, 0:禁用）|

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": true
}
```

**错误响应**:
```json
{
  "code": 400,
  "message": "添加职称失败：职称名称不能为空",
  "data": false
}
```

### 更新职称
- **接口**: `PUT /positions/{positionsId}`
- **描述**: 更新现有的医生职称信息
- **权限要求**: 管理员权限

**路径参数**:
| 参数名称 | 类型 | 是否必需 | 描述 |
|---------|------|---------|------|
| positionsId | Integer | 是 | 职称ID |

**请求体**:
```json
{
  "positionsName": "住院医师",
  "description": "初级医师职称，负责住院病人的日常医疗工作",
  "sortOrder": 4,
  "status": 1
}
```

**请求参数说明**:
与添加职称接口相同

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": true
}
```

**错误响应**:
```json
{
  "code": 404,
  "message": "更新职称失败：指定职称不存在",
  "data": false
}
```

### 删除职称
- **接口**: `DELETE /positions/{positionsId}`
- **描述**: 删除指定的医生职称。如果有医生关联了该职称，系统会自动将这些医生的职称设置为空，并在响应中提供受影响的医生数量。
- **权限要求**: 管理员权限

**路径参数**:
| 参数名称 | 类型 | 是否必需 | 描述 |
|---------|------|---------|------|
| positionsId | Integer | 是 | 职称ID |

**响应示例** (成功，无关联医生):
```json
{
  "code": 200,
  "message": "职称已成功删除",
  "data": {
    "success": true,
    "affectedDoctors": 0,
    "message": "职称已成功删除"
  }
}
```

**响应示例** (成功，有关联医生):
```json
{
  "code": 200,
  "message": "职称已删除，同时更新了5位医生的职称",
  "data": {
    "success": true,
    "affectedDoctors": 5,
    "message": "职称已删除，同时更新了5位医生的职称"
  }
}
```

**错误响应**:
```json
{
  "code": 400,
  "message": "删除职称失败：职称不存在",
  "data": null
}
```

## 错误代码

| 错误代码 | 描述 |
|---------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权，需要登录 |
| 403 | 禁止访问，权限不足 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

## 数据结构

### Positions (职称)

| 字段名 | 类型 | 说明 |
|-------|------|------|
| positionsId | Integer | 职称ID，唯一标识 |
| positionsName | String | 职称名称，如"主任医师"、"副主任医师" |
| description | String | 职称描述信息 |
| sortOrder | Integer | 排序顺序，值越小排序越靠前 |
| status | Integer | 状态（1:启用, 0:禁用）|
| createTime | Date | 创建时间 |
| updateTime | Date | 更新时间 |

### PositionDistribution (职称分布)

| 字段名 | 类型 | 说明 |
|-------|------|------|
| positionsId | Integer | 职称ID |
| positionsName | String | 职称名称 |
| doctorCount | Integer | 该职称下的医生数量 |

## 使用示例

### 示例1: 获取所有职称
```
GET /positions
```

### 示例2: 获取职称分布统计
```
GET /positions/distribution
```

### 示例3: 添加新职称
```
POST /positions
Content-Type: application/json

{
  "positionsName": "实习医师",
  "description": "医学院校毕业实习阶段的医师",
  "sortOrder": 5,
  "status": 1
}
```

### 示例4: 更新职称信息
```
PUT /positions/5
Content-Type: application/json

{
  "positionsName": "实习医师",
  "description": "处于实习期的医师，在指导医师监督下进行临床实践",
  "sortOrder": 5,
  "status": 1
}
```

### 示例5: 删除职称
```
DELETE /positions/5
```

## 注意事项

1. 删除职称前，请确保该职称下没有关联的医生，否则删除操作将失败
2. 职称的排序顺序(sortOrder)影响在前端界面上的显示顺序
3. 职称是医生资质的重要标识，请谨慎操作修改和删除操作
4. 添加和更新操作需要管理员权限 