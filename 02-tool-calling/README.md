# 02-tool-calling

第二课只做 tool calling：把几个 Java 方法交给模型，由模型决定要不要调用、调用哪一个、参数填什么。应用执行方法，把返回值写回对话，模型再据此给出最终回答。

本课没有 RAG、对话记忆，也不接 MCP。

## 什么是 tool calling

模型不会执行你的代码。它只能在一次响应里声明「我要调用这个工具，参数是这段 JSON」。真正调用方法的是应用。模型拿不到数据库、HTTP 客户端或你的进程；它只能看到你事先声明的名字、说明和参数 schema，以及你事后送回去的结果文本。

Spring AI 2 把这个循环放在 `ChatClient` 的 `ToolCallingAdvisor` 里，而不是藏在某一个 `ChatModel` 实现内部。所以本课用 mock 的 `ChatModel` 就能把循环跑通：mock 返回 tool call，框架执行真的 Java 方法，再把结果放进下一次请求。

## 你在看的对象

```text
HTTP 或 CLI → ToolChat → ChatClient → ToolCallingAdvisor → ChatModel
                                      ↘ ToolCallingManager → @Tool 方法
```

- `WeatherTools`、`CalculatorTools`、`TodoTools` 用 `@Tool` 和 `@ToolParam` 声明方法。`@Tool` 的名字默认就是方法名。
- `ToolCatalog.callbacks` 用 `ToolCallbacks.from(...)` 把这三个对象收成 `ToolCallback[]`。`@Tool` 方法和手写的 `ToolCallback` 走的是同一条执行路径；本课用注解生成 `ToolCallback`。
- `ToolChat` 构造 `ChatClient` 时 `defaultTools(...)` 把它们注册成默认工具。每次请求都会带上。
- `complete` 只取模型最终的文本。中间的 tool call 不会直接成为 HTTP 或 CLI 的输出。
- REST 在 `ChatController`：`POST /api/chat`。
- CLI 在 profile `cli` 下由 `ToolCliRunner` 启动。它关掉 Web 服务器，读 `--prompt`，打完结果就退出。

提示词在 `PromptTexts`，用户输入校验在 `TextInputs`。天气对照表在 `WeatherLookup`，四则运算在 `Arithmetic`。这些类只有静态方法，REST 和 CLI 共用 `ToolChat`。

三个工具都是本地实现，不访问外网：

| 工具 | 作用 | 参数 |
| --- | --- | --- |
| `getWeather` | 按城市返回一份桩预报。北京、上海、杭州有固定句子，其他城市也返回一句带 stub 的预报 | `city` |
| `calculate` | 加、减、乘、除两个数。除数为 0 或运算名不认识时，返回 `error: ...`，不把异常抛出请求 | `operation`（`add` / `subtract` / `multiply` / `divide`）、`left`、`right` |
| `addTodo` | 往进程内的列表加一条，返回更新后的列表 | `item` |
| `listTodos` | 列出还没完成的待办 | 无 |
| `completeTodo` | 按从 1 开始的序号完成一条 | `index` |

待办存在 `TodoTools` 这个单例里，所有请求共用，进程退出就没了。这是为了把工具调用看清楚，不是会话记忆。

## 模型什么时候会选工具

选择发生在模型侧，不发生在 `if (prompt.contains("天气"))` 这种业务分支里。

`ChatClient` 组请求时，会把每个 `ToolCallback` 的 `name`、`description` 和 `inputSchema` 放进 `ToolCallingChatOptions`。OpenAI 兼容客户端的 options 实现了这个接口，所以工具定义会跟着请求发到供应商。模型对照用户的问题和这些说明，决定：

- 直接写文本，一个工具都不调
- 调一个工具
- 在同一次响应里调多个工具

`PromptTexts.system()` 只是提醒模型「有这些能力时再用」。它不能代替工具说明。说明要写清这个方法干什么、什么时候该用；参数说明写清每个 JSON 字段是什么。模型看不到方法体。

本课的测试用 mock 把两种选择都走了一遍：直接回答，以及返回 tool call。生产环境里哪一次会调工具，取决于模型和当次的说明，不是框架帮你选的。

## 参数怎么绑到 Java 方法

模型给出的参数是一段 JSON 对象字符串，例如：

```json
{"city":"北京"}
```

```json
{"operation":"multiply","left":6,"right":7}
```

Spring AI 用 JSON 的字段名对应方法参数名，再把 JSON 类型转成参数类型：字符串对 `String`，数字对 `double` 或 `int`。没有参数的 `listTodos` 对应 `{}`。

字段名就是 Java 参数名。Spring Boot 父 POM 打开了编译器的 `-parameters`，所以名字是 `city`、`operation`，不是 `arg0`。`@ToolParam` 的 `description` 会进 schema，给模型看；它不改字段名。没标 `required = false` 的参数默认必填。

