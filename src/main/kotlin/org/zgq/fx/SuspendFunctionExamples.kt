package org.zgq.fx

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import arrow.fx.coroutines.parMap
import arrow.fx.coroutines.parTraverse
import arrow.fx.coroutines.raceN
import kotlinx.coroutines.delay
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeout

/**
 * Arrow Fx suspend函数组合示例
 * 
 * 展示如何使用Arrow Fx进行异步操作组合、并行处理和错误处理
 */
object SuspendFunctionExamples {
    
    /**
     * 基础suspend函数链式调用示例
     */
    suspend fun basicSuspendChainExample(): String {
        return fetchUserData(1)
            .let { userData -> processUserData(userData) }
            .let { processedData -> saveProcessedData(processedData) }
    }
    
    /**
     * 使用Either进行错误处理的suspend函数组合
     */
    suspend fun suspendWithErrorHandlingExample(userId: Int): Either<AppError, String> {
        return try {
            val userData = fetchUserDataSafely(userId)
            when (userData) {
                is Either.Left -> userData
                is Either.Right -> {
                    val processed = processUserDataSafely(userData.value)
                    when (processed) {
                        is Either.Left -> processed
                        is Either.Right -> saveProcessedDataSafely(processed.value)
                    }
                }
            }
        } catch (e: Exception) {
            AppError.UnexpectedError(e.message ?: "Unknown error").left()
        }
    }
    
    /**
     * 并行处理多个异步操作示例
     */
    suspend fun parallelProcessingExample(): List<String> {
        val userIds = listOf(1, 2, 3, 4, 5)
        
        // 使用parTraverse并行处理所有用户数据
        return userIds.parTraverse { userId ->
            fetchAndProcessUser(userId)
        }
    }
    
    /**
     * 使用parMap进行并行映射操作
     */
    suspend fun parallelMappingExample(): List<ProcessedUserData> {
        val userDataList = listOf(
            UserData(1, "Alice", "alice@example.com"),
            UserData(2, "Bob", "bob@example.com"),
            UserData(3, "Charlie", "charlie@example.com")
        )
        
        return userDataList.parMap { userData ->
            enhanceUserData(userData)
        }
    }
    
    /**
     * 竞争执行示例 - 使用最快完成的结果
     */
    suspend fun raceExecutionExample(): String {
        return raceN(
            { slowDataSource() },
            { fastDataSource() },
            { mediumDataSource() }
        ).fold(
            { "Slow source: $it" },
            { "Fast source: $it" },
            { "Medium source: $it" }
        )
    }
    
    /**
     * 超时处理示例
     */
    suspend fun timeoutHandlingExample(): Either<AppError, String> {
        return try {
            val result = withTimeout(5000) {
                longRunningOperation()
            }
            result.right()
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            AppError.TimeoutError("Operation timed out").left()
        } catch (e: Exception) {
            AppError.UnexpectedError(e.message ?: "Unknown error").left()
        }
    }
    
