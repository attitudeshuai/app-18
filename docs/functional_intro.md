# BabyGearPass 功能介绍

## 1. 业务背景与解决的问题

婴幼儿用品（如推车、安全座椅、餐椅、玩具等）具有使用周期短的特点——往往几个月到一两年便不再适用，但物品本身仍具备良好的使用价值。许多家庭面临以下困境：

- **闲置浪费**：孩子长大后，用品堆积在家，占用空间却无法处理。
- **信息不对称**：有需要的家庭难以找到可靠、成色可信赖的二手用品来源。
- **情感断层**：每一件母婴用品背后都承载着家庭的回忆，简单的丢弃或转卖无法传递这份情感。

BabyGearPass 致力于解决上述问题，提供一个母婴用品流转与故事传承平台，让闲置的母婴用品在家庭之间有序传递，减少资源浪费，同时通过"故事"功能记录每件用品的传承经历，赋予物品情感价值，构建温暖的社区氛围。

---

## 2. 用户角色与核心用例

### 用户角色

| 角色 | 说明 |
|------|------|
| 普通用户 | 注册后即可发布用品、浏览搜索、发起流转、撰写故事、查看统计 |

### 核心用例

- **注册/登录**：新用户通过用户名、邮箱、密码完成注册，登录后获取 JWT 令牌进行后续操作。
- **发布用品**：用户发布闲置母婴用品信息，包括标题、分类、成色、品牌、适用年龄、描述、价格类型等。
- **浏览/搜索**：用户可通过关键词、分类、状态、成色等条件筛选和搜索用品，支持分页和排序。
- **创建流转**：用品所有者（赠送方）将物品流转给接收方，记录交接日期、地点和备注。
- **撰写故事**：用户可为用品撰写使用故事和传承经历，附加照片，增添情感价值。
- **查看统计**：用户可查看平台概览统计数据（用户数、用品数、流转数、故事数等）及趋势分析。

---

## 3. 功能模块详细说明

### 3.1 用户认证

| 功能 | 接口 | 说明 |
|------|------|------|
| 注册 | `POST /api/auth/register` | 提供用户名、邮箱、密码完成注册，返回 JWT 令牌 |
| 登录 | `POST /api/auth/login` | 使用用户名和密码登录，返回 JWT 令牌 |
| 查看个人信息 | `GET /api/auth/me` | 需携带 Bearer Token，返回当前用户信息 |
| 更新个人信息 | `PUT /api/auth/me` | 需携带 Bearer Token，更新用户资料 |

认证机制基于 JWT，所有受保护接口均需在请求头中携带 `Authorization: Bearer <token>`。

### 3.2 用品管理

| 功能 | 接口 | 说明 |
|------|------|------|
| 用品列表 | `GET /api/gearitems` | 支持关键词搜索、分类/状态/成色筛选，支持分页和排序 |
| 创建用品 | `POST /api/gearitems` | 需认证，填写标题、分类、成色、价格类型等 |
| 查看用品详情 | `GET /api/gearitems/{id}` | 返回用品完整信息 |
| 更新用品 | `PUT /api/gearitems/{id}` | 需认证，仅所有者可操作 |
| 删除用品 | `DELETE /api/gearitems/{id}` | 需认证，仅所有者可操作 |
| 更新用品状态 | `PATCH /api/gearitems/{id}/status` | 需认证，仅所有者可操作，控制用品状态流转 |
| 我的用品 | `GET /api/gearitems/mine` | 需认证，返回当前用户发布的用品列表 |

### 3.3 流转管理

| 功能 | 接口 | 说明 |
|------|------|------|
| 流转列表 | `GET /api/gearhandovers` | 支持关键词、状态、用品ID筛选，支持分页和排序 |
| 创建流转 | `POST /api/gearhandovers` | 需认证，赠送方指定用品和接收方，填写交接日期、地点 |
| 查看流转详情 | `GET /api/gearhandovers/{id}` | 返回流转完整信息 |
| 更新流转 | `PUT /api/gearhandovers/{id}` | 需认证，仅赠送方可操作 |
| 删除流转 | `DELETE /api/gearhandovers/{id}` | 需认证，仅赠送方可操作 |
| 更新流转状态 | `PATCH /api/gearhandovers/{id}/status` | 需认证，流转状态从 Pending 变更为 Completed 或 Cancelled |

### 3.4 分类管理

| 功能 | 接口 | 说明 |
|------|------|------|
| 分类列表 | `GET /api/gearcategories` | 返回所有分类，按排序字段排列 |
| 创建分类 | `POST /api/gearcategories` | 需认证，填写分类名称、描述、排序值 |
| 查看分类详情 | `GET /api/gearcategories/{id}` | 返回分类信息 |
| 更新分类 | `PUT /api/gearcategories/{id}` | 需认证 |
| 删除分类 | `DELETE /api/gearcategories/{id}` | 需认证，若该分类下存在用品则禁止删除 |

### 3.5 故事管理

| 功能 | 接口 | 说明 |
|------|------|------|
| 故事列表 | `GET /api/gearstories` | 支持关键词、用品ID筛选，支持分页和排序 |
| 创建故事 | `POST /api/gearstories` | 需认证，关联用品ID，填写内容和照片 |
| 查看故事详情 | `GET /api/gearstories/{id}` | 返回故事完整信息 |
| 更新故事 | `PUT /api/gearstories/{id}` | 需认证，仅作者可操作 |
| 删除故事 | `DELETE /api/gearstories/{id}` | 需认证，仅作者可操作 |

### 3.6 统计与搜索