绑定失败（例如参数不是 JSON）会变成 `ToolExecutionException`。默认处理是把异常信息当作这次工具的结果文本，送回模型，而不是让整次请求失败。工具自己想让模型看到的业务错误，本课直接返回 `error: ...` 字符串，走同一条「结果回到对话」的路。

模型如果报了一个没有注册过的工具名，框架找不到 `ToolCallback`，请求失败。REST 上这是 `502`。

## 结果怎么回到对话

方法返回值先经过 `@Tool.resultConverter`，变成一段字符串，然后才进对话。默认的 `DefaultToolCallResultConverter` 把返回值编成 JSON：一句中文会多一对引号；如果返回值本身已经是合法 JSON（例如字符串 `"42"`），它会原样转发，模型看到的是 JSON 数字 `42` 而不是文本。本课的工具返回给人读的句子，所以指定了 `PlainTextToolResultConverter`：`null` 变成空字符串，其他对象用 `toString()`。模型下一轮读到的就是方法返回的那段文本。

`ToolCallingManager` 拿到这段文本之后做两件事：

1. 把模型刚才那条带 tool call 的 assistant 消息留在历史里。
2. 追加一条 `ToolResponseMessage`，里面是工具名、call id 和返回文本。

`ToolCallingAdvisor` 用这份加长的历史再请求一次模型。模型还可以继续调工具。循环停在模型给出一条没有 tool call 的响应。`call().content()` 取的是这最后一段文本，也就是 `POST /api/chat` 的 `answer` 和 CLI 打出来的那一行。

`@Tool(returnDirect = true)` 可以跳过回喂、把工具结果直接交给调用方。本课不这么做，默认 `false`，好把「结果回到对话」看完整。

一轮天气问题在 mock 里长这样：

```text
用户: 北京天气

第 1 轮请求
  system + user + 工具定义
第 1 轮响应
  tool call getWeather {"city":"北京"}

应用执行 WeatherTools.getWeather("北京")
  → 北京：22°C，晴

第 2 轮请求
  上面的历史 + assistant tool call + tool result
第 2 轮响应
  北京今天晴，22°C。
```

待办可以跨请求留下来：一次请求里的 `addTodo` 改的是同一个 `TodoTools` 实例，下一次请求的 `listTodos` 还能看见。换一个进程就清空。

## 运行

配置见仓库根目录的 README。`LLM_API_KEY` 必须有值，否则客户端起得来，但一调用就会被供应商拒绝。工具本身不需要 key。

在仓库根目录：

```bash
mvn -pl 02-tool-calling spring-boot:run
```

默认端口 `8080`，可以用 `SERVER_PORT` 改。

```bash
curl -s localhost:8080/api/chat \
  -H 'content-type: application/json' \
  -d '{"prompt":"北京今天天气怎么样？"}'
```

期望的形状：

```json
{"answer":"..."}
```

再试计算和待办：

```bash
curl -s localhost:8080/api/chat \
  -H 'content-type: application/json' \
  -d '{"prompt":"帮我算 6 乘 7"}'

curl -s localhost:8080/api/chat \
  -H 'content-type: application/json' \
  -d '{"prompt":"记一条待办：写完第二课"}'

curl -s localhost:8080/api/chat \
  -H 'content-type: application/json' \
  -d '{"prompt":"现在有哪些待办？"}'
```

最终句子由模型写。它应该先调用对应工具，再把工具返回的数字或列表写进回答。不需要工具的问题会直接回答。

`prompt` 为空白时返回 `400`，正文是 `{"error":"..."}`。模型返回空内容，或要调用一个不存在的工具时，返回 `502`。

CLI：

```bash
mvn -pl 02-tool-calling spring-boot:run \
  -Dspring-boot.run.profiles=cli \
  -Dspring-boot.run.arguments="--prompt=杭州今天天气怎么样"
```

没写 `--prompt` 时进程会失败，并说明需要 `--prompt=<text>`。

## 测试

`mvn -B verify` 不需要真实 key。`ToolCatalogTest` 直接调用 `ToolCallback`，确认 JSON 参数绑到了 Java 方法上。`ToolChatTest` 用 `ChatClient.builder(mock ChatModel)`：mock 的 `getOptions()` 必须是 `ToolCallingChatOptions`，工具才会被放进请求；第一轮返回 tool call，第二轮的 prompt 里能看到 `ToolResponseMessage`。`ChatControllerTest` 启动 Spring 上下文，用 `@MockitoBean` 替换自动配置的 `ChatModel`，base URL 指到一个不会被访问的本地端口。

```bash
mvn -pl 02-tool-calling -B verify
```
