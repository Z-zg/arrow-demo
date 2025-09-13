package org.zgq.core

import arrow.core.Either
import arrow.core.left
import arrow.core.right

/**
 * Validated特性示例（使用Either实现）
 * 
 * 注意：在Arrow 1.2.1中，Validated已被弃用，其功能已合并到Either中。
 * 本示例展示如何使用Either来实现验证逻辑，这是Arrow推荐的新方法。
 * 
 * Either可以用于：
 * - 错误处理：Left表示错误，Right表示成功
 * - 验证逻辑：虽然不能像Validated那样自动累积错误，但可以手动实现
 * - 类型安全：提供编译时类型检查
 */
object ValidatedExamples {
    
    // 定义错误类型
    sealed class ValidationError {
        data class FieldRequired(val field: String) : ValidationError()
        data class FieldTooShort(val field: String, val minLength: Int) : ValidationError()
        data class FieldTooLong(val field: String, val maxLength: Int) : ValidationError()
        data class InvalidFormat(val field: String, val format: String) : ValidationError()
        data class InvalidRange(val field: String, val min: Int, val max: Int) : ValidationError()
        data class CustomError(val message: String) : ValidationError()
    }
    
    /**
     * 创建Either实例进行验证
     */
    fun creationExamples() {
        // 使用扩展函数创建Right（成功）
        val success: Either<ValidationError, String> = "Success!".right()
        
        // 使用扩展函数创建Left（错误）
        val error: Either<ValidationError, String> = ValidationError.FieldRequired("name").left()
        
        // 使用扩展函数创建Right（成功值）
        val successValue: Either<ValidationError, Int> = 42.right()
        
        // 使用扩展函数创建Left（错误值）
        val errorValue: Either<ValidationError, Int> = ValidationError.InvalidRange("age", 0, 120).left()
        
        println("Success: $success")
        println("Error: $error")
        println("Success value: $successValue")
        println("Error value: $errorValue")
    }
    
    /**
     * Either的基本验证操作
     */
    fun basicOperations() {
        val success: Either<ValidationError, String> = "Hello".right()
        val error: Either<ValidationError, String> = ValidationError.FieldRequired("name").left()
        
        // 检查是否为Right或Left
        println("success.isRight(): ${success.isRight()}")
        println("success.isLeft(): ${success.isLeft()}")
        println("error.isRight(): ${error.isRight()}")
        println("error.isLeft(): ${error.isLeft()}")
        
        // 获取值或null
        println("success value: ${success.getOrNull()}")
        println("error value: ${error.getOrNull()}")
        
        // 获取错误或null
        println("success error: ${success.leftOrNull()}")
        println("error error: ${error.leftOrNull()}")
    }
    
    /**
     * 使用map进行Right值的转换
     */
    fun mapOperations() {
        val number: Either<ValidationError, Int> = 5.right()
        val error: Either<ValidationError, Int> = ValidationError.FieldRequired("number").left()
        
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
        val success: Either<ValidationError, String> = "Success".right()
        val error: Either<ValidationError, String> = ValidationError.FieldRequired("name").left()
        
        // mapLeft操作：只对Left值应用函数，Right值保持不变
        val mappedSuccess = success.mapLeft { ValidationError.CustomError("Mapped: $it") }
        val mappedError = error.mapLeft { ValidationError.CustomError("Mapped: $it") }
        
        println("Original success: $success")
        println("Mapped success: $mappedSuccess")
        println("Original error: $error")
        println("Mapped error: $mappedError")
    }
    
    /**
     * 使用fold进行值提取和处理
     */
    fun foldOperations() {
        val success: Either<ValidationError, String> = "Hello".right()
        val error: Either<ValidationError, String> = ValidationError.FieldRequired("name").left()
        
        // fold：提供两个函数，一个处理Left情况，一个处理Right情况
        val successResult = success.fold(
            { "Error: $it" },
            { "Success: $it" }
        )
        
        val errorResult = error.fold(
            { "Error: $it" },
            { "Success: $it" }
        )
        
        println("Success fold result: $successResult")
        println("Error fold result: $errorResult")
    }
    
    /**
     * 使用swap交换Left和Right
     */
    fun swapOperations() {
        val success: Either<ValidationError, String> = "Hello".right()
        val error: Either<ValidationError, String> = ValidationError.FieldRequired("name").left()
        
        // swap：交换Left和Right的位置
        val swappedSuccess = success.swap()
        val swappedError = error.swap()
        
        println("Original success: $success")
        println("Swapped success: $swappedSuccess")
        println("Original error: $error")
        println("Swapped error: $swappedError")
    }
    
