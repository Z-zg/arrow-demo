package org.zgq.fx

import arrow.fx.coroutines.Resource
import arrow.fx.coroutines.resource
import kotlinx.coroutines.delay
import java.io.Closeable
import java.io.FileWriter

/**
 * Arrow Fx Resource资源管理示例
 * 
 * Resource提供了安全的资源管理，确保资源在使用后正确释放，
 * 即使在异常情况下也能保证资源清理。
 */
object ResourceExamples {
    
    /**
     * 基础Resource创建和使用示例
     */
    suspend fun basicResourceExample(): String {
        // 创建一个简单的Resource
        val stringResource = resource(
            acquire = {
                object : Closeable {
                    val value = "Hello Resource"
                    override fun close() {
                        println("Resource closed: $value")
                    }
                }
            },
            release = { resource, _ ->
                resource.close()
            }
        )
        
        // 使用Resource
        return stringResource.use { resource ->
            resource.value
        }
    }
    
    /**
     * 文件资源管理示例
     */
    suspend fun fileResourceExample(filename: String, content: String): Unit {
        val fileResource = resource(
            acquire = { FileWriter(filename) },
            release = { writer, _ -> writer.close() }
        )
        
        fileResource.use { writer ->
            writer.write(content)
            writer.flush()
        }
    }
    
    /**
     * 模拟数据库连接资源管理示例
     */
    suspend fun databaseResourceExample(): List<String> {
        val connectionResource = resource(
            acquire = { MockConnection() },
            release = { connection, _ -> connection.close() }
        )
        
        return connectionResource.use { connection ->
            connection.query("SELECT name FROM users")
        }
    }
    
    /**
     * 自定义Resource创建示例
     */
    suspend fun customResourceExample(): String {
        val customResource = resource(
            acquire = {
                val resource = CustomResource("Custom Value")
                resource.initialize()
                resource
            },
            release = { resource, _ ->
                resource.cleanup()
            }
        )
        
        return customResource.use { resource ->
            resource.getValue()
        }
    }
    
    /**
     * Resource组合示例
     */
    suspend fun resourceCompositionExample(): String {
        val resource1 = resource(
            acquire = { CustomResource("Resource 1").also { it.initialize() } },
            release = { it, _ -> it.cleanup() }
        )
        
        val resource2 = resource(
            acquire = { CustomResource("Resource 2").also { it.initialize() } },
            release = { it, _ -> it.cleanup() }
        )
        
        // 组合多个Resource
        return resource1.zip(resource2).use { pair ->
            "${pair.first.getValue()} + ${pair.second.getValue()}"
        }
    }
    
    /**
     * Resource异常处理示例
     */
    suspend fun resourceExceptionHandlingExample(): Result<String> {
        val riskyResource = resource(
            acquire = { RiskyResource().also { it.initialize() } },
            release = { resource, _ -> resource.cleanup() }
        )
        
        return try {
            val result = riskyResource.use { resource ->
                resource.performRiskyOperation()
            }
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Resource嵌套使用示例
     */
    suspend fun nestedResourceExample(): String {
        val outerResource = resource(
            acquire = { CustomResource("Outer").also { it.initialize() } },
            release = { it, _ -> it.cleanup() }
        )
        
        return outerResource.use { outer ->
            val innerResource = resource(
                acquire = { CustomResource("Inner").also { it.initialize() } },
                release = { it, _ -> it.cleanup() }
            )
            
            innerResource.use { inner ->
                "${outer.getValue()} -> ${inner.getValue()}"
            }
        }
    }
    
    /**
     * Resource与suspend函数结合示例
     */
    suspend fun resourceWithSuspendExample(): String {
        val asyncResource = resource(
            acquire = {
                val res = AsyncResource()
                res.initialize()
                delay(100) // 模拟异步初始化
                res
            },
            release = { resource, _ ->
                resource.cleanup()
                delay(50) // 模拟异步清理
            }
        )
        
        return asyncResource.use { resource ->
            resource.performAsyncOperation()
        }
    }
}

/**
 * 自定义资源类
 */
class CustomResource(private val value: String) {
    private var initialized = false
    private var cleaned = false
    
    fun initialize() {
        initialized = true
        println("CustomResource initialized: $value")
    }
    
    fun cleanup() {
        cleaned = true
        println("CustomResource cleaned up: $value")
    }
    
    fun getValue(): String {
        check(initialized) { "Resource not initialized" }
        check(!cleaned) { "Resource already cleaned up" }
        return value
    }
    
    fun isInitialized() = initialized
    fun isCleaned() = cleaned
}

/**
 * 有风险的资源类，可能抛出异常
 */
class RiskyResource {
    private var initialized = false
    private var cleaned = false
    
    fun initialize() {
        initialized = true
        println("RiskyResource initialized")
    }
    
    fun cleanup() {
        cleaned = true
        println("RiskyResource cleaned up")
    }
    
    fun performRiskyOperation(): String {
        check(initialized) { "Resource not initialized" }
        
        // 50%概率抛出异常
        if (Math.random() < 0.5) {
            throw RuntimeException("Risky operation failed")
        }
        
        return "Risky operation succeeded"
    }
    
    fun isInitialized() = initialized
    fun isCleaned() = cleaned
}

/**
 * 模拟数据库连接类
 */
class MockConnection {
    private var closed = false
    
    fun query(sql: String): List<String> {
        check(!closed) { "Connection is closed" }
        return listOf("User1", "User2", "User3")
    }
    
    fun close() {
        closed = true
        println("MockConnection closed")
    }
}

/**
 * 异步资源类
 */
class AsyncResource {
    private var initialized = false
    private var cleaned = false
    
    suspend fun initialize() {
        delay(50)
        initialized = true
        println("AsyncResource initialized")
    }
    
    suspend fun cleanup() {
        delay(30)
        cleaned = true
        println("AsyncResource cleaned up")
    }
    
    suspend fun performAsyncOperation(): String {
        check(initialized) { "Resource not initialized" }
        delay(100)
        return "Async operation completed"
    }
    
    fun isInitialized() = initialized
    fun isCleaned() = cleaned
}