# telegram-gpt

一个本地运行的 Telegram GPT Bot，当前版本为 V0.1。

## 已实现

- Telegram Bot API 长轮询收取文本消息
- 调用 OpenAI Responses API
- 以 Telegram `chatId` 关联连续对话
- `/start`、`/help` 和 `/new` 命令
- 自动拆分超过 Telegram 单条消息限制的回复
- 纯内存会话状态；重启程序后会话会清空

代码按 `telegram`、`ai`、`config` 分层，后续可在 `ai` 层扩展 Web Search、图片/文件、语音和 MCP / Function Calling，而不需要改 Telegram 收发流程。

## 环境要求

- JDK 21+
- Maven 3.9+
- 一个 Telegram Bot Token
- 一个可用的 OpenAI API Key

## 配置与启动

如果使用 macOS Homebrew 安装的 JDK 21，可以先设置：

```bash
export JAVA_HOME="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
export PATH="/opt/homebrew/opt/openjdk@21/bin:/opt/homebrew/bin:$PATH"
```

在项目目录执行：

```bash
export TELEGRAM_BOT_TOKEN="你的 Telegram Bot Token"
export OPENAI_API_KEY="你的 OpenAI API Key"

./start.sh
```

启动脚本会自动检查 Java、Maven 和两个环境变量，不会读取或保存密钥文件。

也可以先打包再启动：

```bash
mvn clean package
java -jar target/telegram-gpt-0.1.0-SNAPSHOT.jar
```

默认模型是 `gpt-4.1-mini`，可以通过环境变量替换：

```bash
export OPENAI_MODEL="gpt-4.1-mini"
```

启动后，在 Telegram 中向 Bot 发送文字即可。发送 `/new` 会清除该 Telegram 聊天的上下文。

## 常用可选配置

```bash
export TELEGRAM_POLL_TIMEOUT_SECONDS="30"
export OPENAI_BASE_URL="https://api.openai.com/v1"
export TELEGRAM_API_BASE_URL="https://api.telegram.org"
```

不要把 Token 或 API Key 写入 `application.yml`、源码或 Git。项目已将 `.env` 排除在版本控制之外。

## 验证

```bash
mvn clean test
```