    /**
     * 使用Either进行验证的示例
     */
    fun validationWithEither() {
        // 简单的验证函数，使用字符串作为错误类型
        fun validateName(name: String): Either<String, String> {
            return when {
                name.isBlank() -> "Name is required".left()
                name.length < 2 -> "Name too short".left()
                else -> name.right()
            }
        }
        
        fun validateAge(age: Int): Either<String, Int> {
            return when {
                age < 0 -> "Age must be positive".left()
                age > 120 -> "Age too high".left()
                else -> age.right()
            }
        }
        
        fun validateEmail(email: String): Either<String, String> {
            return when {
                email.isBlank() -> "Email is required".left()
                !email.contains("@") -> "Email must contain @".left()
                else -> email.right()
            }
        }
        
        // 成功案例：所有验证都通过
        val validName = validateName("Alice")
        val validAge = validateAge(25)
        val validEmail = validateEmail("alice@example.com")
        
        println("Valid name: $validName")
        println("Valid age: $validAge")
        println("Valid email: $validEmail")
        
        // 错误案例：单个验证失败
        val invalidName = validateName("")
        val invalidAge = validateAge(-5)
        val invalidEmail = validateEmail("invalid")
        
        println("Invalid name: $invalidName")
        println("Invalid age: $invalidAge")
        println("Invalid email: $invalidEmail")
        
        // 组合验证（快速失败模式）
        fun validateUser(name: String, age: Int, email: String): Either<String, User> {
            val nameResult = validateName(name)
            if (nameResult.isLeft()) return nameResult.leftOrNull()!!.left()
            
            val ageResult = validateAge(age)
            if (ageResult.isLeft()) return ageResult.leftOrNull()!!.left()
            
            val emailResult = validateEmail(email)
            if (emailResult.isLeft()) return emailResult.leftOrNull()!!.left()
            
            return User(
                nameResult.getOrNull()!!,
                ageResult.getOrNull()!!,
                emailResult.getOrNull()!!
            ).right()
        }
        
        val validUser = validateUser("Alice", 25, "alice@example.com")
        val invalidUser = validateUser("", -5, "invalid")
        
        println("Valid user: $validUser")
        println("Invalid user (first error): $invalidUser")
    }
    
    /**
     * 复杂的验证组合：嵌套对象验证
     */
    fun complexValidationCombination() {
        data class Address(val street: String, val city: String, val zipCode: String)
        
        fun validateStreet(street: String): Either<String, String> {
            return when {
                street.isBlank() -> "Street is required".left()
                street.length < 5 -> "Street too short".left()
                else -> street.right()
            }
        }
        
        fun validateCity(city: String): Either<String, String> {
            return when {
                city.isBlank() -> "City is required".left()
                city.length < 2 -> "City too short".left()
                else -> city.right()
            }
        }
        
        fun validateZipCode(zipCode: String): Either<String, String> {
            return when {
                zipCode.isBlank() -> "ZipCode is required".left()
                !zipCode.matches(Regex("\\d{5}")) -> "ZipCode must be 5 digits".left()
                else -> zipCode.right()
            }
        }
        
        // 使用fold进行地址验证（快速失败）
        fun validateAddress(street: String, city: String, zipCode: String): Either<String, Address> {
            val streetResult = validateStreet(street)
            if (streetResult.isLeft()) return streetResult.leftOrNull()!!.left()
            
            val cityResult = validateCity(city)
            if (cityResult.isLeft()) return cityResult.leftOrNull()!!.left()
            
            val zipResult = validateZipCode(zipCode)
            if (zipResult.isLeft()) return zipResult.leftOrNull()!!.left()
            
            return Address(
                streetResult.getOrNull()!!,
                cityResult.getOrNull()!!,
                zipResult.getOrNull()!!
            ).right()
        }
        
        // 测试完全有效的数据
        val validAddress = validateAddress("123 Main Street", "New York", "12345")
        println("Valid address: $validAddress")
        
        // 测试无效的数据（返回第一个错误）
        val invalidAddress = validateAddress("", "A", "abc")
        println("Invalid address (first error): $invalidAddress")
    }
    
    // 辅助函数：单个字段验证
    private fun validateName(name: String): Either<String, String> {
        return when {
            name.isBlank() -> "Name is required".left()
            name.length < 2 -> "Name too short".left()
            name.length > 50 -> "Name too long".left()
            else -> name.right()
        }
    }
    
    private fun validateAge(age: Int): Either<String, Int> {
        return when {
            age < 0 -> "Age must be positive".left()
            age > 120 -> "Age too high".left()
            else -> age.right()
        }
    }
    
    private fun validateEmail(email: String): Either<String, String> {
        return when {
            email.isBlank() -> "Email is required".left()
            !email.contains("@") -> "Email must contain @".left()
            !email.contains(".") -> "Email must contain .".left()
            else -> email.right()
        }
    }
    
