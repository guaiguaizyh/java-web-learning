# 智谱AI聊天窗口 API 文档

## 接口概述

本文档提供了与智谱AI交互的RESTful API接口说明，可用于在前端实现智谱AI聊天窗口，并通过AI为用户提供科室推荐功能。

## 基础信息

- 基础URL: `/ai`
- 数据格式: JSON
- 认证方式: JWT (需要在 Authorization 头部使用 Bearer token)

## 接口列表

### 1. 发送聊天消息

**请求方式**: POST

**路径**: `/ai/chat`

**功能描述**: 向智谱AI发送聊天消息，获取AI回复

**请求参数**:

| 参数名   | 类型   | 必填 | 描述                 |
|--------|--------|-----|---------------------|
| message | String | 是   | 用户发送的消息内容     |

**请求示例**:

```json
{
  "message": "我最近经常头痛，该怎么办？"
}
```

**响应参数**:

响应使用统一的 Result 包装格式:

```json
{
  "code": 200,       // 状态码，200表示成功
  "message": "操作成功", // 状态消息
  "data": {          // 响应数据
    "type": "assistant",  // 消息类型：assistant 表示 AI 回复
    "content": "建议您及时就医...", // AI回复的消息内容
    "timestamp": "2024-04-19T10:30:45" // 消息时间戳
  }
}
```

### 2. 根据症状推荐科室

**请求方式**: POST

**路径**: `/ai/recommend-department`

**功能描述**: 根据用户描述的症状，推荐适合就诊的医院科室

**请求参数**:

| 参数名     | 类型   | 必填 | 描述             |
|----------|--------|-----|-----------------|
| symptoms | String | 是   | 用户描述的症状     |

**请求示例**:

```json
{
  "symptoms": "我最近出现高烧，咳嗽，还有点喘不过气"
}
```

**响应参数**:

响应使用统一的 Result 包装格式:

```json
{
  "code": 200,       // 状态码，200表示成功
  "message": "操作成功", // 状态消息
  "data": {          // 响应数据
    "type": "assistant",  // 消息类型：assistant 表示 AI 回复
    "content": "推荐科室：呼吸内科\n根据您描述的症状，高烧、咳嗽和呼吸困难可能与呼吸系统感染有关，如急性支气管炎或肺炎。呼吸内科医生专门处理这类问题，能够提供专业诊断和治疗方案。", 
    "timestamp": "2024-04-19T10:31:20" // 消息时间戳
  }
}
```

### 3. 根据症状直接推荐医生

**请求方式**: POST

**路径**: `/ai/recommend-doctors`

**功能描述**: 根据用户描述的症状，直接推荐适合的医生，包括科室和医生详细信息

**请求参数**:

| 参数名     | 类型   | 必填 | 描述             |
|----------|--------|-----|-----------------|
| symptoms | String | 是   | 用户描述的症状     |
| limit    | Integer | 否  | 返回医生数量限制，默认为3 |

**请求示例**:

```json
{
  "symptoms": "我最近出现高烧，咳嗽，还有点喘不过气",
  "limit": 5
}
```

**响应参数**:

响应使用统一的 Result 包装格式:

```json
{
  "code": 200,       // 状态码，200表示成功
  "message": "操作成功", // 状态消息
  "data": {          // 响应数据
    "recommendedDepartment": "呼吸内科", // 推荐科室
    "reason": "根据您描述的症状，高烧、咳嗽和呼吸困难可能与呼吸系统感染有关", // 推荐理由
    "doctors": [     // 推荐医生列表
      {
        "id": 1,
        "name": "张医生",
        "title": "主任医师",
        "department": "呼吸内科",
        "hospital": "省人民医院",
        "specialty": "擅长呼吸系统疾病、慢性支气管炎等",
        "rating": 4.9,
        "reviewCount": 125
      },
      {
        "id": 2,
        "name": "李医生",
        "title": "副主任医师",
        "department": "呼吸内科",
        "hospital": "市第一医院",
        "specialty": "擅长肺部感染疾病的诊断与治疗",
        "rating": 4.8,
        "reviewCount": 98
      },
      {
        "id": 3,
        "name": "王医生",
        "title": "主治医师",
        "department": "呼吸内科",
        "hospital": "市第二医院",
        "specialty": "擅长各类呼吸道感染和哮喘等慢性气道疾病",
        "rating": 4.7,
        "reviewCount": 76
      }
    ]
  }
}
```

### 4. AI直接分析症状并推荐医生

**请求方式**: POST

**路径**: `/recommend/doctors/ai-direct`

**功能描述**: 使用智谱AI直接分析用户描述的症状，跳过关键词匹配步骤，直接推断相关专长并推荐医生

**请求参数**:

