# 接口认证指南

本文档说明了系统接口的认证要求。

## 1. 接口认证方式

系统使用JWT（JSON Web Token）进行用户认证。

## 2. 接口分类

### 2.1 公开接口（无需认证）

以下接口任何人都可以访问，无需认证：

- `POST /login` - 用户登录
- `POST /register` - 用户注册
- `GET /register/check-username` - 检查用户名是否可用

### 2.2 认证接口（需要JWT认证）

除了上述公开接口外，所有其他接口都需要用户通过JWT认证才能访问。请求时必须在请求头中携带有效的JWT Token。

## 3. JWT认证配置说明

系统使用Spring Security进行JWT认证配置：

```java
.authorizeHttpRequests(authz -> authz
    .requestMatchers("/login", "/register", "/register/check-username").permitAll()
    // 以下权限检查已被注释掉
    //.requestMatchers("/admin/**").hasRole("ADMIN")
    //.requestMatchers("/satisfaction/**").hasAnyRole("ADMIN", "USER")
    //.requestMatchers("/recommend/**").hasAnyRole("ADMIN", "USER")
    //.requestMatchers("/reviews/**").hasAnyRole("ADMIN", "USER")
    .anyRequest().authenticated()
)
```

## 4. JWT认证

系统使用JWT (JSON Web Token) 进行认证。用户登录后获取token，后续请求需在请求头中携带：

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

## 5. 认证错误码

认证相关错误会返回以下状态码：

- 401 (Unauthorized) - 用户未登录或token无效
~~- 403 (Forbidden) - 用户已登录但权限不足~~ 