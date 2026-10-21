# agent-playground

给 Java / Spring 开发者用的 agent 练习仓库。每一课是一个独立的 Maven 模块，按编号往后加。现在有 `01-hello-llm` 和 `02-tool-calling`。

## 技术栈

- Java 21
- Spring Boot 4.1.1
- Spring AI 2.0.1，版本由 `spring-ai-bom` 管理

当前稳定版 Spring AI 2.0.x 只支持 Spring Boot 4.0 和 4.1，所以这个仓库用 Boot 4.1.1。Spring AI 1.1.x 才是还能跑在 Boot 3 上的维护线；本仓库跟当前稳定版走。

模型接入用的是 OpenAI 兼容客户端（`spring-ai-starter-model-openai`）。换 `base-url`、API key 和模型名，就可以指向 OpenAI、DeepSeek、通义千问 DashScope 的兼容接口，或者本机 Ollama。

## 路线图

| 模块 | 主题 | 状态 |
| --- | --- | --- |
| `01-hello-llm` | 把一段 prompt 发给模型，拿回文本；再把回答映射成 Java record | 本课 |
| `02-tool-calling` | 让模型调用 Java 方法 | 本课 |
| `03-rag` | 检索增强生成 | 计划中 |
| `04-memory` | 对话记忆 | 计划中 |
| `05-mcp-server` | 把能力暴露成 MCP server | 计划中 |
| `06-multi-agent` | 多个 agent 协作 | 计划中 |

后面的课会各自成为一个模块，加进根 `pom.xml` 的 `<modules>`。编号继续往后排。

## 环境

- JDK 21
- Maven 3.8+

在仓库根目录执行：

```bash
mvn -B verify
```

测试用 mock 的 `ChatModel`，不需要真实 API key，也不会访问外网。GitHub Actions 跑的是同一条命令。

## 配置模型

Key 只放在环境变量里。不要写进 `application.yml`，不要提交 `.env`。仓库里的 `.env.example` 是空模板。

| 环境变量 | 作用 | 默认值 |
| --- | --- | --- |
| `LLM_API_KEY` | 供应商的 API key。Ollama 可以填任意非空字符串，例如 `ollama` | 无，调用模型前必须设置 |
| `LLM_BASE_URL` | OpenAI 兼容接口的 base URL，要带 `/v1` | `https://api.openai.com/v1` |
| `LLM_MODEL` | 模型名 | `gpt-4o-mini` |

也兼容 Spring Boot 自己的变量名：`SPRING_AI_OPENAI_API_KEY`、`SPRING_AI_OPENAI_BASE_URL`、`SPRING_AI_OPENAI_CHAT_MODEL`。同名时上面的 `LLM_*` 优先。

```bash
cp .env.example .env
# 编辑 .env 后：
set -a && source .env && set +a
```

各供应商示例：

```bash
# OpenAI
export LLM_API_KEY=sk-...
export LLM_BASE_URL=https://api.openai.com/v1
export LLM_MODEL=gpt-4o-mini

# DeepSeek
export LLM_API_KEY=sk-...
export LLM_BASE_URL=https://api.deepseek.com/v1
export LLM_MODEL=deepseek-chat

# 通义千问 / DashScope 兼容模式
export LLM_API_KEY=sk-...
export LLM_BASE_URL=https://dashscope.aliyuncs.com/compatible-mode/v1
export LLM_MODEL=qwen-plus

# 本机 Ollama 的 OpenAI 兼容接口
export LLM_API_KEY=ollama
export LLM_BASE_URL=http://localhost:11434/v1
export LLM_MODEL=llama3.2
```

Spring AI 2 走官方 OpenAI Java SDK。base URL 本身要包含 `/v1`，SDK 再请求 `/chat/completions`。不要把 `/chat/completions` 写进 `LLM_BASE_URL`。OpenAI 上也可以把 `LLM_MODEL` 换成 `gpt-5-mini`，那是 Spring AI 2.0 自带的默认模型名；其他供应商必须换成它们自己的模型 id。

## 运行第一课

先看 [01-hello-llm/README.md](01-hello-llm/README.md)。在仓库根目录：

```bash
# REST
mvn -pl 01-hello-llm spring-boot:run

# CLI，跑完一次调用就退出
mvn -pl 01-hello-llm spring-boot:run \
  -Dspring-boot.run.profiles=cli \
  -Dspring-boot.run.arguments="--prompt=用一句话解释 ChatClient"
```

REST 起来之后：

```bash
curl -s localhost:8080/api/chat \
  -H 'content-type: application/json' \
  -d '{"prompt":"用一句话解释 ChatClient"}'

curl -s localhost:8080/api/chat/structured \
  -H 'content-type: application/json' \
  -d '{"topic":"ChatClient"}'
```

## 运行第二课

先看 [02-tool-calling/README.md](02-tool-calling/README.md)。环境变量和第一课相同。在仓库根目录：

```bash
mvn -pl 02-tool-calling spring-boot:run
```

```bash
curl -s localhost:8080/api/chat \
  -H 'content-type: application/json' \
  -d '{"prompt":"北京今天天气怎么样？"}'
```

CLI 同样用 `cli` profile，跑完一次就退出：

```bash
mvn -pl 02-tool-calling spring-boot:run \
  -Dspring-boot.run.profiles=cli \
  -Dspring-boot.run.arguments="--prompt=帮我算 6 乘 7"
```
