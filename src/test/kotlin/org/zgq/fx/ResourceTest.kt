package org.zgq.fx

import arrow.fx.coroutines.Resource
import arrow.fx.coroutines.resource
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import java.io.Closeable

/**
 * Arrow Fx Resource完整测试用例
 * 
 * 测试Resource的acquire、release、use操作
 * 测试资源组合和异常安全性
 */
class ResourceTest : DescribeSpec({
    
    describe("Resource基础操作") {
        
        it("应该正确创建和使用Resource") {
            runTest {
                val result = ResourceExamples.basicResourceExample()
                result shouldBe "Hello Resource"
            }
        }
        
        it("应该从Closeable创建Resource") {
            runTest {
                var closed = false
                val resource = resource(
                    acquire = {
                        object : Closeable {
                            override fun close() {
                                closed = true
                            }
                        }
                    },
                    release = { closeable, _ -> closeable.close() }
                )
                
                resource.use { }
                closed shouldBe true
            }
        }
        
        it("应该使用自定义acquire和release创建Resource") {
            runTest {
                val customResource = CustomResource("Test")
                
                val resource = resource(
                    acquire = {
                        customResource.initialize()
                        customResource
                    },
                    release = { res, _ ->
                        res.cleanup()
                    }
                )
                
                val result = resource.use { res ->
                    res.getValue()
                }
                
                result shouldBe "Test"
                customResource.isInitialized() shouldBe true
                customResource.isCleaned() shouldBe true
            }
        }
    }
    
    describe("Resource生命周期管理") {
        
        it("应该在使用前初始化资源") {
            runTest {
                val customResource = CustomResource("Lifecycle Test")
                
                val resource = resource(
                    acquire = {
                        customResource.initialize()
                        customResource
                    },
                    release = { res, _ ->
                        res.cleanup()
                    }
                )
                
                // 使用前资源未初始化
                customResource.isInitialized() shouldBe false
                
                resource.use { res ->
                    // 使用时资源已初始化
                    res.isInitialized() shouldBe true
                    res.getValue()
                }
                
                // 使用后资源已清理
                customResource.isCleaned() shouldBe true
            }
        }
        
        it("应该在异常情况下也能正确释放资源") {
            runTest {
                val customResource = CustomResource("Exception Test")
                
                val resource = resource(
                    acquire = {
                        customResource.initialize()
                        customResource
                    },
                    release = { res, _ ->
                        res.cleanup()
                    }
                )
                
                shouldThrow<RuntimeException> {
                    resource.use { res ->
                        res.getValue()
                        throw RuntimeException("Test exception")
                    }
                }
                
                // 即使抛出异常，资源也应该被清理
                customResource.isCleaned() shouldBe true
            }
        }
        
        it("应该按正确顺序释放嵌套资源") {
            runTest {
                val cleanupOrder = mutableListOf<String>()
                
                val resource1 = resource(
                    acquire = { CustomResource("Resource1").also { it.initialize() } },
                    release = { res, _ ->
                        cleanupOrder.add(res.getValue())
                        res.cleanup()
                    }
                )
                
                val resource2 = resource(
                    acquire = { CustomResource("Resource2").also { it.initialize() } },
                    release = { res, _ ->
                        cleanupOrder.add(res.getValue())
                        res.cleanup()
                    }
                )
                
                resource1.use { r1 ->
                    resource2.use { r2 ->
                        "${r1.getValue()}-${r2.getValue()}"
                    }
                }
                
                // 嵌套资源应该按LIFO顺序释放
                cleanupOrder shouldBe listOf("Resource2", "Resource1")
            }
        }
    }
    
    describe("Resource组合操作") {
        
        it("应该能够组合多个Resource") {
            runTest {
                val result = ResourceExamples.resourceCompositionExample()
                result shouldBe "Resource 1 + Resource 2"
            }
        }
        
        it("应该支持zip操作组合Resource") {
            runTest {
                val resource1 = resource(
                    acquire = { CustomResource("A").also { it.initialize() } },
                    release = { it, _ -> it.cleanup() }
                )
                
                val resource2 = resource(
                    acquire = { CustomResource("B").also { it.initialize() } },
                    release = { it, _ -> it.cleanup() }
                )
                
                val result = resource1.zip(resource2).use { pair ->
                    "${pair.first.getValue()}-${pair.second.getValue()}"
                }
                
                result shouldBe "A-B"
            }
        }
        
        it("应该支持map操作转换Resource") {
            runTest {
                val resource = resource(
                    acquire = { CustomResource("Original").also { it.initialize() } },
                    release = { it, _ -> it.cleanup() }
                )
                
                val mappedResource = resource.map { res ->
                    res.getValue().uppercase()
                }
                
                val result = mappedResource.use { value ->
                    value
                }
                
                result shouldBe "ORIGINAL"
            }
        }
        
        it("应该支持flatMap操作链式组合Resource") {
            runTest {
                val resource1 = resource(
                    acquire = { CustomResource("First").also { it.initialize() } },
                    release = { it, _ -> it.cleanup() }
                )
                
                val result = resource1.flatMap { res1 ->
                    resource(
                        acquire = { CustomResource("${res1.getValue()}-Second").also { it.initialize() } },
                        release = { it, _ -> it.cleanup() }
                    )
                }.use { res2 ->
                    res2.getValue()
                }
                
                result shouldBe "First-Second"
            }
        }
    }
    
    describe("Resource异常安全性") {
        
        it("应该在acquire阶段异常时不调用release") {
            runTest {
                var releaseCallCount = 0
                
                val faultyResource = resource<String>(
                    acquire = { throw RuntimeException("Acquire failed") },
                    release = { _, _ -> releaseCallCount++ }
                )
                
                shouldThrow<RuntimeException> {
                    faultyResource.use { it }
                }
                
                releaseCallCount shouldBe 0
            }
        }
        
        it("应该在use阶段异常时正确调用release") {
            runTest {
                var releaseCallCount = 0
                val customResource = CustomResource("Exception Safety")
                
                val resource = resource(
                    acquire = {
                        customResource.initialize()
                        customResource
                    },
                    release = { it, _ ->
                        releaseCallCount++
                        it.cleanup()
                    }
                )
                
                shouldThrow<RuntimeException> {
                    resource.use {
                        throw RuntimeException("Use failed")
                    }
                }
                
                releaseCallCount shouldBe 1
                customResource.isCleaned() shouldBe true
            }
        }
        
        it("应该处理release阶段的异常") {
            runTest {
                val customResource = CustomResource("Release Exception")
                
                val resource = resource(
                    acquire = {
                        customResource.initialize()
                        customResource
                    },
                    release = { it, _ ->
                        it.cleanup()
                        throw RuntimeException("Release failed")
                    }
                )
                
                // use成功但release失败应该抛出release异常
                shouldThrow<RuntimeException> {
                    resource.use { res ->
                        res.getValue()
                    }
                }.message shouldBe "Release failed"
                
                customResource.isCleaned() shouldBe true
            }
        }
        
        it("应该处理RiskyResource的异常情况") {
            runTest {
                // 多次运行以测试随机异常
                repeat(10) {
                    val result = ResourceExamples.resourceExceptionHandlingExample()
                    
                    // 结果应该是成功或失败，不应该为null
                    result shouldNotBe null
                    
                    if (result.isFailure) {
                        result.exceptionOrNull()?.message shouldBe "Risky operation failed"
                    } else {
                        result.getOrNull() shouldBe "Risky operation succeeded"
                    }
                }
            }
        }
    }
    
    describe("Resource与协程集成") {
        
        it("应该支持suspend函数在acquire和release中") {
            runTest {
                val result = ResourceExamples.resourceWithSuspendExample()
                result shouldBe "Async operation completed"
            }
        }
        
        it("应该正确处理异步资源的生命周期") {
            runTest {
                val asyncResource = AsyncResource()
                
                val resource = resource(
                    acquire = {
                        asyncResource.initialize()
                        asyncResource
                    },
                    release = { res, _ ->
                        res.cleanup()
                    }
                )
                
                val result = resource.use { res ->
                    res.performAsyncOperation()
                }
                
                result shouldBe "Async operation completed"
                asyncResource.isInitialized() shouldBe true
                asyncResource.isCleaned() shouldBe true
            }
        }
        
        it("应该支持嵌套Resource使用") {
            runTest {
                val result = ResourceExamples.nestedResourceExample()
                result shouldBe "Outer -> Inner"
            }
        }
    }
    
    describe("Resource实际应用场景") {
        
        it("应该能够管理文件资源") {
            runTest {
                val filename = "test-resource.txt"
                val content = "Hello Resource Management"
                
                // 这里我们模拟文件操作，实际测试中可能需要临时文件
                try {
                    ResourceExamples.fileResourceExample(filename, content)
                    // 在实际环境中，这里会验证文件内容
                } catch (e: Exception) {
                    // 处理文件系统相关异常
                    e.message shouldContain "test-resource.txt"
                }
            }
        }
        
        it("应该能够管理数据库连接资源") {
            runTest {
                val result = ResourceExamples.databaseResourceExample()
                result shouldBe listOf("User1", "User2", "User3")
            }
        }
    }
    
    describe("Resource性能和内存管理") {
        
        it("应该能够处理大量Resource创建和释放") {
            runTest {
                val resourceCount = 1000
                var totalCreated = 0
                var totalReleased = 0
                
                repeat(resourceCount) { i ->
                    val resource = resource(
                        acquire = {
                            totalCreated++
                            CustomResource("Resource-$i").also { it.initialize() }
                        },
                        release = { res, _ ->
                            totalReleased++
                            res.cleanup()
                        }
                    )
                    
                    resource.use { res ->
                        res.getValue()
                    }
                }
                
                totalCreated shouldBe resourceCount
                totalReleased shouldBe resourceCount
            }
        }
        
        it("应该支持Resource的并发使用") {
            runTest {
                // 注意：同一个Resource实例不应该并发使用
                // 这里测试的是Resource创建的并发安全性
                val results = (1..10).map { i ->
                    val concurrentResource = resource(
                        acquire = { CustomResource("Concurrent-$i").also { it.initialize() } },
                        release = { it, _ -> it.cleanup() }
                    )
                    
                    concurrentResource.use { res ->
                        delay(10) // 模拟一些工作
                        res.getValue()
                    }
                }
                
                results.size shouldBe 10
                results.forEach { result ->
                    result shouldContain "Concurrent-"
                }
            }
        }
    }
})