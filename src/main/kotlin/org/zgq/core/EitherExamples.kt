package org.zgq.core

import arrow.core.Either
import arrow.core.left
import arrow.core.right

/**
 * Either特性示例
 * 
 * Either是Arrow中用于处理错误和成功两种情况的函数式数据类型，
 * 它提供了一种类型安全的方式来处理可能失败的操作，避免异常处理。
 * Either有两个子类型：Left（通常表示错误）和Right（通常表示成功值）。
 */
object EitherExamples {
    
    // 定义错误类型
    sealed class AppError {
        object NetworkError : AppError()
        data class ValidationError(val message: String) : AppError()
        data class ParseError(val input: String) : AppError()
        object NotFoundError : AppError()
    }
    
    /**
     * 创建Either实例的不同方式
     */
    fun creationExamples() {
        // 使用扩展函数创建Right
        val success: Either<AppError, String> = "Success!".right()
        
        // 使用扩展函数创建Left
        val error: Either<AppError, String> = AppError.NetworkError.left()
        
        // 使用扩展函数创建Right
        val successValue: Either<AppError, Int> = 42.right()
        
        // 使用扩展函数创建Left
        val errorValue: Either<AppError, Int> = AppError.ValidationError("Invalid input").left()
        
        println("Success: $success")
        println("Error: $error")
        println("Success value: $successValue")
        println("Error value: $errorValue")
    }
    
    /**
     * Either的基本操作
     */
    fun basicOperations() {
        val success: Either<AppError, String> = "Hello".right()
        val error: Either<AppError, String> = AppError.NetworkError.left()
        
        // 检查是否为Right或Left
        println("success.isRight(): ${success.isRight()}")
        println("success.isLeft(): ${success.isLeft()}")
        println("error.isRight(): ${error.isRight()}")
        println("error.isLeft(): ${error.isLeft()}")
        
        // 获取值或null
        println("success.getOrNull(): ${success.getOrNull()}")
        println("error.getOrNull(): ${error.getOrNull()}")
        
        // 获取错误或null
        println("success.leftOrNull(): ${success.leftOrNull()}")
        println("error.leftOrNull(): ${error.leftOrNull()}")
    }
    
    /**
     * 使用map进行Right值的转换
     */
    fun mapOperations() {
        val number: Either<AppError, Int> = 5.right()
        val error: Either<AppError, Int> = AppError.NetworkError.left()
        
        // map操作：只对Right值应用函数，Left值保持不变
        val doubled = number.map { it * 2 }
        val errorDoubled = error.map { it * 2 }
        
        println("Original: $number")
        println("Doubled: $doubled")
        println("Error doubled: $errorDoubled")
        
        // 链式map操作
        val result = number
            .map { it * 2 }
            .map { it + 1 }
            .map { "Result: $it" }
        
        println("Chain result: $result")
    }
    
    /**
     * 使用mapLeft进行Left值的转换
     */
    fun mapLeftOperations() {
        val success: Either<AppError, String> = "Success".right()
        val error: Either<AppError, String> = AppError.ValidationError("Invalid").left()
        
        // mapLeft操作：只对Left值应用函数，Right值保持不变
        val mappedSuccess = success.mapLeft { AppError.NetworkError }
        val mappedError = error.mapLeft { AppError.NetworkError }
        
        println("Original success: $success")
        println("Mapped success: $mappedSuccess")
        println("Original error: $error")
        println("Mapped error: $mappedError")
    }
    
    /**
     * 使用fold进行单子操作模拟
     */
    fun flatMapOperations() {
        val number: Either<AppError, Int> = 10.right()
        
        // 使用fold模拟flatMap操作
        val result = number.fold(
            ifLeft = { it.left() },
            ifRight = { n ->
                if (n > 0) (n * 2).right() else AppError.ValidationError("Number must be positive").left()
            }
        )
        
        println("FlatMap result: $result")
        
        // 链式操作使用fold
        val chainResult = number.fold(
            ifLeft = { it.left() },
            ifRight = { n ->
                if (n > 0) {
                    val step1 = n
                    if (step1 < 20) {
                        val step2 = step1 * 3
                        if (step2 % 2 == 0) "Even: $step2".right() else AppError.ValidationError("Must be even").left()
                    } else {
                        AppError.ValidationError("Too large").left()
                    }
                } else {
                    AppError.ValidationError("Must be positive").left()
                }
            }
        )
        
        println("Chain flatMap result: $chainResult")
    }
    
    /**
     * 使用fold进行值提取和处理
     */
    fun foldOperations() {
        val success: Either<AppError, String> = "Hello".right()
        val error: Either<AppError, String> = AppError.NetworkError.left()
        
        // fold：提供两个函数，一个处理Left情况，一个处理Right情况
        val successResult = success.fold(
            ifLeft = { "Error: $it" },
            ifRight = { "Success: $it" }
        )
        
        val errorResult = error.fold(
            ifLeft = { "Error: $it" },
            ifRight = { "Success: $it" }
        )
        
        println("Success fold result: $successResult")
        println("Error fold result: $errorResult")
    }
    
    /**
     * 使用swap交换Left和Right
     */
    fun swapOperations() {
        val success: Either<AppError, String> = "Hello".right()
        val error: Either<AppError, String> = AppError.NetworkError.left()
        
        // swap：交换Left和Right的位置
        val swappedSuccess = success.swap()
        val swappedError = error.swap()
        
        println("Original success: $success")
        println("Swapped success: $swappedSuccess")
        println("Original error: $error")
        println("Swapped error: $swappedError")
    }
    