    /**
     * 复杂的异步工作流示例
     */
    suspend fun complexAsyncWorkflowExample(userId: Int): Either<AppError, WorkflowResult> {
        return coroutineScope {
            try {
                // 并行获取用户数据和权限信息
                val userDataDeferred = async { fetchUserDataSafely(userId) }
                val permissionsDeferred = async { fetchUserPermissions(userId) }
                
                val userData = userDataDeferred.await()
                val permissions = permissionsDeferred.await()
                
                when {
                    userData is Either.Left -> userData
                    permissions is Either.Left -> permissions
                    else -> {
                        val user = (userData as Either.Right).value
                        val perms = (permissions as Either.Right).value
                        
                        // 验证权限
                        if (!perms.canAccess) {
                            AppError.PermissionDenied("User does not have access").left()
                        } else {
                            // 处理数据并保存
                            val processed = processUserDataSafely(user)
                            when (processed) {
                                is Either.Left -> processed
                                is Either.Right -> {
                                    val saved = saveProcessedDataSafely(processed.value)
                                    when (saved) {
                                        is Either.Left -> saved
                                        is Either.Right -> WorkflowResult(
                                            userId = userId,
                                            processedData = processed.value,
                                            savedAt = System.currentTimeMillis()
                                        ).right()
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                AppError.UnexpectedError(e.message ?: "Workflow failed").left()
            }
        }
    }
    
    /**
     * 重试机制示例
     */
    suspend fun retryMechanismExample(): Either<AppError, String> {
        var attempts = 0
        val maxAttempts = 3
        
        while (attempts < maxAttempts) {
            attempts++
            try {
                val result = unreliableOperation()
                return result.right()
            } catch (e: Exception) {
                if (attempts >= maxAttempts) {
                    return AppError.MaxRetriesExceeded("Failed after $maxAttempts attempts: ${e.message}").left()
                }
                delay(1000L * attempts) // 指数退避
            }
        }
        
        return AppError.UnexpectedError("Should not reach here").left()
    }
    
    /**
     * 流水线处理示例
     */
    suspend fun pipelineProcessingExample(data: List<String>): List<String> {
        return data
            .parMap { item -> step1Processing(item) }
            .parMap { item -> step2Processing(item) }
            .parMap { item -> step3Processing(item) }
    }
    
    // 辅助函数和数据类
    
    private suspend fun fetchUserData(userId: Int): UserData {
        delay(100L) // 模拟网络延迟
        return UserData(userId, "User$userId", "user$userId@example.com")
    }
    
    private suspend fun processUserData(userData: UserData): ProcessedUserData {
        delay(50L) // 模拟处理时间
        return ProcessedUserData(
            userData.id,
            userData.name.uppercase(),
            userData.email,
            System.currentTimeMillis()
        )
    }
    
    private suspend fun saveProcessedData(data: ProcessedUserData): String {
        delay(30L) // 模拟保存时间
        return "Saved user ${data.id} successfully"
    }
    
    private suspend fun fetchUserDataSafely(userId: Int): Either<AppError, UserData> {
        return try {
            delay(100L)
            if (userId <= 0) {
                AppError.InvalidInput("User ID must be positive").left()
            } else {
                UserData(userId, "User$userId", "user$userId@example.com").right()
            }
        } catch (e: Exception) {
            AppError.NetworkError(e.message ?: "Network error").left()
        }
    }
    
    private suspend fun processUserDataSafely(userData: UserData): Either<AppError, ProcessedUserData> {
        return try {
            delay(50L)
            ProcessedUserData(
                userData.id,
                userData.name.uppercase(),
                userData.email,
                System.currentTimeMillis()
            ).right()
        } catch (e: Exception) {
            AppError.ProcessingError(e.message ?: "Processing error").left()
        }
    }
    
    private suspend fun saveProcessedDataSafely(data: ProcessedUserData): Either<AppError, String> {
        return try {
            delay(30L)
            "Saved user ${data.id} successfully".right()
        } catch (e: Exception) {
            AppError.DatabaseError(e.message ?: "Database error").left()
        }
    }
    
    private suspend fun fetchAndProcessUser(userId: Int): String {
        delay(100L + (userId * 10L)) // 不同的延迟模拟不同的处理时间
        return "Processed user $userId"
    }
    
    private suspend fun enhanceUserData(userData: UserData): ProcessedUserData {
        delay(50L)
        return ProcessedUserData(
            userData.id,
            "${userData.name} (Enhanced)",
            userData.email,
            System.currentTimeMillis()
        )
    }
    
    private suspend fun slowDataSource(): String {
        delay(1000L)
        return "Slow data"
    }
    
    private suspend fun fastDataSource(): String {
        delay(100L)
        return "Fast data"
    }
    
    private suspend fun mediumDataSource(): String {
        delay(500L)
        return "Medium data"
    }
    
    private suspend fun longRunningOperation(): String {
        delay(3000L)
        return "Long operation completed"
    }
    
    private suspend fun fetchUserPermissions(userId: Int): Either<AppError, UserPermissions> {
        delay(80L)
        return UserPermissions(userId, canAccess = userId % 2 == 1).right()
    }
    
    private suspend fun unreliableOperation(): String {
        delay(100L)
        if (Math.random() < 0.7) { // 70% 失败率
            throw RuntimeException("Operation failed randomly")
        }
        return "Operation succeeded"
    }
    
    private suspend fun step1Processing(item: String): String {
        delay(50L)
        return "Step1($item)"
    }
    
    private suspend fun step2Processing(item: String): String {
        delay(30L)
        return "Step2($item)"
    }
    
    private suspend fun step3Processing(item: String): String {
        delay(20L)
        return "Step3($item)"
    }
}

/**
 * 用户数据类
 */
data class UserData(
    val id: Int,
    val name: String,
    val email: String
)

/**
 * 处理后的用户数据类
 */
data class ProcessedUserData(
    val id: Int,
    val name: String,
    val email: String,
    val processedAt: Long
)

/**
 * 用户权限类
 */
data class UserPermissions(
    val userId: Int,
    val canAccess: Boolean
)

/**
 * 工作流结果类
 */
data class WorkflowResult(
    val userId: Int,
    val processedData: ProcessedUserData,
    val savedAt: Long
)

/**
 * 应用错误类型
 */
sealed class AppError {
    data class InvalidInput(val message: String) : AppError()
    data class NetworkError(val message: String) : AppError()
    data class ProcessingError(val message: String) : AppError()
    data class DatabaseError(val message: String) : AppError()
    data class PermissionDenied(val message: String) : AppError()
    data class TimeoutError(val message: String) : AppError()
    data class MaxRetriesExceeded(val message: String) : AppError()
    data class UnexpectedError(val message: String) : AppError()
}