| 参数名       | 类型     | 必填 | 描述                                      |
|------------|----------|-----|------------------------------------------|
| description | String   | 是   | 用户对症状的自然语言描述                  |
| userId      | Integer  | 否   | 用户ID，提供后可结合协同过滤进行个性化推荐 |
| limit       | Integer  | 否   | 返回的医生数量限制，默认为10              |
| debug       | Boolean  | 否   | 是否返回AI分析调试信息，默认为false        |

**请求示例**:

```json
{
  "description": "我最近总是感到头晕目眩，有时伴随耳鸣，站起来时症状会加重",
  "userId": 1001,
  "limit": 5
}
```

**响应参数**:

响应使用统一的 Result 包装格式:

```json
{
  "code": 200,       // 状态码，200表示成功
  "message": "推荐成功", // 状态消息
  "data": [          // 推荐医生列表
    {
      "doctorId": 42,
      "name": "张医生",
      "gender": "男",
      "departmentName": "神经内科",
      "positionsName": "主任医师",
      "workYears": 15,
      "averageRating": 4.8,
      "ratingCount": 126,
      "avatarUrl": "https://example.com/avatars/doctor42.jpg",
      "expertiseList": "头晕,眩晕症,耳鸣",
      "matchScore": 0.89,
      "recommendReason": "AI推荐：专长与您的症状相符。患者评价良好。"
    },
    {
      "doctorId": 56,
      "name": "李医生",
      "gender": "女",
      "departmentName": "神经内科",
      "positionsName": "副主任医师",
      "workYears": 12,
      "averageRating": 4.6,
      "ratingCount": 98,
      "avatarUrl": "https://example.com/avatars/doctor56.jpg",
      "expertiseList": "头晕,神经系统疾病,平衡障碍",
      "matchScore": 0.76,
      "recommendReason": "AI推荐：专长与您的症状相符。患者评价良好。"
    }
  ]
}
```

**调试模式响应示例** (仅当debug=true时返回):

```json
{
  "code": 200,       // 状态码，200表示成功
  "message": "推荐成功", // 状态消息
  "data": {          // 响应数据
    "aiResponse": "神经内科,耳鼻喉科,内科,眩晕专科,神经外科",
    "parsedExpertises": ["神经内科", "耳鼻喉科", "内科", "眩晕专科", "神经外科"],
    "foundExpertiseIds": [12, 15, 8, 42, 35],
    "doctors": [
      // 医生列表与标准响应相同
    ]
  }
}
```

**特殊说明**:

1. 本接口与传统推荐接口的主要区别是直接使用AI分析症状并推断专长，完全跳过关键词匹配过程
2. 当症状描述为空或为"无"时，接口会自动返回评分最高的医生列表
3. 如果AI无法识别症状或未找到匹配的医生，也会返回评分最高的医生列表
4. 此接口响应时间可能略长于传统接口，因为需要额外调用AI服务
5. 推荐分数计算权重与传统接口不同：AI匹配 (60%) + 评分 (20%) + 协同过滤 (20%)

## 错误码说明

| 错误码 | 说明               |
|-------|-------------------|
| 200   | 操作成功           |
| 400   | 请求参数错误       |
| 401   | 未授权（未登录）   |
| 403   | 没有权限           |
| 500   | 服务器内部错误     |

## 前端集成建议

### 聊天窗口实现
1. 创建一个简洁的聊天界面，包含消息显示区域和输入框
2. 用户输入症状后，调用相应API并显示AI回复
3. 保持聊天历史记录，方便用户查看整个对话流程

### 科室推荐实现
1. 可以在聊天窗口中添加一个"获取科室推荐"的按钮
2. 点击按钮后，基于用户之前输入的症状描述调用推荐科室API
3. 展示推荐的科室及解释理由
4. 可以进一步提供该科室的医生列表链接，方便用户快速找到合适的医生

## 示例代码

### 前端调用示例 (使用 JavaScript fetch API)

```javascript
// 发送聊天消息
async function sendChatMessage(message) {
  const response = await fetch('/ai/chat', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer ' + localStorage.getItem('token')
    },
    body: JSON.stringify({ message })
  });
  
  return await response.json();
}

// 获取科室推荐
async function getDepartmentRecommendation(symptoms) {
  const response = await fetch('/ai/recommend-department', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer ' + localStorage.getItem('token')
    },
    body: JSON.stringify({ symptoms })
  });
  
  return await response.json();
}

// 获取医生推荐
async function getDoctorRecommendation(symptoms, limit = 3) {
  const response = await fetch('/ai/recommend-doctors', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer ' + localStorage.getItem('token')
    },
    body: JSON.stringify({ symptoms, limit })
  });
  
  return await response.json();
}
```

## 注意事项

1. API调用需要用户登录权限，确保在请求头中包含有效的JWT token
2. 消息内容有长度限制，过长的消息可能会被截断
3. AI回复时间可能有波动，建议在前端添加加载状态提示
4. 避免频繁调用API，建议实现合理的节流或防抖机制 