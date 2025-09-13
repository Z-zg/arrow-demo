# KtArrow Library

[![Kotlin](https://img.shields.io/badge/kotlin-2.0.21-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Arrow](https://img.shields.io/badge/Arrow-1.2.1-orange.svg)](https://arrow-kt.io/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)]()

一个基于 Gradle 的 Kotlin Arrow 函数式编程库示例项目，展示了 Arrow 库的核心特性和最佳实践。

## 🚀 特性

### Arrow Core 数据类型
- **Option** - 类型安全的可选值处理，替代 null
- **Either** - 双值类型，优雅的错误处理
- **Validated** - 累积错误验证，适用于表单验证
- **Ior** - 包含性或，可以是左值、右值或两者
- **Nel (NonEmptyList)** - 非空列表，保证至少有一个元素
- **Eval** - 惰性求值和栈安全递归

### Arrow Fx 并发编程
- **Resource** - 自动资源管理，确保资源正确释放
- **Suspend Functions** - 协程函数组合和异步操作

### Arrow Optics 光学系统
- **Lens** - 透镜，聚焦于数据结构的特定字段
- **Prism** - 棱镜，处理联合类型的分支

## 📦 安装

### 前置要求
- JDK 21 或更高版本
- Gradle 8.5 或更高版本


### 构建项目
```bash
./gradlew build
```

### 运行测试
```bash
./gradlew test
```

### 生成测试覆盖率报告
```bash
./gradlew jacocoTestReport
```

## 🎯 快速开始

### Option - 安全的可选值处理

```kotlin
import arrow.core.*

// 创建 Option
val someValue: Option<String> = "Hello".some()
val noneValue: Option<String> = none()

// 安全的值转换
val result = someValue
    .map { it.uppercase() }
    .filter { it.length > 3 }
    .fold(
        ifEmpty = { "默认值" },
        ifSome = { "结果: $it" }
    )

println(result) // 输出: 结果: HELLO
```

### Either - 优雅的错误处理

```kotlin
import arrow.core.*

sealed class AppError {
    object NetworkError : AppError()
    data class ValidationError(val message: String) : AppError()
}

fun fetchUser(id: String): Either<AppError, User> {
    return if (id.isBlank()) {
        AppError.ValidationError("用户ID不能为空").left()
    } else {
        User(id, "张三").right()
    }
}

// 使用 Either 进行错误处理
val result = fetchUser("123")
    .map { user -> user.copy(name = user.name.uppercase()) }
    .fold(
        ifLeft = { error -> "错误: $error" },
        ifRight = { user -> "用户: ${user.name}" }
    )
```

### Resource - 自动资源管理

```kotlin
import arrow.fx.coroutines.*
import java.io.FileInputStream

suspend fun readFileContent(path: String): String {
    return resource {
        FileInputStream(path)
    } use { inputStream ->
        inputStream.readBytes().toString(Charsets.UTF_8)
    }
}
```

### Lens - 不可变数据更新

```kotlin
import arrow.optics.*

@optics
data class Address(val street: String, val city: String) {
    companion object
}

@optics  
data class Person(val name: String, val address: Address) {
    companion object
}

// 使用 Lens 更新嵌套数据
val person = Person("张三", Address("长安街", "北京"))
val updatedPerson = Person.address.city.set(person, "上海")

println(updatedPerson) // Person(name=张三, address=Address(street=长安街, city=上海))
```

## 📚 项目结构

```
src/
├── main/kotlin/org/zgq/
│   ├── core/           # Arrow Core 特性示例
│   │   ├── OptionExamples.kt
│   │   ├── EitherExamples.kt
│   │   ├── ValidatedExamples.kt
│   │   ├── IorExamples.kt
│   │   ├── NelExamples.kt
│   │   └── EvalExamples.kt
│   ├── fx/             # Arrow Fx 特性示例
│   │   ├── ResourceExamples.kt
│   │   └── SuspendFunctionExamples.kt
│   └── optics/         # Arrow Optics 特性示例
│       ├── LensExamples.kt
│       └── PrismExamples.kt
└── test/kotlin/org/zgq/
    ├── core/           # Core 特性测试
    ├── fx/             # Fx 特性测试
    └── optics/         # Optics 特性测试
```

## 🔧 配置说明

### Gradle 配置优化

项目针对中国大陆网络环境进行了优化：

1. **腾讯云镜像源** - `gradle-wrapper.properties` 使用腾讯云下载源
2. **并行构建** - 启用 Gradle 并行构建和缓存
3. **JVM 优化** - 配置合适的 JVM 参数

### 依赖版本

- Kotlin: 2.0.21
- Arrow: 1.2.1
- JUnit 5: 5.10.1
- Kotest: 5.8.0
- MockK: 1.13.8

## 🧪 测试

项目包含完整的测试套件，覆盖所有 Arrow 特性：

### 运行所有测试
```bash
./gradlew test
```

### 运行特定模块测试
```bash
# Core 模块测试
./gradlew test --tests "*core*"

# Fx 模块测试  
./gradlew test --tests "*fx*"

# Optics 模块测试
./gradlew test --tests "*optics*"
```

### 测试覆盖率
```bash
./gradlew jacocoTestReport
open build/reports/jacoco/test/html/index.html
```

## 📖 学习资源

### Arrow 官方文档
- [Arrow 官网](https://arrow-kt.io/)
- [Arrow Core 文档](https://arrow-kt.io/docs/core/)
- [Arrow Fx 文档](https://arrow-kt.io/docs/fx/)
- [Arrow Optics 文档](https://arrow-kt.io/docs/optics/)

### 推荐阅读
- [函数式编程入门](https://arrow-kt.io/docs/patterns/glossary/)
- [Kotlin 协程与 Arrow Fx](https://arrow-kt.io/docs/fx/coroutines/)
- [光学系统详解](https://arrow-kt.io/docs/optics/lens/)

## 🤝 贡献指南

我们欢迎所有形式的贡献！

### 如何贡献

1. **Fork** 本仓库
2. 创建你的特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交你的更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开一个 **Pull Request**

### 代码规范

- 遵循 [Kotlin 官方代码风格](https://kotlinlang.org/docs/coding-conventions.html)
- 使用 Detekt 进行静态代码分析
- 确保所有测试通过
- 新功能需要包含相应的测试用例
- 提交信息使用中文，格式清晰

### 开发环境设置

1. 安装 JDK 21
2. 克隆仓库并导入到 IntelliJ IDEA
3. 运行 `./gradlew build` 确保环境正常
4. 运行 `./gradlew detekt` 检查代码质量

## 📄 许可证

本项目采用 [Apache License 2.0](LICENSE) 许可证。


**Happy Coding with Arrow! 🏹**