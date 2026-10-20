# 01-hello-llm

第一课只做两件事：把一段 prompt 发给模型并拿回文本，以及把模型的 JSON 回答映射成一个 Java record。

## 你在看的对象

调用链是：

```text
HTTP 或 CLI → HelloChat → ChatClient → ChatModel → OpenAI 兼容接口
```

- `ChatModel` 是供应商那一层的端口。Spring AI 的 OpenAI starter 会按 `spring.ai.openai.*` 自动配置一个实现。换 base URL 就能接到 DeepSeek、DashScope 兼容模式或 Ollama，业务代码不用改。
- `ChatClient` 是你真正调用的流畅 API。自动配置提供的是 prototype 的 `ChatClient.Builder`，`HelloChat` 在构造时 `build()` 一次。
- `complete` 走 `call().content()`，结果是一段字符串。
- `lessonCard` 走 `call().entity(LessonCard.class)`。Spring AI 把 record 的结构告诉模型，再把返回的 JSON 反序列化成 `LessonCard`。这就是本课的 structured output。
- REST 在 `ChatController`：`POST /api/chat` 和 `POST /api/chat/structured`。
- CLI 在 profile `cli` 下由 `HelloCliRunner` 启动。它关掉 Web 服务器，读 `--prompt`，可选 `--structured`，打完结果就退出。

提示词集中在 `PromptTexts`，参数校验在 `TextInputs`。两边都是只有静态方法的类，REST 和 CLI 共用 `HelloChat`。

## 运行

配置见仓库根目录的 README。`LLM_API_KEY` 必须有值，否则客户端起得来，但一调用就会被供应商拒绝。

在仓库根目录：

```bash
mvn -pl 01-hello-llm spring-boot:run
```

默认端口 `8080`，可以用 `SERVER_PORT` 改。

```bash
curl -s localhost:8080/api/chat \
  -H 'content-type: application/json' \
  -d '{"prompt":"用一句话解释 ChatClient"}'
```

期望的形状：

```json
{"answer":"..."}
```

结构化输出：

```bash
curl -s localhost:8080/api/chat/structured \
  -H 'content-type: application/json' \
  -d '{"topic":"ChatClient"}'
```

期望的形状：

```json
{"title":"...","summary":"...","concepts":["..."]}
```

`prompt` 或 `topic` 为空白时返回 `400`，正文是 `{"error":"..."}`。模型返回空内容时返回 `502`。

CLI：

```bash
mvn -pl 01-hello-llm spring-boot:run \
  -Dspring-boot.run.profiles=cli \
  -Dspring-boot.run.arguments="--prompt=用一句话解释 ChatClient"
```

加上 `--structured` 会要一张教学卡片，并按纯文本打印 `title`、`summary` 和 `concepts`：

```bash
mvn -pl 01-hello-llm spring-boot:run \
  -Dspring-boot.run.profiles=cli \
  -Dspring-boot.run.arguments="--prompt=ChatClient --structured"
```

没写 `--prompt` 时进程会失败，并说明需要 `--prompt=<text>`。

## 测试

`mvn -B verify` 不需要真实 key。单元测试直接 `ChatClient.builder(mock ChatModel)`。`ChatControllerTest` 启动 Spring 上下文，用 `@MockitoBean` 替换自动配置的 `ChatModel`，base URL 指到一个不会被访问的本地端口。mock 返回普通文本或一段 JSON，`entity()` 仍然走真实的解析路径。

```bash
mvn -pl 01-hello-llm -B verify
```
