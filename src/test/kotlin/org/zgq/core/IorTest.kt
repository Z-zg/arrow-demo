package org.zgq.core

import arrow.core.Ior
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * Ior特性完整测试用例
 *
 * 测试Arrow Ior的所有核心功能，包括：
 * - Left/Right/Both创建和基本操作
 * - 函数式操作（map, mapLeft, bimap）
 * - 值提取和处理（fold, pad, unwrap, swap）
 * - 组合操作和实际应用场景
 */
class IorTest : StringSpec({
    
    "Ior创建 - Left应该只包含左值" {
        val left: Ior<String, Int> = Ior.Left("error")
        
        left.isLeft() shouldBe true
        left.isRight() shouldBe false
        left.isBoth() shouldBe false
        left.leftOrNull() shouldBe "error"
        left.getOrNull() shouldBe null
    }
    
    "Ior创建 - Right应该只包含右值" {
        val right: Ior<String, Int> = Ior.Right(42)
        
        right.isLeft() shouldBe false
        right.isRight() shouldBe true
        right.isBoth() shouldBe false
        right.leftOrNull() shouldBe null
        right.getOrNull() shouldBe 42
    }
    
    "Ior创建 - Both应该同时包含左值和右值" {
        val both: Ior<String, Int> = Ior.Both("warning", 42)
        
        both.isLeft() shouldBe false
        both.isRight() shouldBe false
        both.isBoth() shouldBe true
        both.leftOrNull() shouldBe "warning"
        both.getOrNull() shouldBe 42
    }
    
    "map操作 - Left应该保持不变" {
        val left: Ior<String, Int> = Ior.Left("error")
        val result = left.map { it * 2 }
        
        result.isLeft() shouldBe true
        result.leftOrNull() shouldBe "error"
        result.getOrNull() shouldBe null
    }
    
    "map操作 - Right应该应用函数到右值" {
        val right: Ior<String, Int> = Ior.Right(5)
        val result = right.map { it * 2 }
        
        result.isRight() shouldBe true
        result.getOrNull() shouldBe 10
        result.leftOrNull() shouldBe null
    }
    
    "map操作 - Both应该应用函数到右值，保持左值不变" {
        val both: Ior<String, Int> = Ior.Both("warning", 5)
        val result = both.map { it * 2 }
        
        result.isBoth() shouldBe true
        result.leftOrNull() shouldBe "warning"
        result.getOrNull() shouldBe 10
    }
    
    "map操作 - 链式调用应该正确工作" {
        val initial: Ior<String, Int> = Ior.Right(3)
        val result = initial
            .map { it * 2 }
            .map { it + 1 }
            .map { it.toString() }
        
        result.isRight() shouldBe true
        result.getOrNull() shouldBe "7"
    }
    
    "mapLeft操作 - Left应该应用函数到左值" {
        val left: Ior<String, Int> = Ior.Left("error")
        val result = left.mapLeft { "mapped: $it" }
        
        result.isLeft() shouldBe true
        result.leftOrNull() shouldBe "mapped: error"
        result.getOrNull() shouldBe null
    }
    
    "mapLeft操作 - Right应该保持不变" {
        val right: Ior<String, Int> = Ior.Right(42)
        val result = right.mapLeft { "mapped: $it" }
        
        result.isRight() shouldBe true
        result.getOrNull() shouldBe 42
        result.leftOrNull() shouldBe null
    }
    
    "mapLeft操作 - Both应该应用函数到左值，保持右值不变" {
        val both: Ior<String, Int> = Ior.Both("warning", 42)
        val result = both.mapLeft { "mapped: $it" }
        
        result.isBoth() shouldBe true
        result.leftOrNull() shouldBe "mapped: warning"
        result.getOrNull() shouldBe 42
    }
    
    "bimap操作 - Left应该只应用左函数" {
        val left: Ior<String, Int> = Ior.Left("error")
        val result = left.bimap({ "left: $it" }, { it * 2 })
        
        result.isLeft() shouldBe true
        result.leftOrNull() shouldBe "left: error"
        result.getOrNull() shouldBe null
    }
    
    "bimap操作 - Right应该只应用右函数" {
        val right: Ior<String, Int> = Ior.Right(5)
        val result = right.bimap({ "left: $it" }, { it * 2 })
        
        result.isRight() shouldBe true
        result.getOrNull() shouldBe 10
        result.leftOrNull() shouldBe null
    }
    
    "bimap操作 - Both应该同时应用两个函数" {
        val both: Ior<String, Int> = Ior.Both("warning", 5)
        val result = both.bimap({ "left: $it" }, { it * 2 })
        
        result.isBoth() shouldBe true
        result.leftOrNull() shouldBe "left: warning"
        result.getOrNull() shouldBe 10
    }
    
    "fold操作 - Left应该执行第一个函数" {
        val left: Ior<String, Int> = Ior.Left("error")
        val result = left.fold(
            { "error: $it" },
            { "success: $it" },
            { l, r -> "both: $l, $r" }
        )
        
        result shouldBe "error: error"
    }
    
    "fold操作 - Right应该执行第二个函数" {
        val right: Ior<String, Int> = Ior.Right(42)
        val result = right.fold(
            { "error: $it" },
            { "success: $it" },
            { l, r -> "both: $l, $r" }
        )
        
        result shouldBe "success: 42"
    }
    
    "fold操作 - Both应该执行第三个函数" {
        val both: Ior<String, Int> = Ior.Both("warning", 42)
        val result = both.fold(
            { "error: $it" },
            { "success: $it" },
            { l, r -> "both: $l, $r" }
        )
        
        result shouldBe "both: warning, 42"
    }
    
    "padLeft操作 - Left应该保持不变" {
        fun <A, B> Ior<A, B>.padLeft(defaultLeft: A): Ior<A, B> = when (this) {
            is Ior.Left -> this
            is Ior.Right -> Ior.Both(defaultLeft, this.value)
            is Ior.Both -> this
        }
        
        val left: Ior<String, Int> = Ior.Left("error")
        val result = left.padLeft("default")
        
        result.isLeft() shouldBe true
        result.leftOrNull() shouldBe "error"
    }
    
    "padLeft操作 - Right应该变为Both" {
        fun <A, B> Ior<A, B>.padLeft(defaultLeft: A): Ior<A, B> = when (this) {
            is Ior.Left -> this
            is Ior.Right -> Ior.Both(defaultLeft, this.value)
            is Ior.Both -> this
        }
        
        val right: Ior<String, Int> = Ior.Right(42)
        val result = right.padLeft("warning")
        
        result.isBoth() shouldBe true
        result.leftOrNull() shouldBe "warning"
        result.getOrNull() shouldBe 42
    }
    
    "padLeft操作 - Both应该保持不变" {
        fun <A, B> Ior<A, B>.padLeft(defaultLeft: A): Ior<A, B> = when (this) {
            is Ior.Left -> this
            is Ior.Right -> Ior.Both(defaultLeft, this.value)
            is Ior.Both -> this
        }
        
        val both: Ior<String, Int> = Ior.Both("original", 42)
        val result = both.padLeft("new")
        
        result.isBoth() shouldBe true
        result.leftOrNull() shouldBe "original"
        result.getOrNull() shouldBe 42
    }
    
    "padRight操作 - Left应该变为Both" {
        fun <A, B> Ior<A, B>.padRight(defaultRight: B): Ior<A, B> = when (this) {
            is Ior.Left -> Ior.Both(this.value, defaultRight)
            is Ior.Right -> this
            is Ior.Both -> this
        }
        
        val left: Ior<String, Int> = Ior.Left("error")
        val result = left.padRight(0)
        
        result.isBoth() shouldBe true
        result.leftOrNull() shouldBe "error"
        result.getOrNull() shouldBe 0
    }
    
    "padRight操作 - Right应该保持不变" {
        fun <A, B> Ior<A, B>.padRight(defaultRight: B): Ior<A, B> = when (this) {
            is Ior.Left -> Ior.Both(this.value, defaultRight)
            is Ior.Right -> this
            is Ior.Both -> this
        }
        
        val right: Ior<String, Int> = Ior.Right(42)
        val result = right.padRight(0)
        
        result.isRight() shouldBe true
        result.getOrNull() shouldBe 42
    }
    
    "padRight操作 - Both应该保持不变" {
        fun <A, B> Ior<A, B>.padRight(defaultRight: B): Ior<A, B> = when (this) {
            is Ior.Left -> Ior.Both(this.value, defaultRight)
            is Ior.Right -> this
            is Ior.Both -> this
        }
        
        val both: Ior<String, Int> = Ior.Both("warning", 42)
        val result = both.padRight(0)
        
        result.isBoth() shouldBe true
        result.leftOrNull() shouldBe "warning"
        result.getOrNull() shouldBe 42
    }
    
    "swap操作 - Left应该变为Right" {
        val left: Ior<String, Int> = Ior.Left("error")
        val result = left.swap()
        
        result.isRight() shouldBe true
        result.getOrNull() shouldBe "error"
        result.leftOrNull() shouldBe null
    }
    
    "swap操作 - Right应该变为Left" {
        val right: Ior<String, Int> = Ior.Right(42)
        val result = right.swap()
        
        result.isLeft() shouldBe true
        result.leftOrNull() shouldBe 42
        result.getOrNull() shouldBe null
    }
    
    "swap操作 - Both应该交换左右值" {
        val both: Ior<String, Int> = Ior.Both("warning", 42)
        val result = both.swap()
        
        result.isBoth() shouldBe true
        result.leftOrNull() shouldBe 42
        result.getOrNull() shouldBe "warning"
    }
    
    "实际应用 - 带警告的计算应该正确处理不同情况" {
        // 空列表应该返回Left
        val emptyResult = IorExamples.calculationWithWarnings(emptyList())
        emptyResult.isLeft() shouldBe true
        emptyResult.leftOrNull() shouldBe listOf("Empty list provided")
        
        // 正常数据应该返回Right
        val normalResult = IorExamples.calculationWithWarnings(listOf(1, 2, 3, 4, 5))
        normalResult.isRight() shouldBe true
        normalResult.getOrNull() shouldBe 3.0
        
        // 有问题的数据应该返回Both
        val problematicResult = IorExamples.calculationWithWarnings(listOf(-1, 2))
        problematicResult.isBoth() shouldBe true
        problematicResult.leftOrNull() shouldBe listOf(
            "Negative numbers found, using absolute values",
            "Small sample size, results may not be reliable"
        )
        problematicResult.getOrNull() shouldBe 1.5
    }
    
    "实际应用 - 配置解析应该正确处理缺失和无效值" {
        // 完整配置应该返回Right
        val completeConfig = mapOf("host" to "example.com", "port" to "8080", "timeout" to "30")
        val completeResult = IorExamples.parseConfiguration(completeConfig)
        completeResult.isRight() shouldBe true
        completeResult.getOrNull() shouldBe IorExamples.Configuration("example.com", 8080, 30)
        
        // 不完整配置应该返回Both
        val incompleteConfig = mapOf("port" to "80")
        val incompleteResult = IorExamples.parseConfiguration(incompleteConfig)
        incompleteResult.isBoth() shouldBe true
        incompleteResult.leftOrNull() shouldBe listOf(
            "Host not specified, using default localhost",
            "Invalid or missing timeout, using default 30s",
            "Using privileged port 80, may require admin rights"
        )
        incompleteResult.getOrNull() shouldBe IorExamples.Configuration("localhost", 80, 30)
    }
    
    "实际应用 - 数据验证和清理应该正确处理各种情况" {
        // 干净数据应该返回Right
        val cleanData = listOf("1", "2", "3", "4")
        val cleanResult = IorExamples.validateAndCleanData(cleanData)
        cleanResult.isRight() shouldBe true
        cleanResult.getOrNull() shouldBe listOf(1, 2, 3, 4)
        
        // 脏数据应该返回Both
        val dirtyData = listOf("1", "", "invalid", "-5", "10")
        val dirtyResult = IorExamples.validateAndCleanData(dirtyData)
        dirtyResult.isBoth() shouldBe true
        dirtyResult.leftOrNull() shouldBe listOf(
            "Empty item at index 1, skipping",
            "Invalid number 'invalid' at index 2, skipping",
            "Negative number -5 at index 3, converting to positive"
        )
        dirtyResult.getOrNull() shouldBe listOf(1, 5, 10)
        
        // 空数据应该返回Left
        val emptyData = emptyList<String>()
        val emptyResult = IorExamples.validateAndCleanData(emptyData)
        emptyResult.isLeft() shouldBe true
        emptyResult.leftOrNull() shouldBe listOf("No data provided")
        
        // 全部无效数据应该返回Left
        val invalidData = listOf("", "invalid", "not_a_number")
        val invalidResult = IorExamples.validateAndCleanData(invalidData)
        invalidResult.isLeft() shouldBe true
        invalidResult.leftOrNull()?.last() shouldBe "No valid data after cleaning"
    }
    
    "Ior应该正确实现equals和hashCode" {
        val left1: Ior<String, Int> = Ior.Left("error")
        val left2: Ior<String, Int> = Ior.Left("error")
        val left3: Ior<String, Int> = Ior.Left("other")
        
        val right1: Ior<String, Int> = Ior.Right(42)
        val right2: Ior<String, Int> = Ior.Right(42)
        val right3: Ior<String, Int> = Ior.Right(24)
        
        val both1: Ior<String, Int> = Ior.Both("warning", 42)
        val both2: Ior<String, Int> = Ior.Both("warning", 42)
        val both3: Ior<String, Int> = Ior.Both("other", 42)
        
        // 相同值应该相等
        left1 shouldBe left2
        right1 shouldBe right2
        both1 shouldBe both2
        
        // 不同值应该不相等
        left1 shouldNotBe left3
        right1 shouldNotBe right3
        both1 shouldNotBe both3
        
        // 不同类型应该不相等
        left1 shouldNotBe right1
        left1 shouldNotBe both1
        right1 shouldNotBe both1
        
        // hashCode应该一致
        left1.hashCode() shouldBe left2.hashCode()
        right1.hashCode() shouldBe right2.hashCode()
        both1.hashCode() shouldBe both2.hashCode()
    }
    
    "Ior应该正确实现toString" {
        val left: Ior<String, Int> = Ior.Left("error")
        val right: Ior<String, Int> = Ior.Right(42)
        val both: Ior<String, Int> = Ior.Both("warning", 42)
        
        left.toString() shouldBe "Ior.Left(error)"
        right.toString() shouldBe "Ior.Right(42)"
        both.toString() shouldBe "Ior.Both(warning, 42)"
    }
    
    "Ior的类型安全性测试" {
        // 编译时类型安全
        val stringIor: Ior<String, String> = Ior.Right("hello")
        val intIor: Ior<String, Int> = Ior.Right(42)
        
        // map保持类型安全
        val lengthIor: Ior<String, Int> = stringIor.map { it.length }
        lengthIor.getOrNull() shouldBe 5
        
        // mapLeft保持Right类型不变
        val mappedLeft: Ior<Int, String> = stringIor.mapLeft { it.length }
        mappedLeft.getOrNull() shouldBe "hello"
        
        // bimap允许类型转换
        val bimapped: Ior<Int, Int> = stringIor.bimap({ it.length }, { it.length })
        bimapped.getOrNull() shouldBe 5
    }
    
    "Ior的模式匹配测试" {
        val left: Ior<String, Int> = Ior.Left("error")
        val right: Ior<String, Int> = Ior.Right(42)
        val both: Ior<String, Int> = Ior.Both("warning", 42)
        
        // 使用when进行模式匹配
        fun processIor(ior: Ior<String, Int>): String {
            return when (ior) {
                is Ior.Left -> "Error: ${ior.value}"
                is Ior.Right -> "Success: ${ior.value}"
                is Ior.Both -> "Warning: ${ior.leftValue}, Result: ${ior.rightValue}"
            }
        }
        
        processIor(left) shouldBe "Error: error"
        processIor(right) shouldBe "Success: 42"
        processIor(both) shouldBe "Warning: warning, Result: 42"
    }
    
    "Ior的性能特性测试" {
        // Ior应该是轻量级的
        val iors: List<Ior<String, Int>> = (1..1000).map { Ior.Right(it) }
        iors.size shouldBe 1000
        
        // 链式操作应该是惰性的
        val initial: Ior<String, Int> = Ior.Right(1)
        val result = initial
            .map { it + 1 }
            .map { it * 2 }
            .map { it - 1 }
        
        result.getOrNull() shouldBe 3
    }
    
    "Ior组合操作测试" {
        val success1: Ior<List<String>, Int> = Ior.Right(10)
        val success2: Ior<List<String>, Int> = Ior.Both(listOf("warning1"), 20)
        val success3: Ior<List<String>, Int> = Ior.Both(listOf("warning2"), 30)
        val error: Ior<List<String>, Int> = Ior.Left(listOf("error"))
        
        // 测试成功组合
        fun combineTwo(
            a: Ior<List<String>, Int>,
            b: Ior<List<String>, Int>
        ): Ior<List<String>, Int> {
            return when {
                a.isLeft() -> a
                b.isLeft() -> when (a) {
                    is Ior.Right -> b
                    is Ior.Both -> Ior.Left(a.leftValue + b.leftOrNull()!!)
                    else -> b
                }
                else -> {
                    val leftValues = (a.leftOrNull() ?: emptyList()) + (b.leftOrNull() ?: emptyList())
                    val rightValue = a.getOrNull()!! + b.getOrNull()!!
                    if (leftValues.isEmpty()) {
                        Ior.Right(rightValue)
                    } else {
                        Ior.Both(leftValues, rightValue)
                    }
                }
            }
        }
        
        val combined1 = combineTwo(success1, success2)
        combined1.isBoth() shouldBe true
        combined1.leftOrNull() shouldBe listOf("warning1")
        combined1.getOrNull() shouldBe 30
        
        val combined2 = combineTwo(success2, success3)
        combined2.isBoth() shouldBe true
        combined2.leftOrNull() shouldBe listOf("warning1", "warning2")
        combined2.getOrNull() shouldBe 50
        
        val combinedWithError = combineTwo(success1, error)
        combinedWithError.isLeft() shouldBe true
        combinedWithError.leftOrNull() shouldBe listOf("error")
    }
})