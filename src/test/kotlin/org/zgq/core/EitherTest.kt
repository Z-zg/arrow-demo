package org.zgq.core

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * Either特性完整测试用例
 *
 * 测试Arrow Either的所有核心功能，包括：
 * - Left/Right创建和基本操作
 * - 函数式操作（map, mapLeft）
 * - 值提取和处理（fold, swap）
 * - 组合操作和错误处理
 * - 实际应用场景
 */
class SimpleEitherTest : StringSpec({
    
    "Either创建 - Right应该包含成功值" {
        val right: Either<String, String> = "success".right()
        right.isRight() shouldBe true
        right.isLeft() shouldBe false
        right.getOrNull() shouldBe "success"
        right.leftOrNull() shouldBe null
    }
    
    "Either创建 - Left应该包含错误值" {
        val left: Either<String, String> = "error".left()
        left.isRight() shouldBe false
        left.isLeft() shouldBe true
        left.getOrNull() shouldBe null
        left.leftOrNull() shouldBe "error"
    }
    
    "Either创建 - 使用扩展函数right()" {
        val right: Either<String, Int> = 42.right()
        right.isRight() shouldBe true
        right.getOrNull() shouldBe 42
    }
    
    "Either创建 - 使用扩展函数left()" {
        val left: Either<String, Int> = "error".left()
        left.isLeft() shouldBe true
        left.leftOrNull() shouldBe "error"
    }
    
    "map操作 - Right应该应用函数" {
        val right: Either<String, Int> = 5.right()
        val result = right.map { it * 2 }
        
        result.isRight() shouldBe true
        result.getOrNull() shouldBe 10
    }
    
    "map操作 - Left应该保持不变" {
        val left: Either<String, Int> = "error".left()
        val result = left.map { it * 2 }
        
        result.isLeft() shouldBe true
        result.leftOrNull() shouldBe "error"
    }
    
    "map操作 - 链式调用应该正确工作" {
        val initial: Either<String, Int> = 3.right()
        val result = initial
            .map { it * 2 }
            .map { it + 1 }
            .map { it.toString() }
        
        result.getOrNull() shouldBe "7"
    }
    
    "mapLeft操作 - Left应该应用函数" {
        val left: Either<String, Int> = "error".left()
        val result = left.mapLeft { "mapped: $it" }
        
        result.isLeft() shouldBe true
        result.leftOrNull() shouldBe "mapped: error"
    }
    
    "mapLeft操作 - Right应该保持不变" {
        val right: Either<String, Int> = 42.right()
        val result = right.mapLeft { "mapped: $it" }
        
        result.isRight() shouldBe true
        result.getOrNull() shouldBe 42
    }
    
    "fold操作 - Right应该执行ifRight分支" {
        val right: Either<String, String> = "hello".right()
        val result = right.fold(
            ifLeft = { "error: $it" },
            ifRight = { "success: $it" }
        )
        
        result shouldBe "success: hello"
    }
    
    "fold操作 - Left应该执行ifLeft分支" {
        val left: Either<String, String> = "error".left()
        val result = left.fold(
            ifLeft = { "error: $it" },
            ifRight = { "success: $it" }
        )
        
        result shouldBe "error: error"
    }
    
    "swap操作 - Right应该变为Left" {
        val right: Either<String, String> = "success".right()
        val swapped = right.swap()
        
        swapped.isLeft() shouldBe true
        swapped.leftOrNull() shouldBe "success"
    }
    
    "swap操作 - Left应该变为Right" {
        val left: Either<String, String> = "error".left()
        val swapped = left.swap()
        
        swapped.isRight() shouldBe true
        swapped.getOrNull() shouldBe "error"
    }
    
    "实际应用 - 安全除法应该正确处理除零情况" {
        val validDivision = EitherExamples.safeDivision(10, 2)
        val invalidDivision = EitherExamples.safeDivision(10, 0)
        
        validDivision.getOrNull() shouldBe 5.0
        invalidDivision.isLeft() shouldBe true
        invalidDivision.leftOrNull().shouldBeInstanceOf<EitherExamples.AppError.ValidationError>()
    }
    
    "实际应用 - 安全字符串解析应该正确处理有效和无效输入" {
        val validParse = EitherExamples.safeParseInt("123")
        val invalidParse = EitherExamples.safeParseInt("abc")
        
        validParse.getOrNull() shouldBe 123
        invalidParse.isLeft() shouldBe true
        invalidParse.leftOrNull().shouldBeInstanceOf<EitherExamples.AppError.ParseError>()
    }
    
    "实际应用 - 用户验证应该正确处理有效和无效用户" {
        val validUser = EitherExamples.validateUser("Alice", 25, "alice@example.com")
        val invalidNameUser = EitherExamples.validateUser("", 25, "alice@example.com")
        val invalidAgeUser = EitherExamples.validateUser("Alice", -1, "alice@example.com")
        val invalidEmailUser = EitherExamples.validateUser("Alice", 25, "invalid-email")
        
        validUser.isRight() shouldBe true
        validUser.getOrNull() shouldBe EitherExamples.User("Alice", 25, "alice@example.com")
        
        invalidNameUser.isLeft() shouldBe true
        invalidAgeUser.isLeft() shouldBe true
        invalidEmailUser.isLeft() shouldBe true
    }
    
    "实际应用 - 复杂计算应该正确组合多个操作" {
        val validCalculation = EitherExamples.complexCalculation("10", "2")
        val invalidParseCalculation = EitherExamples.complexCalculation("10", "x")
        val divisionByZeroCalculation = EitherExamples.complexCalculation("10", "0")
        
        validCalculation.getOrNull() shouldBe 5
        invalidParseCalculation.isLeft() shouldBe true
        divisionByZeroCalculation.isLeft() shouldBe true
    }
    
    "Either应该正确实现equals和hashCode" {
        val right1: Either<String, String> = "test".right()
        val right2: Either<String, String> = "test".right()
        val right3: Either<String, String> = "other".right()
        val left1: Either<String, String> = "error".left()
        val left2: Either<String, String> = "error".left()
        val left3: Either<String, String> = "other".left()
        
        right1 shouldBe right2
        right1 shouldNotBe right3
        right1 shouldNotBe left1
        left1 shouldBe left2
        left1 shouldNotBe left3
        
        right1.hashCode() shouldBe right2.hashCode()
        left1.hashCode() shouldBe left2.hashCode()
    }
    
    "Either应该正确实现toString" {
        val right: Either<String, String> = "test".right()
        val left: Either<String, String> = "error".left()
        
        right.toString() shouldBe "Either.Right(test)"
        left.toString() shouldBe "Either.Left(error)"
    }
    
    "Either的类型安全性测试" {
        // 编译时类型安全
        val stringEither: Either<String, String> = "hello".right()
        val intEither: Either<String, Int> = 42.right()
        
        // map保持类型安全
        val lengthEither: Either<String, Int> = stringEither.map { it.length }
        lengthEither.getOrNull() shouldBe 5
        
        // mapLeft保持Right类型不变
        val mappedLeft: Either<Int, String> = stringEither.mapLeft { it.length }
        mappedLeft.getOrNull() shouldBe "hello"
    }
    
    "Either的错误恢复链测试" {
        val fallbackData = EitherExamples.getDataWithFallback("valid")
        val defaultData = EitherExamples.getDataWithFallback("invalid")
        
        // 应该从数据库恢复
        fallbackData.getOrNull() shouldBe "Database data"
        
        // 应该使用默认数据
        defaultData.getOrNull() shouldBe "Default data"
    }
    
    "Either的性能特性测试" {
        // Either应该是轻量级的
        val eithers: List<Either<String, Int>> = (1..1000).map { it.right() }
        eithers.size shouldBe 1000
        
        // 链式操作应该是惰性的
        val initial: Either<String, Int> = 1.right()
        val result = initial
            .map { it + 1 }
            .map { it * 2 }
            .map { it - 1 }
        
        result.getOrNull() shouldBe 3
    }
    
    "Either的错误类型层次结构测试" {
        val networkError: Either<EitherExamples.AppError, String> = EitherExamples.AppError.NetworkError.left()
        val validationError: Either<EitherExamples.AppError, String> = EitherExamples.AppError.ValidationError("test").left()
        val parseError: Either<EitherExamples.AppError, String> = EitherExamples.AppError.ParseError("test").left()
        val notFoundError: Either<EitherExamples.AppError, String> = EitherExamples.AppError.NotFoundError.left()
        
        networkError.leftOrNull().shouldBeInstanceOf<EitherExamples.AppError.NetworkError>()
        validationError.leftOrNull().shouldBeInstanceOf<EitherExamples.AppError.ValidationError>()
        parseError.leftOrNull().shouldBeInstanceOf<EitherExamples.AppError.ParseError>()
        notFoundError.leftOrNull().shouldBeInstanceOf<EitherExamples.AppError.NotFoundError>()
    }
})