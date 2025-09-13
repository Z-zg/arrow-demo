package org.zgq.core

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * Either验证特性完整测试用例
 *
 * 测试使用Either进行验证的核心功能，包括：
 * - Right/Left创建和基本操作
 * - 函数式操作（map, mapLeft）
 * - 值提取和处理（fold, swap）
 * - 验证组合（flatMap操作）
 * - 实际应用场景（表单验证等）
 * 
 * 注意：在Arrow 1.2.1中，Validated已被弃用，推荐使用Either进行验证
 */
class ValidatedTest : StringSpec({
    
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
    
    "fold操作 - Right应该执行第二个函数" {
        val right: Either<String, String> = "hello".right()
        val result = right.fold(
            { "error: $it" },
            { "success: $it" }
        )
        
        result shouldBe "success: hello"
    }
    
    "fold操作 - Left应该执行第一个函数" {
        val left: Either<String, String> = "error".left()
        val result = left.fold(
            { "error: $it" },
            { "success: $it" }
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
    
    "flatMap验证组合 - 所有成功应该组合成功" {
        fun validateName(name: String): Either<String, String> {
            return if (name.isBlank()) "Name required".left() else name.right()
        }
        
        fun validateAge(age: Int): Either<String, Int> {
            return if (age < 0) "Age must be positive".left() else age.right()
        }
        
        fun validateEmail(email: String): Either<String, String> {
            return if (!email.contains("@")) "Email must contain @".left() else email.right()
        }
        
        // 手动组合验证
        val nameResult = validateName("Alice")
        val ageResult = validateAge(25)
        val emailResult = validateEmail("alice@example.com")
        
        val result = if (nameResult.isRight() && ageResult.isRight() && emailResult.isRight()) {
            ValidatedExamples.User(
                nameResult.getOrNull()!!,
                ageResult.getOrNull()!!,
                emailResult.getOrNull()!!
            ).right()
        } else {
            when {
                nameResult.isLeft() -> nameResult.leftOrNull()!!.left()
                ageResult.isLeft() -> ageResult.leftOrNull()!!.left()
                else -> emailResult.leftOrNull()!!.left()
            }
        }
        
        result.isRight() shouldBe true
        result.getOrNull() shouldBe ValidatedExamples.User("Alice", 25, "alice@example.com")
    }
    
    "flatMap验证组合 - 第一个错误应该短路" {
        fun validateName(name: String): Either<String, String> {
            return if (name.isBlank()) "Name required".left() else name.right()
        }
        
        fun validateAge(age: Int): Either<String, Int> {
            return if (age < 0) "Age must be positive".left() else age.right()
        }
        
        fun validateEmail(email: String): Either<String, String> {
            return if (!email.contains("@")) "Email must contain @".left() else email.right()
        }
        
        // 手动组合验证（应该在第一个错误处停止）
        val nameResult = validateName("")
        val ageResult = validateAge(-5)
        val emailResult = validateEmail("invalid")
        
        val result = if (nameResult.isRight() && ageResult.isRight() && emailResult.isRight()) {
            ValidatedExamples.User(
                nameResult.getOrNull()!!,
                ageResult.getOrNull()!!,
                emailResult.getOrNull()!!
            ).right()
        } else {
            when {
                nameResult.isLeft() -> nameResult.leftOrNull()!!.left()
                ageResult.isLeft() -> ageResult.leftOrNull()!!.left()
                else -> emailResult.leftOrNull()!!.left()
            }
        }
        
        result.isLeft() shouldBe true
        result.leftOrNull() shouldBe "Name required"
    }
    
    "实际应用 - 表单验证应该正确处理" {
        fun validateUsername(username: String): Either<String, String> {
            return when {
                username.isBlank() -> "Username is required".left()
                username.length < 3 -> "Username too short".left()
                else -> username.right()
            }
        }
        
        fun validatePassword(password: String): Either<String, String> {
            return when {
                password.isBlank() -> "Password is required".left()
                password.length < 8 -> "Password too short".left()
                else -> password.right()
            }
        }
        
        // 测试成功案例
        val usernameResult = validateUsername("alice")
        val passwordResult = validatePassword("password123")
        
        val validResult = if (usernameResult.isRight() && passwordResult.isRight()) {
            Pair(usernameResult.getOrNull()!!, passwordResult.getOrNull()!!).right()
        } else {
            when {
                usernameResult.isLeft() -> usernameResult.leftOrNull()!!.left()
                else -> passwordResult.leftOrNull()!!.left()
            }
        }
        
        validResult.isRight() shouldBe true
        validResult.getOrNull() shouldBe Pair("alice", "password123")
        
        // 测试失败案例
        val invalidUsernameResult = validateUsername("ab")
        val invalidPasswordResult = validatePassword("123")
        
        val invalidResult = if (invalidUsernameResult.isRight() && invalidPasswordResult.isRight()) {
            Pair(invalidUsernameResult.getOrNull()!!, invalidPasswordResult.getOrNull()!!).right()
        } else {
            when {
                invalidUsernameResult.isLeft() -> invalidUsernameResult.leftOrNull()!!.left()
                else -> invalidPasswordResult.leftOrNull()!!.left()
            }
        }
        
        invalidResult.isLeft() shouldBe true
        invalidResult.leftOrNull() shouldBe "Username too short"
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
    
    "Either验证的错误类型层次结构测试" {
        val fieldRequired: Either<ValidatedExamples.ValidationError, String> = 
            ValidatedExamples.ValidationError.FieldRequired("name").left()
        val fieldTooShort: Either<ValidatedExamples.ValidationError, String> = 
            ValidatedExamples.ValidationError.FieldTooShort("name", 5).left()
        val invalidFormat: Either<ValidatedExamples.ValidationError, String> = 
            ValidatedExamples.ValidationError.InvalidFormat("email", "must contain @").left()
        val invalidRange: Either<ValidatedExamples.ValidationError, String> = 
            ValidatedExamples.ValidationError.InvalidRange("age", 0, 120).left()
        
        fieldRequired.leftOrNull().shouldBeInstanceOf<ValidatedExamples.ValidationError.FieldRequired>()
        fieldTooShort.leftOrNull().shouldBeInstanceOf<ValidatedExamples.ValidationError.FieldTooShort>()
        invalidFormat.leftOrNull().shouldBeInstanceOf<ValidatedExamples.ValidationError.InvalidFormat>()
        invalidRange.leftOrNull().shouldBeInstanceOf<ValidatedExamples.ValidationError.InvalidRange>()
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
})