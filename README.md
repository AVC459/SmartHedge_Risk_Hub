# 🛢️ SmartHedge Risk Hub

大宗商品交易与风险管理后台。后端 Spring Boot 3 + MyBatis-Plus，前端是放在 `static/` 下的 Vue 3 单页应用（不走构建，直接用 CDN 版运行时），另外带一个基于本地 Ollama 的 PDF 知识库问答模块。

管理的是平台的主数据：交易品种、交易所、期货合约、现货商品、供货品类、产地、经营者、供应商档案。业务上不做撮合和结算，只负责把这些基础档案维护好，供交易与风控系统引用。

## 界面

登录后左侧是分组菜单，右侧是列表页。所有页面共用一套 `api.js` 请求层，返回结构统一为 `{ success, code, msg, data, total }`。

```
运营概览        统计卡片 + 各模块记录数
基础数据        交易品种 / 供货品类 / 产地字典 / 经营者
现货与供应商     现货商品 / 供应商档案
衍生品合约       期货合约
智能助手        知识库问答（PDF 检索 + 流式回答）
```

## 功能

| 模块 | 做什么 |
| --- | --- |
| 登录鉴权 | 账号密码登录，Session 保存在服务端，前端启动时请求 `/api/user/current` 做路由守卫 |
| 运营概览 | 汇总品种、品类、经营者、现货、供应商、合约的数量，数据来自各模块分页接口的 `total` |
| 交易品种 | 分页查询，关联交易所与行业字典 |
| 供货品类 | 列表 + 关键字模糊查询 |
| 产地字典 | 分页查询，给现货商品做下拉数据源 |
| 经营者 | 全量列表，供应商新增时选填 |
| 现货商品 | 新增与分页，关联产地、品类 |
| 供应商档案 | 增删改查，保存时同步维护与供货品类的关联表 |
| 期货合约 | 增删改查，关联交易品种，编辑时回填 |
| 交易所 | 后端提供 CRUD 与分页，全量列表走 Redis 缓存，前端暂无独立页面 |
| 知识库问答 | 上传 PDF/MD/TXT，切分向量化，提问时先返回引用片段再逐字推送回答 |

## 技术栈

| 层 | 选型 |
| --- | --- |
| 运行时 | JDK 17 |
| 框架 | Spring Boot 3.4.3 |
| 持久层 | MyBatis-Plus 3.5.10.1（`mybatis-plus-spring-boot3-starter` + `jsqlparser`） |
| 数据库 | MySQL 8，连接池 Druid 1.2.23 |
| 缓存 | Redis（`StringRedisTemplate` + Jackson JSON 序列化） |
| 前端 | Vue 3 + Element Plus，原生 `<script>` 引入，无打包 |
| AI | Spring AI 1.0.0-M6 + Ollama（`qwen2.5:7b` 对话 / `bge-m3` 向量） |
| 文档解析 | Spring AI PDF Reader + `TokenTextSplitter` |

## 结构

```
browser
  │  GET /  →  static/index.html（Vue3 + Element Plus）
  │  fetch /api/**（同源，credentials: same-origin）
  ▼
Spring Boot :8086
  ├── /api/user/**            登录、注册、登出、当前用户（Session）
  ├── /api/tradingvariety/**  交易品种
  ├── /api/exchange/**        交易所（命中 Redis，未命中查库回填）
  ├── /api/supplyCategory/**  供货品类
  ├── /api/origindict/**      产地字典
  ├── /api/operator/**        经营者
  ├── /api/spotproduct/**     现货商品
  ├── /api/supplier/**        供应商 + 品类关联
  ├── /api/futuresContract/** 期货合约
  └── /api/ai/**              知识库与流式问答
        ├── SimpleVectorStore（堆内存 + data/vectorstore.json 落盘）
        └── Ollama :11434
```

前端静态资源和后端同源同端口，所以没有配 CORS。改端口只需要动 `application.yml` 里的 `server.port`。

## 快速开始

### 1. 环境

- JDK 17
- MySQL 8
- Redis（可选，没启动会自动降级查库）
- Ollama（只有用到智能问答才需要）

```bash
ollama pull qwen2.5:7b
ollama pull bge-m3
```

### 2. 数据库

新建库 `smarthedge_risk_hub`（utf8mb4），业务流程依赖的 10 张业务表需要你按现有库结构自行导入。仓库里只带了登录用户表的建表脚本：

```bash
mysql -uroot -p --default-character-set=utf8mb4 smarthedge_risk_hub < sql/sys_user.sql
```

脚本会建 `sys_user` 并写入默认账号 `admin / 123456`，重复执行不会重复插入。

### 3. 配置

`src/main/resources/jdbc.properties` 里改数据库地址和库名。账号口令支持环境变量覆盖，不改文件也能跑：

```properties
jdbc.username=${MYSQL_USERNAME:root}
jdbc.password=${MYSQL_PASSWORD:123456}
```

### 4. 启动

```bash
./mvnw spring-boot:run
```

或者打包后运行：

```bash
./mvnw clean package -DskipTests
java -jar target/SmartHedge_Risk_Hub-0.0.1-SNAPSHOT.jar
```

打开 http://localhost:8086 ，用 `admin / 123456` 登录。

### 5. 知识库首次启动

应用启动时会把 jar 内 `classpath:knowledge/` 下的四份 PDF 释放到工作目录的 `knowledge/`，然后解析、切分、调用 `bge-m3` 生成向量，落到 `data/vectorstore.json`。