    /**
     * 使用fold进行错误恢复
     */
    fun recoverOperations() {
        val error: Either<AppError, String> = AppError.NetworkError.left()
        val success: Either<AppError, String> = "Success".right()
        
        // 使用fold进行错误恢复：当Either为Left时，提供一个恢复函数
        val recoveredError = error.fold(
            ifLeft = { "Recovered from error: $it" },
            ifRight = { it }
        )
        val recoveredSuccess = success.fold(
            ifLeft = { "This won't be called" },
            ifRight = { it }
        )
        
        println("Original error: $error")
        println("Recovered error: $recoveredError")
        println("Original success: $success")
        println("Recovered success: $recoveredSuccess")
    }
    
    /**
     * Either的组合操作
     */
    fun combinationOperations() {
        val firstName: Either<AppError, String> = "John".right()
        val lastName: Either<AppError, String> = "Doe".right()
        val errorName: Either<AppError, String> = AppError.ValidationError("Invalid name").left()
        
        // 使用fold组合两个Either
        val fullName = firstName.fold(
            ifLeft = { it.left() },
            ifRight = { first ->
                lastName.map { last -> "$first $last" }
            }
        )
        
        val errorFullName = firstName.fold(
            ifLeft = { it.left() },
            ifRight = { first ->
                errorName.map { last -> "$first $last" }
            }
        )
        
        println("Full name: $fullName")
        println("Error full name: $errorFullName")
    }
    
    /**
     * 实际应用场景：安全的除法操作
     */
    fun safeDivision(dividend: Int, divisor: Int): Either<AppError, Double> {
        return if (divisor != 0) {
            (dividend.toDouble() / divisor).right()
        } else {
            AppError.ValidationError("Division by zero").left()
        }
    }
    
    /**
     * 实际应用场景：安全的字符串解析
     */
    fun safeParseInt(str: String): Either<AppError, Int> {
        return try {
            str.toInt().right()
        } catch (e: NumberFormatException) {
            AppError.ParseError(str).left()
        }
    }
    
    /**
     * 实际应用场景：用户验证
     */
    fun validateUser(name: String, age: Int, email: String): Either<AppError, User> {
        return when {
            name.isBlank() -> AppError.ValidationError("Name cannot be blank").left()
            age < 0 -> AppError.ValidationError("Age cannot be negative").left()
            !email.contains("@") -> AppError.ValidationError("Invalid email format").left()
            else -> User(name, age, email).right()
        }
    }
    
    data class User(val name: String, val age: Int, val email: String)
    
    /**
     * 组合多个可能失败的操作
     */
    fun complexCalculation(a: String, b: String): Either<AppError, Int> {
        return safeParseInt(a).fold(
            ifLeft = { it.left() },
            ifRight = { numA ->
                safeParseInt(b).fold(
                    ifLeft = { it.left() },
                    ifRight = { numB ->
                        safeDivision(numA, numB).map { result ->
                            result.toInt()
                        }
                    }
                )
            }
        )
    }
    
    /**
     * 错误处理链：尝试多种方式获取数据
     */
    fun getDataWithFallback(id: String): Either<AppError, String> {
        return getFromCache(id).fold(
            ifLeft = { 
                getFromDatabase(id).fold(
                    ifLeft = { "Default data".right() },
                    ifRight = { it.right() }
                )
            },
            ifRight = { it.right() }
        )
    }
    
    private fun getFromCache(id: String): Either<AppError, String> {
        // 模拟缓存查找失败
        return AppError.NotFoundError.left()
    }
    
    private fun getFromDatabase(id: String): Either<AppError, String> {
        // 模拟数据库查找
        return if (id == "valid") {
            "Database data".right()
        } else {
            AppError.NotFoundError.left()
        }
    }
    
    /**
     * 演示所有示例
     */
    fun runAllExamples() {
        println("=== Either Creation Examples ===")
        creationExamples()
        
        println("\n=== Basic Operations ===")
        basicOperations()
        
        println("\n=== Map Operations ===")
        mapOperations()
        
        println("\n=== MapLeft Operations ===")
        mapLeftOperations()
        
        println("\n=== FlatMap Operations ===")
        flatMapOperations()
        
        println("\n=== Fold Operations ===")
        foldOperations()
        
        println("\n=== Swap Operations ===")
        swapOperations()
        
        println("\n=== Recover Operations ===")
        recoverOperations()
        
        println("\n=== Combination Operations ===")
        combinationOperations()
        
        println("\n=== Practical Examples ===")
        println("Safe division 10/2: ${safeDivision(10, 2)}")
        println("Safe division 10/0: ${safeDivision(10, 0)}")
        println("Parse '123': ${safeParseInt("123")}")
        println("Parse 'abc': ${safeParseInt("abc")}")
        println("Validate user: ${validateUser("Alice", 25, "alice@example.com")}")
        println("Validate invalid user: ${validateUser("", -1, "invalid-email")}")
        println("Complex calculation ('10', '2'): ${complexCalculation("10", "2")}")
        println("Complex calculation ('10', '0'): ${complexCalculation("10", "0")}")
        println("Get data with fallback 'valid': ${getDataWithFallback("valid")}")
        println("Get data with fallback 'invalid': ${getDataWithFallback("invalid")}")
    }
}