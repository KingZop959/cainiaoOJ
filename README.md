# 菜鸟OJ
## 项目简介
菜鸟OJ 是一个基于 **Spring Cloud 微服务架构**、**消息队列 (RabbitMQ)** 和 **Docker** 的在线编程测评平台。  
平台内置自主实现的 **代码沙箱**，支持 **Python、Java、C、C++** 四种语言的 **ACM 刷题模式**。  
结合 **SpringAI** 接入 **千问大模型**，为题目生成 AI 题解，并对用户提交的代码提供智能分析报告。

## 功能特性
- **多语言支持**  
  - Python、Java、C、C++ 四种语言ACM模式刷题
- **自研安全代码沙箱**  
  - 基于 Docker 设计可扩展且安全的执行环境
  - 容器池化处理，显著提升判题响应速度
- **安全防护机制**  
  - Docker 隔离、容器权限限制、内存与时间限制
  - 黑白名单 + 字典树匹配机制，过滤敏感代码指令
- **判题策略与特判支持**  
  - 采用策略模式封装多种判题逻辑
- **异步通信架构**  
  - RabbitMQ 实现微服务间的异步消息传递
  - 降低耦合度并提升系统吞吐性能
- **流量与频率控制**  
  - Sentinel 实现网关层全局流量控制
  - 针对单用户的题目提交频率限流，防止恶意刷提交

## 技术架构
- **后端框架**：Spring Boot、Spring Cloud
- **消息队列**：RabbitMQ
- **容器化**：Docker
- **AI 集成**：SpringAI + 千问大模型
- **流量控制**：Sentinel
- **数据库**：MySQL / Redis（可按需配置）

## 系统架构图
<img width="1053" height="729" alt="image" src="https://github.com/user-attachments/assets/127d01de-0f86-4411-9b93-cbcfc6e0756f" />