    /**
     * 实际应用场景：表单验证
     */
    fun formValidationExample() {
        data class RegistrationForm(
            val username: String,
            val password: String,
            val email: String,
            val age: Int
        )
        
        fun validateUsername(username: String): Either<String, String> {
            return when {
                username.isBlank() -> "Username is required".left()
                username.length < 3 -> "Username too short".left()
                username.length > 20 -> "Username too long".left()
                !username.matches(Regex("[a-zA-Z0-9_]+")) -> "Username invalid format".left()
                else -> username.right()
            }
        }
        
        fun validatePassword(password: String): Either<String, String> {
            return when {
                password.isBlank() -> "Password is required".left()
                password.length < 8 -> "Password too short".left()
                !password.any { it.isUpperCase() } -> "Password needs uppercase".left()
                !password.any { it.isLowerCase() } -> "Password needs lowercase".left()
                !password.any { it.isDigit() } -> "Password needs digit".left()
                else -> password.right()
            }
        }
        
        // 使用fold进行表单验证（快速失败）
        fun validateRegistrationForm(
            username: String,
            password: String,
            email: String,
            age: Int
        ): Either<String, RegistrationForm> {
            val usernameResult = validateUsername(username)
            if (usernameResult.isLeft()) return usernameResult.leftOrNull()!!.left()
            
            val passwordResult = validatePassword(password)
            if (passwordResult.isLeft()) return passwordResult.leftOrNull()!!.left()
            
            val emailResult = validateEmail(email)
            if (emailResult.isLeft()) return emailResult.leftOrNull()!!.left()
            
            val ageResult = validateAge(age)
            if (ageResult.isLeft()) return ageResult.leftOrNull()!!.left()
            
            return RegistrationForm(
                usernameResult.getOrNull()!!,
                passwordResult.getOrNull()!!,
                emailResult.getOrNull()!!,
                ageResult.getOrNull()!!
            ).right()
        }
        
        // 测试有效的注册表单
        val validForm = validateRegistrationForm(
            "alice_123",
            "SecurePass1",
            "alice@example.com",
            25
        )
        println("Valid registration form: $validForm")
        
        // 测试无效的注册表单（返回第一个错误）
        val invalidForm = validateRegistrationForm(
            "a", // 用户名太短
            "weak", // 密码太弱
            "invalid-email", // 无效邮箱
            -5 // 无效年龄
        )
        println("Invalid registration form (first error): $invalidForm")
    }
    
    /**
     * Either验证模式的对比示例
     */
    fun eitherValidationComparison() {
        println("=== Either Validation Patterns ===")
        
        // 快速失败模式（使用fold）
        fun validateWithFold(name: String, age: Int, email: String): Either<String, User> {
            val nameResult = validateName(name)
            if (nameResult.isLeft()) return nameResult.leftOrNull()!!.left()
            
            val ageResult = validateAge(age)
            if (ageResult.isLeft()) return ageResult.leftOrNull()!!.left()
            
            val emailResult = validateEmail(email)
            if (emailResult.isLeft()) return emailResult.leftOrNull()!!.left()
            
            return User(
                nameResult.getOrNull()!!,
                ageResult.getOrNull()!!,
                emailResult.getOrNull()!!
            ).right()
        }
        
        // 手动检查模式（检查所有字段但只返回第一个错误）
        fun validateWithManualCheck(name: String, age: Int, email: String): Either<String, User> {
            val nameResult = validateName(name)
            val ageResult = validateAge(age)
            val emailResult = validateEmail(email)
            
            return when {
                nameResult.isRight() && ageResult.isRight() && emailResult.isRight() -> 
                    User(
                        nameResult.getOrNull()!!,
                        ageResult.getOrNull()!!,
                        emailResult.getOrNull()!!
                    ).right()
                nameResult.isLeft() -> nameResult.leftOrNull()!!.left()
                ageResult.isLeft() -> ageResult.leftOrNull()!!.left()
                else -> emailResult.leftOrNull()!!.left()
            }
        }
        
        // 测试数据：多个错误
        val testName = ""
        val testAge = -5
        val testEmail = "invalid"
        
        val foldResult = validateWithFold(testName, testAge, testEmail)
        val manualResult = validateWithManualCheck(testName, testAge, testEmail)
        
        println("Fold result (stops at first error): $foldResult")
        println("Manual check result (first error found): $manualResult")
        
        // 展示Either的优势
        println("\nEither provides:")
        println("- Type-safe error handling")
        println("- Composable validation with fold")
        println("- Clear success/failure semantics")
        println("- Integration with Arrow ecosystem")
    }
    
    data class User(val name: String, val age: Int, val email: String)
    
    /**
     * 演示所有示例
     */
    fun runAllExamples() {
        println("=== Either Validation Creation Examples ===")
        creationExamples()
        
        println("\n=== Basic Operations ===")
        basicOperations()
        
        println("\n=== Map Operations ===")
        mapOperations()
        
        println("\n=== MapLeft Operations ===")
        mapLeftOperations()
        
        println("\n=== Fold Operations ===")
        foldOperations()
        
        println("\n=== Swap Operations ===")
        swapOperations()
        
        println("\n=== Validation with Either ===")
        validationWithEither()
        
        println("\n=== Complex Validation Combination ===")
        complexValidationCombination()
        
        println("\n=== Form Validation Example ===")
        formValidationExample()
        
        println("\n=== Either Validation Patterns ===")
        eitherValidationComparison()
    }
}