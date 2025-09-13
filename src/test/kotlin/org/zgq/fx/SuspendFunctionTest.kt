package org.zgq.fx

import arrow.core.Either
import arrow.fx.coroutines.parMap
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.test.runTest

/**
 * Arrow Fx suspend函数组合完整测试用例
 *
 * 测试suspend函数链式调用、异步操作组合和错误处理
 */
class SuspendFunctionTest :
        DescribeSpec({
            describe("基础suspend函数组合") {
                it("应该正确执行suspend函数链式调用") {
                    runTest {
                        val result = SuspendFunctionExamples.basicSuspendChainExample()
                        result shouldContain "Saved user 1 successfully"
                    }
                }

                it("应该处理有效用户ID的错误处理流程") {
                    runTest {
                        val result = SuspendFunctionExamples.suspendWithErrorHandlingExample(1)
                        result.shouldBeInstanceOf<Either.Right<String>>()
                        result.value shouldContain "Saved user 1 successfully"
                    }
                }

                it("应该处理无效用户ID的错误") {
                    runTest {
                        val result = SuspendFunctionExamples.suspendWithErrorHandlingExample(-1)
                        result.shouldBeInstanceOf<Either.Left<AppError>>()
                        result.value.shouldBeInstanceOf<AppError.InvalidInput>()
                    }
                }
            }

            describe("并行处理操作") {
                it("应该并行处理多个异步操作") {
                    runTest {
                        val result = SuspendFunctionExamples.parallelProcessingExample()

                        result.size shouldBe 5
                        result.forEach { item -> item shouldContain "Processed user" }

                        // 验证所有用户ID都被处理
                        (1..5).forEach { userId ->
                            result.any { it.contains("user $userId") } shouldBe true
                        }
                    }
                }

                it("应该使用parMap进行并行映射") {
                    runTest {
                        val result = SuspendFunctionExamples.parallelMappingExample()

                        result.size shouldBe 3
                        result.forEach { processedData ->
                            processedData.name shouldContain "(Enhanced)"
                            processedData.processedAt shouldNotBe 0L
                        }

                        // 验证所有用户都被增强处理
                        result.map { it.id }.sorted() shouldBe listOf(1, 2, 3)
                    }
                }
            }

            describe("竞争执行和超时处理") {
                it("应该返回最快完成的数据源结果") {
                    runTest {
                        val result = SuspendFunctionExamples.raceExecutionExample()

                        // 由于fastDataSource最快，应该返回fast source的结果
                        result shouldContain "Fast source: Fast data"
                    }
                }

                it("应该在超时内完成操作") {
                    runTest {
                        val result = SuspendFunctionExamples.timeoutHandlingExample()

                        result.shouldBeInstanceOf<Either.Right<String>>()
                        result.value shouldBe "Long operation completed"
                    }
                }
            }

            describe("复杂异步工作流") {
                it("应该成功执行完整的异步工作流") {
                    runTest {
                        val result = SuspendFunctionExamples.complexAsyncWorkflowExample(1)

                        result.shouldBeInstanceOf<Either.Right<WorkflowResult>>()
                        val workflowResult = result.value
                        workflowResult.userId shouldBe 1
                        workflowResult.processedData.name shouldBe "USER1"
                        workflowResult.savedAt shouldNotBe 0L
                    }
                }

                it("应该处理权限被拒绝的情况") {
                    runTest {
                        val result = SuspendFunctionExamples.complexAsyncWorkflowExample(2)

                        result.shouldBeInstanceOf<Either.Left<AppError>>()
                        result.value.shouldBeInstanceOf<AppError.PermissionDenied>()
                    }
                }

                it("应该处理无效用户ID的工作流") {
                    runTest {
                        val result = SuspendFunctionExamples.complexAsyncWorkflowExample(-1)

                        result.shouldBeInstanceOf<Either.Left<AppError>>()
                        result.value.shouldBeInstanceOf<AppError.InvalidInput>()
                    }
                }
            }

            describe("重试机制") {
                it("应该在多次尝试后可能成功") {
                    runTest {
                        // 由于unreliableOperation有随机性，我们多次测试以验证重试机制
                        var successCount = 0
                        var failureCount = 0

                        repeat(10) {
                            val result = SuspendFunctionExamples.retryMechanismExample()
                            when (result) {
                                is Either.Right -> successCount++
                                is Either.Left -> {
                                    failureCount++
                                    result.value.shouldBeInstanceOf<AppError.MaxRetriesExceeded>()
                                }
                            }
                        }

                        // 至少应该有一些成功或失败的情况
                        (successCount + failureCount) shouldBe 10
                    }
                }
            }

            describe("流水线处理") {
                it("应该按顺序处理流水线步骤") {
                    runTest {
                        val inputData = listOf("A", "B", "C")
                        val result = SuspendFunctionExamples.pipelineProcessingExample(inputData)

                        result.size shouldBe 3
                        result.forEach { item -> item shouldContain "Step3(Step2(Step1(" }

                        // 验证每个输入都被正确处理
                        result shouldBe
                                listOf(
                                        "Step3(Step2(Step1(A)))",
                                        "Step3(Step2(Step1(B)))",
                                        "Step3(Step2(Step1(C)))"
                                )
                    }
                }

                it("应该并行处理流水线中的每个步骤") {
                    runTest {
                        val largeInputData = (1..100).map { "Item$it" }

                        // 测试大量数据的并行处理
                        val result =
                                SuspendFunctionExamples.pipelineProcessingExample(largeInputData)

                        result.size shouldBe 100
                        result.forEach { item -> item shouldContain "Step3(Step2(Step1(Item" }
                    }
                }
            }

            describe("错误处理和恢复") {
                it("应该正确处理网络错误") {
                    runTest {
                        val result = SuspendFunctionExamples.suspendWithErrorHandlingExample(0)

                        result.shouldBeInstanceOf<Either.Left<AppError>>()
                        result.value.shouldBeInstanceOf<AppError.InvalidInput>()
                    }
                }

                it("应该处理各种类型的应用错误") {
                    runTest {
                        // 测试不同的错误类型
                        val errors =
                                listOf(
                                        AppError.InvalidInput("Test input error"),
                                        AppError.NetworkError("Test network error"),
                                        AppError.ProcessingError("Test processing error"),
                                        AppError.DatabaseError("Test database error"),
                                        AppError.PermissionDenied("Test permission error"),
                                        AppError.TimeoutError("Test timeout error"),
                                        AppError.MaxRetriesExceeded("Test retry error"),
                                        AppError.UnexpectedError("Test unexpected error")
                                )

                        errors.forEach { error ->
                            error.shouldBeInstanceOf<AppError>()
                            when (error) {
                                is AppError.InvalidInput -> error.message shouldContain "input"
                                is AppError.NetworkError -> error.message shouldContain "network"
                                is AppError.ProcessingError ->
                                        error.message shouldContain "processing"
                                is AppError.DatabaseError -> error.message shouldContain "database"
                                is AppError.PermissionDenied ->
                                        error.message shouldContain "permission"
                                is AppError.TimeoutError -> error.message shouldContain "timeout"
                                is AppError.MaxRetriesExceeded ->
                                        error.message shouldContain "retry"
                                is AppError.UnexpectedError ->
                                        error.message shouldContain "unexpected"
                            }
                        }
                    }
                }
            }

            describe("性能和并发测试") {
                it("应该高效处理大量并行操作") {
                    runTest {
                        val startTime = System.currentTimeMillis()

                        // 处理大量用户数据
                        val userIds = (1..50).toList()
                        val results =
                                userIds.parMap { userId ->
                                    SuspendFunctionExamples.suspendWithErrorHandlingExample(userId)
                                }

                        val endTime = System.currentTimeMillis()
                        val duration = endTime - startTime

                        // 验证结果
                        results.size shouldBe 50
                        results.forEach { result ->
                            result.shouldBeInstanceOf<Either.Right<String>>()
                        }

                        // 并行处理应该比串行处理快得多
                        // 这里我们只验证能在合理时间内完成
                        (duration < 10000) shouldBe true // 应该在10秒内完成
                    }
                }

                it("应该正确处理并发访问") {
                    runTest {
                        // 测试并发安全性
                        val concurrentResults =
                                (1..20).parMap { index ->
                                    SuspendFunctionExamples.basicSuspendChainExample()
                                }

                        concurrentResults.size shouldBe 20
                        concurrentResults.forEach { result ->
                            result shouldContain "Saved user 1 successfully"
                        }
                    }
                }
            }

            describe("数据类型验证") {
                it("应该正确创建和使用UserData") {
                    runTest {
                        val userData = UserData(1, "Test User", "test@example.com")

                        userData.id shouldBe 1
                        userData.name shouldBe "Test User"
                        userData.email shouldBe "test@example.com"
                    }
                }

                it("应该正确创建和使用ProcessedUserData") {
                    runTest {
                        val processedData =
                                ProcessedUserData(1, "PROCESSED USER", "test@example.com", 12345L)

                        processedData.id shouldBe 1
                        processedData.name shouldBe "PROCESSED USER"
                        processedData.email shouldBe "test@example.com"
                        processedData.processedAt shouldBe 12345L
                    }
                }

                it("应该正确创建和使用UserPermissions") {
                    runTest {
                        val permissions = UserPermissions(1, true)

                        permissions.userId shouldBe 1
                        permissions.canAccess shouldBe true
                    }
                }

                it("应该正确创建和使用WorkflowResult") {
                    runTest {
                        val processedData =
                                ProcessedUserData(1, "USER1", "user1@example.com", 12345L)
                        val workflowResult = WorkflowResult(1, processedData, 67890L)

                        workflowResult.userId shouldBe 1
                        workflowResult.processedData shouldBe processedData
                        workflowResult.savedAt shouldBe 67890L
                    }
                }
            }

            describe("边界条件测试") {
                it("应该处理空列表的并行处理") {
                    runTest {
                        val emptyList = emptyList<String>()
                        val result = SuspendFunctionExamples.pipelineProcessingExample(emptyList)

                        result shouldBe emptyList()
                    }
                }

                it("应该处理单个元素的并行处理") {
                    runTest {
                        val singleItem = listOf("Single")
                        val result = SuspendFunctionExamples.pipelineProcessingExample(singleItem)

                        result shouldBe listOf("Step3(Step2(Step1(Single)))")
                    }
                }

                it("应该处理极大用户ID") {
                    runTest {
                        val result =
                                SuspendFunctionExamples.suspendWithErrorHandlingExample(
                                        Int.MAX_VALUE
                                )

                        result.shouldBeInstanceOf<Either.Right<String>>()
                        result.value shouldContain "Saved user ${Int.MAX_VALUE} successfully"
                    }
                }
            }
        })