第一次启动要跑几十秒到几分钟，取决于本机推理速度。向量文件和指纹对得上时下次直接加载，不重复计算；PDF 增删或大小变化会触发后台重建。

## 智能问答是怎么跑的

提问走 `POST /api/ai/ask/stream`，返回 `text/event-stream`，分三类事件：

| 事件 | 内容 |
| --- | --- |
| `sources` | 命中的知识库片段，带文件名、段号和原文摘录 |
| `token` | 模型逐字生成的片段 |
| `done` | 总耗时（毫秒） |

设计上有几点是踩过坑才这么写的：

**先发 sources 再发 token。** 检索是同步的、几百毫秒就完成，模型生成要几秒。先把引用推给前端，用户马上能看到「答案是从哪份文档的哪一段来的」，等待期间页面不是空的。

**SSE 的 data 全部走 Jackson 编码。** 早期直接拼字符串，模型输出里出现换行就把 SSE 帧切断了，前端解析直接报错。现在统一 `objectMapper.writeValueAsString()`。

**断开连接要能停。** `SseEmitter` 注册了 `onTimeout` 和 `onError`，用 `AtomicBoolean` 保证 `complete()` 只执行一次，同时 `dispose()` 掉上游的 Flux 订阅，不然模型还在后台跑。

**提示词里把边界写死。** 系统提示词要求结论必须来自检索片段，信息不足就说「知识库中暂未收录」，禁止编造保证金比例、手续费、涨跌停板这类具体数值，也不给投资建议。知识库答不上来的问题，宁可让它说不知道。

**会话记忆在内存里。** `InMemoryChatMemory` + 固定 `conversationId`，重启即清空，单机够用，多实例部署要换成 Redis 或 JDBC 版本。

## Redis 缓存

交易所这类变动很少、读得很频繁的数据走缓存，键是 `smarthedge:exchange:list` 之类，值统一存 JSON 字符串，避免 JDK 序列化在不同版本间的兼容问题。

Redis 不可用不影响业务：

- 请求进来先看熔断窗口，窗口内直接查库，不去连 Redis
- 连接或读写抛异常就记一条 warn，然后把窗口推到 60 秒后
- 主数据变更时主动 `evict` 对应键

这样 Redis 挂了最多是缓存失效，不会让每个请求都卡在连接超时上。

## 接口

一共 40 个接口，完整清单和「哪个页面调了哪个接口、落到哪张表」见 [docs/功能-接口-数据表对照.md](docs/功能-接口-数据表对照.md)。常用的几个：

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/user/login` | 登录，成功后写 Session |
| GET | `/api/user/current` | 当前用户，前端路由守卫用 |
| GET | `/api/tradingvariety/page` | 品种分页 |
| GET | `/api/exchange/list` | 交易所全量（走缓存） |
| GET | `/api/spotproduct/page` | 现货分页 |
| GET | `/api/supplier/page` | 供应商分页 |
| GET | `/api/futuresContract/page` | 合约分页 |
| POST | `/api/ai/ask/stream` | 流式问答 |
| POST | `/api/ai/kb/upload` | 上传文档并入库 |
| POST | `/api/ai/kb/rebuild` | 重建向量索引 |

分页参数统一是 `page` / `size` / `keyword`。

## 目录

```
src/main/java/com/ape/smarthedge_risk_hub/
├── ai/          Spring AI 装配、知识库实现、SSE 接口
├── cache/       Redis 缓存封装（含不可用降级）
├── common/      统一返回体 Result、请求参数基类
├── config/      MyBatis-Plus 分页插件
├── controller/  9 个控制器，40 个接口
├── mapper/      Mapper 接口
├── pojo/        11 个实体
└── service/     服务接口与实现
src/main/resources/
├── application.yml
├── jdbc.properties
├── knowledge/   内置 PDF 知识库（打包进 jar）
└── static/      前端：index.html + assets/{css,js,vendor}
sql/sys_user.sql 登录用户表
docs/            功能、接口、数据表对照
```

## 已知限制

这些是当前版本真实存在的问题，不是待办清单式的客套：

1. **密码明文存库。** 课程演示用途，改成 BCrypt 只是加一行 `PasswordEncoder`，但存量数据要迁移，暂时没动。
2. **业务接口没有服务端鉴权。** 保护只在前端路由守卫这一层，直接 curl `/api/supplier/page` 是能拿到数据的。要补的话加一个 `HandlerInterceptor` 拦 `/api/**`、放行 `/api/user/**` 即可。
3. **分页是内存分页。** Service 里全量查出来再 `subList`，几万条以内感觉不到，数据量上来必须换成 `IPage`。
4. **供应商编辑不回填品类。** `findAllSupplierWithOperator` 没返回已关联的 `categoryIds`，编辑弹窗里品类是空的，直接保存会把原关联清掉。补一个查询关联的接口就能修。
5. **注册是开放的。** 没有邀请码或审批，注册完就是启用状态，角色默认 `operator`。
6. **对话记忆在内存。** 重启丢失，也不支持多实例。
7. **知识库切片参数写死。** `TokenTextSplitter(450, 120, 5, 2000, true)`，换文档类型可能要调，现在没有做成配置项。
8. **交易所模块只有接口。** 前端没做页面，只能直接调接口验证。

## 赞赏

如果这个项目对你有帮助，点个 Star 就够了。

需要改功能、接学校或公司的现有系统，可以邮件联系：<284579804@qq.com>，说明场景和大致需求。

## License

MIT
