package org.zgq.core

import arrow.core.Option
import arrow.core.Some
import arrow.core.none
import arrow.core.some

/**
 * Option特性示例
 * 
 * Option是Arrow中用于处理可能为空值的函数式数据类型，
 * 它提供了一种类型安全的方式来处理null值，避免NullPointerException。
 */
object OptionExamples {
    
    /**
     * 创建Option实例的不同方式
     */
    fun creationExamples() {
        // 使用Some包装非空值
        val someValue: Option<String> = Some("Hello")
        
        // 使用扩展函数创建Some
        val someNumber: Option<Int> = 42.some()
        
        // 创建None表示空值
        val noneValue: Option<String> = none()
        
        // 从可能为null的值创建Option
        val fromNullable: Option<String> = Option.fromNullable("可能为null的值")
        val fromNull: Option<String> = Option.fromNullable(null)
        
        println("Some value: $someValue")
        println("Some number: $someNumber") 
        println("None value: $noneValue")
        println("From nullable: $fromNullable")
        println("From null: $fromNull")
    }
    
    /**
     * Option的基本操作
     */
    fun basicOperations() {
        val name: Option<String> = "Alice".some()
        val empty: Option<String> = none()
        
        // 检查是否有值
        println("name.isSome(): ${name.isSome()}")
        println("name.isNone(): ${name.isNone()}")
        println("empty.isSome(): ${empty.isSome()}")
        println("empty.isNone(): ${empty.isNone()}")
        
        // 获取值或提供默认值
        println("name.fold { \"Unknown\" } { it }: ${name.fold({ "Unknown" }) { it }}")
        println("empty.fold { \"Unknown\" } { it }: ${empty.fold({ "Unknown" }) { it }}")
        
        // 转换为nullable
        println("name.getOrNull(): ${name.getOrNull()}")
        println("empty.getOrNull(): ${empty.getOrNull()}")
    }
    
    /**
     * 使用map进行函数式转换
     */
    fun mapOperations() {
        val number: Option<Int> = 5.some()
        val empty: Option<Int> = none()
        
        // map操作：如果有值则应用函数，否则保持None
        val doubled = number.map { it * 2 }
        val emptyDoubled = empty.map { it * 2 }
        
        println("Original: $number")
        println("Doubled: $doubled")
        println("Empty doubled: $emptyDoubled")
        
        // 链式map操作
        val result = number
            .map { it * 2 }
            .map { it + 1 }
            .map { "Result: $it" }
        
        println("Chain result: $result")
    }
    
    /**
     * 使用flatMap进行单子操作
     */
    fun flatMapOperations() {
        val number: Option<Int> = 10.some()
        
        // flatMap用于避免嵌套的Option
        val result = number.flatMap { n ->
            if (n > 0) (n * 2).some() else none()
        }
        
        println("FlatMap result: $result")
        
        // 链式flatMap操作
        val chainResult = number
            .flatMap { if (it > 0) it.some() else none() }
            .flatMap { if (it < 20) (it * 3).some() else none() }
            .flatMap { if (it % 2 == 0) "Even: $it".some() else none() }
        
        println("Chain flatMap result: $chainResult")
    }
    
    /**
     * 使用filter进行条件过滤
     */
    fun filterOperations() {
        val evenNumber: Option<Int> = 8.some()
        val oddNumber: Option<Int> = 7.some()
        
        // filter：如果值满足条件则保持Some，否则变为None
        val filteredEven = evenNumber.filter { it % 2 == 0 }
        val filteredOdd = oddNumber.filter { it % 2 == 0 }
        
        println("Even number filtered (even): $filteredEven")
        println("Odd number filtered (even): $filteredOdd")
    }
    
    /**
     * 使用fold进行值提取和处理
     */
    fun foldOperations() {
        val value: Option<String> = "Hello".some()
        val empty: Option<String> = none()
        
        // fold：提供两个函数，一个处理None情况，一个处理Some情况
        val valueResult = value.fold(
            ifEmpty = { "No value" },
            ifSome = { "Value: $it" }
        )
        
        val emptyResult = empty.fold(
            ifEmpty = { "No value" },
            ifSome = { "Value: $it" }
        )
        
        println("Value fold result: $valueResult")
        println("Empty fold result: $emptyResult")
    }
    
    /**
     * Option的组合操作
     */
    fun combinationOperations() {
        val firstName: Option<String> = "John".some()
        val lastName: Option<String> = "Doe".some()
        val missingName: Option<String> = none()
        
        // 使用flatMap和map组合两个Option
        val fullName = firstName.flatMap { first ->
            lastName.map { last -> "$first $last" }
        }
        val incompleteName = firstName.flatMap { first ->
            missingName.map { last -> "$first $last" }
        }
        
        println("Full name: $fullName")
        println("Incomplete name: $incompleteName")
    }
    
    /**
     * 实际应用场景：安全的除法操作
     */
    fun safeDivision(dividend: Int, divisor: Int): Option<Double> {
        return if (divisor != 0) {
            (dividend.toDouble() / divisor).some()
        } else {
            none()
        }
    }
    
    /**
     * 实际应用场景：安全的字符串解析
     */
    fun safeParseInt(str: String): Option<Int> {
        return try {
            str.toInt().some()
        } catch (e: NumberFormatException) {
            none()
        }
    }
    
    /**
     * 组合多个可能失败的操作
     */
    fun complexCalculation(a: String, b: String, c: String): Option<Int> {
        return safeParseInt(a).flatMap { numA ->
            safeParseInt(b).flatMap { numB ->
                safeParseInt(c).map { numC ->
                    numA + numB + numC
                }
            }
        }
    }
    
    /**
     * 演示所有示例
     */
    fun runAllExamples() {
        println("=== Option Creation Examples ===")
        creationExamples()
        
        println("\n=== Basic Operations ===")
        basicOperations()
        
        println("\n=== Map Operations ===")
        mapOperations()
        
        println("\n=== FlatMap Operations ===")
        flatMapOperations()
        
        println("\n=== Filter Operations ===")
        filterOperations()
        
        println("\n=== Fold Operations ===")
        foldOperations()
        
        println("\n=== Combination Operations ===")
        combinationOperations()
        
        println("\n=== Practical Examples ===")
        println("Safe division 10/2: ${safeDivision(10, 2)}")
        println("Safe division 10/0: ${safeDivision(10, 0)}")
        println("Parse '123': ${safeParseInt("123")}")
        println("Parse 'abc': ${safeParseInt("abc")}")
        println("Complex calculation ('1', '2', '3'): ${complexCalculation("1", "2", "3")}")
        println("Complex calculation ('1', 'x', '3'): ${complexCalculation("1", "x", "3")}")
    }
}