| 功能 | 接口 | 说明 |
|------|------|------|
| 概览统计 | `GET /api/stats/overview` | 返回用户总数、用品总数、流转总数、故事总数及各状态用品数 |
| 趋势分析 | `GET /api/stats/trend` | 支持日期范围筛选，返回趋势数据 |
| 全局搜索 | 通过各模块列表接口的 `keyword` 参数实现 | 用品、流转、故事均支持关键词搜索 |

---

## 4. 数据库 ER 图文字描述

本系统包含五个核心实体，其关系如下：

### 实体与字段概览

- **User（用户）**：id, username, email, passwordHash, avatar, createdAt, updatedAt
- **GearItem（母婴用品）**：id, title, condition, brand, suitableAge, description, photos, status, priceType, price, createdAt
- **GearHandover（用品流转）**：id, handoverDate, location, status, note
- **GearStory（用品故事）**：id, content, photos, createdAt
- **GearCategory（用品分类）**：id, name, description, sortOrder, createdAt

### 实体关系

| 关系 | 类型 | 说明 |
|------|------|------|
| User → GearItem | 一对多 | 一个用户（owner）可拥有多个用品 |
| User → GearHandover | 一对多 | 一个用户作为赠送方（giver）可发起多个流转；一个用户作为接收方（receiver）可接收多个流转 |
| User → GearStory | 一对多 | 一个用户（author）可撰写多个故事 |
| GearCategory → GearItem | 一对多 | 一个分类下可包含多个用品 |
| GearItem → GearHandover | 一对多 | 一个用品可对应多个流转记录 |
| GearItem → GearStory | 一对多 | 一个用品可关联多个故事 |

---

## 5. 关键业务规则

### 5.1 用品状态流转

用品（GearItem）具有以下状态，按顺序流转：

```
Available（可用） → Reserved（已预留） → HandedOver（已交接） → Archived（已归档）
```

- **Available**：用品刚发布时的初始状态，表示可被他人浏览和申请。
- **Reserved**：用品已被预留，等待交接。
- **HandedOver**：用品已完成交接，不再可用。
- **Archived**：用品已归档，不再展示在可用列表中。

### 5.2 流转状态

流转（GearHandover）具有以下状态：

```
Pending（待处理） → Completed（已完成）
                 → Cancelled（已取消）
```

- **Pending**：流转创建时的初始状态。
- **Completed**：流转成功完成，用品状态随之变为 HandedOver。
- **Cancelled**：流转被取消，用品状态恢复为 Available。

### 5.3 权限规则

- **用品**：仅用品的所有者（owner）可修改、删除其发布的用品及更新用品状态。
- **故事**：仅故事的作者可修改、删除其撰写的故事。
- **流转**：仅流转的赠送方（giver）可修改、删除流转记录及更新流转状态。
- **分类**：分类管理为通用操作，认证用户均可执行；若分类下存在关联用品，则禁止删除该分类。

### 5.4 成色评级

用品的成色（condition）采用以下评级标准：

| 评级 | 说明 |
|------|------|
| 全新 | 未使用过，包装完好 |
| 九成新 | 使用次数极少，几乎无磨损 |
| 七成新 | 正常使用，有轻微使用痕迹 |
| 五成新及以下 | 有明显使用痕迹，功能完好 |

### 5.5 价格类型

用品的价格类型（priceType）支持以下方式：

| 类型 | 说明 |
|------|------|
| 免费 | 无偿赠送 |
| 议价 | 价格可协商 |
| 固定价 | 明码标价，价格字段需填写具体金额 |

---

## 6. 接口调用示例

### 6.1 用户注册

```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "mom_baby",
  "email": "mombaby@example.com",
  "password": "SecurePass123",
  "avatar": "https://example.com/avatar.jpg"
}
```

响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "username": "mom_baby",
    "email": "mombaby@example.com"
  }
}
```

### 6.2 创建用品

```http
POST /api/gearitems
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...

{
  "title": "好孩子轻便推车",
  "categoryId": 1,
  "condition": "九成新",
  "brand": "好孩子",
  "suitableAge": "0-3岁",
  "description": "轻便折叠推车，使用不到半年，成色极好，适合出行使用。",
  "photos": "https://example.com/stroller1.jpg,https://example.com/stroller2.jpg",
  "priceType": "议价",
  "price": 300
}
```

响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "title": "好孩子轻便推车",
    "category": { "id": 1, "name": "推车" },
    "condition": "九成新",
    "brand": "好孩子",
    "suitableAge": "0-3岁",
    "description": "轻便折叠推车，使用不到半年，成色极好，适合出行使用。",
    "status": "Available",
    "priceType": "议价",
    "price": 300,
    "owner": { "id": 1, "username": "mom_baby" },
    "createdAt": "2026-06-20T10:30:00"
  }
}
```

### 6.3 查看统计概览

```http
GET /api/stats/overview
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalUsers": 128,
    "totalGearItems": 456,
    "totalHandovers": 89,
    "totalStories": 67,
    "availableItems": 312,
    "reservedItems": 45,
    "handedOverItems": 78,
    "archivedItems": 21
  }
}
```

### 6.4 创建流转

```http
POST /api/gearhandovers
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...

{
  "gearItemId": 1,
  "receiverId": 2,
  "handoverDate": "2026-06-25",
  "location": "北京市朝阳区望京SOHO",
  "note": "请准时到达，推车已清洁消毒。"
}
```

响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "gearItem": { "id": 1, "title": "好孩子轻便推车" },
    "giver": { "id": 1, "username": "mom_baby" },
    "receiver": { "id": 2, "username": "dad_happy" },
    "handoverDate": "2026-06-25",
    "location": "北京市朝阳区望京SOHO",
    "status": "Pending",
    "note": "请准时到达，推车已清洁消毒。"
  }
}
```
