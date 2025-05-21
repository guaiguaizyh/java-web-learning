# 症状管理 API

## 基本信息
- **基础路径**: `/symptoms`
- **请求格式**: JSON
- **响应格式**: JSON
- **描述**: 提供症状信息管理的相关接口

## 症状信息管理

### 获取症状列表
- **接口**: `GET /symptoms`
- **描述**: 分页获取症状列表，支持按关键词和科室筛选
- **权限要求**: 所有用户可访问

**请求参数**:
| 参数名称 | 类型 | 是否必需 | 描述 |
|---------|------|---------|------|
| page | Integer | 否 | 页码，默认为1 |
| size | Integer | 否 | 每页记录数，默认为10 |
| keyword | String | 否 | 症状关键词，用于模糊搜索 |
| departmentId | Integer | 否 | 科室ID，筛选特定科室的症状 |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "symptomId": 1,
        "keyword": "头痛",
        "departmentId": 1,
        "departmentName": "神经内科"
      }
    ],
    "total": 100,
    "size": 10,
    "current": 1,
    "pages": 10
  }
}
```

### 获取症状详情
- **接口**: `GET /symptoms/{symptomId}`
- **描述**: 获取指定症状的详细信息
- **权限要求**: 所有用户可访问

**路径参数**:
| 参数名称 | 类型 | 是否必需 | 描述 |
|---------|------|---------|------|
| symptomId | Integer | 是 | 症状ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "symptomId": 1,
    "keyword": "头痛",
    "departmentId": 1,
    "departmentName": "神经内科"
  }
}
```

### 搜索症状
- **接口**: `GET /symptoms/search`
- **描述**: 根据关键词搜索症状
- **权限要求**: 所有用户可访问

**查询参数**:
| 参数名称 | 类型 | 是否必需 | 描述 |
|---------|------|---------|------|
| keyword | String | 是 | 搜索关键词 |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "symptomId": 1,
      "keyword": "头痛",
      "departmentId": 1,
      "departmentName": "神经内科"
    }
  ]
}
```

### 添加症状
- **接口**: `POST /symptoms`
- **描述**: 添加新的症状信息
- **权限要求**: 管理员

**请求体**:
```json
{
  "keyword": "头痛",
  "departmentId": 1
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": true
}
```

### 更新症状
- **接口**: `PUT /symptoms/{symptomId}`
- **描述**: 更新指定症状的信息
- **权限要求**: 管理员

**路径参数**:
| 参数名称 | 类型 | 是否必需 | 描述 |
|---------|------|---------|------|
| symptomId | Integer | 是 | 症状ID |

**请求体**:
```json
{
  "keyword": "头痛",
  "departmentId": 1
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": true
}
```

### 删除症状
- **接口**: `DELETE /symptoms/{symptomId}`
- **描述**: 删除指定的症状
- **权限要求**: 管理员

**路径参数**:
| 参数名称 | 类型 | 是否必需 | 描述 |
|---------|------|---------|------|
| symptomId | Integer | 是 | 症状ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": true
}
```

## 症状关联管理

### 获取专长关联的症状列表
- **接口**: `GET /expertises/{expertiseId}/symptoms`
- **描述**: 获取与指定专长关联的所有症状
- **权限要求**: 所有用户可访问

**路径参数**:
| 参数名称 | 类型 | 是否必需 | 描述 |
|---------|------|---------|------|
| expertiseId | Integer | 是 | 专长ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "symptomId": 1,
      "keyword": "头痛",
      "departmentId": 1,
      "departmentName": "神经内科",
      "relevanceScore": 0.8
    }
  ]
}
```

### 更新专长关联的症状
- **接口**: `POST /expertises/{expertiseId}/symptoms`
- **描述**: 更新指定专长关联的症状列表
- **权限要求**: 管理员

**路径参数**:
| 参数名称 | 类型 | 是否必需 | 描述 |
|---------|------|---------|------|
| expertiseId | Integer | 是 | 专长ID |

**请求体**:
```json
{
  "symptomIds": [1, 2, 3],
  "relevanceScores": [0.8, 0.6, 0.4]
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": true
}
```

## 错误代码

| 错误代码 | 描述 |
|---------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权 |
| 403 | 权限不足 |
| 404 | 未找到指定的症状 |
| 500 | 服务器内部错误 |

## 重要说明

1. **症状关键词**: 关键词应简洁明了，通常为2-10个字符
2. **科室关联**: 每个症状必须关联到一个有效的科室
3. **相关度评分**: 症状与专长的相关度评分范围为0-1，1表示完全相关
4. **删除限制**: 如果症状被关联到专长，需要先解除关联才能删除 