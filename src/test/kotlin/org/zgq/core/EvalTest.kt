package org.zgq.core

import arrow.core.Eval
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeLessThan
import kotlinx.coroutines.delay
import java.util.concurrent.atomic.AtomicInteger

/**
 * Eval特性完整测试用例
 * 
 * 测试Eval的三种求值策略：Now、Later、Always
 * 测试栈安全递归、记忆化、惰性求值等特性
 */
class EvalTest : DescribeSpec({
    
    describe("Eval creation") {
        
        it("should create Now with immediate evaluation") {
            var computed = false
            val eval = Eval.now(run {
                computed = true
                42
            })
            
            // Now应该立即计算
            computed shouldBe true
            eval.value() shouldBe 42
        }
        
        it("should create Later with lazy evaluation and memoization") {
            var computeCount = 0
            val eval = Eval.later {
                computeCount++
                42
            }
            
            // 创建时不应该计算
            computeCount shouldBe 0
            
            // 第一次访问时计算
            eval.value() shouldBe 42
            computeCount shouldBe 1
            
            // 第二次访问使用缓存
            eval.value() shouldBe 42
            computeCount shouldBe 1
        }
        
        it("should create Always with lazy evaluation without memoization") {
            var computeCount = 0
            val eval = Eval.always {
                computeCount++
                42
            }
            
            // 创建时不应该计算
            computeCount shouldBe 0
            
            // 每次访问都重新计算
            eval.value() shouldBe 42
            computeCount shouldBe 1
            
            eval.value() shouldBe 42
            computeCount shouldBe 2
            
            eval.value() shouldBe 42
            computeCount shouldBe 3
        }
    }
    
    describe("Eval memoization behavior") {
        
        it("should demonstrate Later memoization") {
            val counter = AtomicInteger(0)
            val eval = Eval.later {
                counter.incrementAndGet()
            }
            
            // 多次访问应该返回相同的值
            val result1 = eval.value()
            val result2 = eval.value()
            val result3 = eval.value()
            
            result1 shouldBe result2
            result2 shouldBe result3
            counter.get() shouldBe 1 // 只计算一次
        }
        
        it("should demonstrate Always non-memoization") {
            val counter = AtomicInteger(0)
            val eval = Eval.always {
                counter.incrementAndGet()
            }
            
            // 每次访问都应该重新计算
            val result1 = eval.value()
            val result2 = eval.value()
            val result3 = eval.value()
            
            result1 shouldBe 1
            result2 shouldBe 2
            result3 shouldBe 3
            counter.get() shouldBe 3 // 计算三次
        }
        
        it("should handle memoization with different values") {
            var currentTime = System.currentTimeMillis()
            
            val laterEval = Eval.later { currentTime }
            val alwaysEval = Eval.always { System.currentTimeMillis() }
            
            val laterResult1 = laterEval.value()
            Thread.sleep(1)
            val laterResult2 = laterEval.value()
            
            val alwaysResult1 = alwaysEval.value()
            Thread.sleep(1)
            val alwaysResult2 = alwaysEval.value()
            
            // Later应该返回相同的值（记忆化）
            laterResult1 shouldBe laterResult2
            
            // Always应该返回不同的值（每次重新计算）
            alwaysResult1 shouldNotBe alwaysResult2
        }
    }
    
    describe("Eval map operations") {
        
        it("should apply map function lazily") {
            var baseComputed = false
            var mapComputed = false
            
            val baseEval = Eval.later {
                baseComputed = true
                10
            }
            
            val mappedEval = baseEval.map { value ->
                mapComputed = true
                value * 2
            }
            
            // 创建map时不应该计算
            baseComputed shouldBe false
            mapComputed shouldBe false
            
            // 访问时才计算
            mappedEval.value() shouldBe 20
            baseComputed shouldBe true
            mapComputed shouldBe true
        }
        
        it("should chain multiple map operations") {
            val eval = Eval.later { 5 }
                .map { it * 2 }
                .map { it + 3 }
                .map { it.toString() }
            
            eval.value() shouldBe "13"
        }
        
        it("should preserve evaluation strategy in map") {
            val counter = AtomicInteger(0)
            
            val alwaysEval = Eval.always { counter.incrementAndGet() }
                .map { it * 2 }
            
            alwaysEval.value() shouldBe 2
            alwaysEval.value() shouldBe 4
            alwaysEval.value() shouldBe 6
            
            counter.get() shouldBe 3
        }
    }
    
    describe("Eval flatMap operations") {
        
        it("should apply flatMap function lazily") {
            var baseComputed = false
            var flatMapComputed = false
            
            val baseEval = Eval.later {
                baseComputed = true
                5
            }
            
            val flatMappedEval = baseEval.flatMap { value ->
                Eval.later {
                    flatMapComputed = true
                    value * value
                }
            }
            
            // 创建flatMap时不应该计算
            baseComputed shouldBe false
            flatMapComputed shouldBe false
            
            // 访问时才计算
            flatMappedEval.value() shouldBe 25
            baseComputed shouldBe true
            flatMapComputed shouldBe true
        }
        
        it("should chain multiple flatMap operations") {
            val eval = Eval.later { 3 }
                .flatMap { x -> Eval.later { x * 2 } }
                .flatMap { x -> Eval.later { x + 1 } }
                .flatMap { x -> Eval.later { x.toString() } }
            
            eval.value() shouldBe "7"
        }
        
        it("should combine different evaluation strategies") {
            val counter = AtomicInteger(0)
            
            val eval = Eval.now(10)
                .flatMap { x -> 
                    Eval.later { x * 2 }
                }
                .flatMap { x ->
                    Eval.always { 
                        counter.incrementAndGet()
                        x + counter.get()
                    }
                }
            
            val result1 = eval.value()
            val result2 = eval.value()
            
            // Always部分应该每次重新计算
            result1 shouldNotBe result2
            counter.get() shouldBe 2
        }
    }
    
    describe("Stack safe recursion") {
        
        it("should handle deep recursion without stack overflow") {
            // 使用简单的递归计数来测试栈安全，避免数值溢出
            fun countDown(n: Int): Eval<Int> {
                return if (n <= 0) {
                    Eval.now(0)
                } else {
                    Eval.defer { countDown(n - 1) }.map { it + 1 }
                }
            }
            
            // 测试深度递归，传统递归可能导致栈溢出
            countDown(1000).value() shouldBe 1000
            countDown(10000).value() shouldBe 10000
        }
        
        it("should implement stack safe fibonacci") {
            fun fibonacci(n: Long): Eval<Long> {
                return when {
                    n <= 0 -> Eval.now(0)
                    n == 1L -> Eval.now(1)
                    else -> Eval.defer { fibonacci(n - 1) }.flatMap { a ->
                        Eval.defer { fibonacci(n - 2) }.map { b ->
                            a + b
                        }
                    }
                }
            }
            
            fibonacci(0).value() shouldBe 0
            fibonacci(1).value() shouldBe 1
            fibonacci(10).value() shouldBe 55
            fibonacci(20).value() shouldBe 6765
        }
        
        it("should handle mutual recursion safely") {
            lateinit var isEven: (Int) -> Eval<Boolean>
            lateinit var isOdd: (Int) -> Eval<Boolean>
            
            isEven = { n: Int ->
                when {
                    n == 0 -> Eval.now(true)
                    n > 0 -> Eval.defer { isOdd(n - 1) }
                    else -> Eval.defer { isOdd(n + 1) }
                }
            }
            
            isOdd = { n: Int ->
                when {
                    n == 0 -> Eval.now(false)
                    n > 0 -> Eval.defer { isEven(n - 1) }
                    else -> Eval.defer { isEven(n + 1) }
                }
            }
            
            isEven(0).value() shouldBe true
            isOdd(0).value() shouldBe false
            isEven(100).value() shouldBe true
            isOdd(101).value() shouldBe true
            isEven(1000).value() shouldBe true
        }
    }
    
    describe("Eval defer operations") {
        
        it("should defer Eval creation") {
            var evalCreated = false
            var valueComputed = false
            
            val deferredEval = Eval.defer {
                evalCreated = true
                Eval.later {
                    valueComputed = true
                    "deferred result"
                }
            }
            
            // defer时不应该创建内部Eval
            evalCreated shouldBe false
            valueComputed shouldBe false
            
            // 访问时才创建和计算
            deferredEval.value() shouldBe "deferred result"
            evalCreated shouldBe true
            valueComputed shouldBe true
        }
        
        it("should enable conditional computation") {
            fun conditionalEval(condition: Boolean): Eval<String> {
                return Eval.defer {
                    if (condition) {
                        Eval.now("condition true")
                    } else {
                        Eval.now("condition false")
                    }
                }
            }
            
            conditionalEval(true).value() shouldBe "condition true"
            conditionalEval(false).value() shouldBe "condition false"
        }
        
        it("should work with recursive defer") {
            fun countdown(n: Int): Eval<List<Int>> {
                return if (n <= 0) {
                    Eval.now(emptyList())
                } else {
                    Eval.defer { countdown(n - 1) }.map { list ->
                        listOf(n) + list
                    }
                }
            }
            
            countdown(5).value() shouldBe listOf(5, 4, 3, 2, 1)
            countdown(0).value() shouldBe emptyList()
        }
    }
    
    describe("Error handling") {
        
        it("should propagate exceptions from Now") {
            shouldThrow<RuntimeException> {
                Eval.now(throw RuntimeException("Now error"))
            }.message shouldBe "Now error"
        }
        
        it("should propagate exceptions from Later") {
            val eval = Eval.later {
                throw RuntimeException("Later error")
            }
            
            shouldThrow<RuntimeException> {
                eval.value()
            }.message shouldBe "Later error"
        }
        
        it("should propagate exceptions from Always") {
            val eval = Eval.always {
                throw RuntimeException("Always error")
            }
            
            shouldThrow<RuntimeException> {
                eval.value()
            }.message shouldBe "Always error"
        }
        
        it("should handle exceptions in map operations") {
            val eval = Eval.later { 10 }
                .map { 
                    if (it > 5) throw RuntimeException("Value too large")
                    it * 2
                }
            
            shouldThrow<RuntimeException> {
                eval.value()
            }.message shouldBe "Value too large"
        }
        
        it("should handle exceptions in flatMap operations") {
            val eval = Eval.later { 5 }
                .flatMap { 
                    Eval.later {
                        throw RuntimeException("FlatMap error")
                    }
                }
            
            shouldThrow<RuntimeException> {
                eval.value()
            }.message shouldBe "FlatMap error"
        }
    }
    
    describe("Performance characteristics") {
        
        it("should demonstrate lazy evaluation performance") {
            var expensiveComputationCalled = false
            
            val eval = Eval.later {
                Thread.sleep(100) // 模拟昂贵计算
                expensiveComputationCalled = true
                "expensive result"
            }
            
            // 创建Eval应该很快
            val startTime = System.currentTimeMillis()
            // eval已创建
            val creationTime = System.currentTimeMillis() - startTime
            
            creationTime shouldBeLessThan 50L
            expensiveComputationCalled shouldBe false
            
            // 第一次访问会触发计算
            val accessStartTime = System.currentTimeMillis()
            eval.value() shouldBe "expensive result"
            val accessTime = System.currentTimeMillis() - accessStartTime
            
            accessTime shouldBeGreaterThan 90L
            expensiveComputationCalled shouldBe true
        }
        
        it("should demonstrate memoization performance benefit") {
            val eval = Eval.later {
                Thread.sleep(50) // 模拟计算延迟
                "computed value"
            }
            
            // 第一次访问
            val firstAccessTime = measureTimeMillis {
                eval.value() shouldBe "computed value"
            }
            
            // 第二次访问应该更快（使用缓存）
            val secondAccessTime = measureTimeMillis {
                eval.value() shouldBe "computed value"
            }
            
            firstAccessTime shouldBeGreaterThan 40L
            secondAccessTime shouldBeLessThan 10L
        }
        
        it("should show Always vs Later performance difference") {
            val laterEval = Eval.later {
                Thread.sleep(20)
                "later result"
            }
            
            val alwaysEval = Eval.always {
                Thread.sleep(20)
                "always result"
            }
            
            // Later: 第一次慢，第二次快
            val laterFirstTime = measureTimeMillis { laterEval.value() }
            val laterSecondTime = measureTimeMillis { laterEval.value() }
            
            // Always: 每次都慢
            val alwaysFirstTime = measureTimeMillis { alwaysEval.value() }
            val alwaysSecondTime = measureTimeMillis { alwaysEval.value() }
            
            laterFirstTime shouldBeGreaterThan 15L
            laterSecondTime shouldBeLessThan 5L
            
            alwaysFirstTime shouldBeGreaterThan 15L
            alwaysSecondTime shouldBeGreaterThan 15L
        }
    }
    
    describe("Complex scenarios") {
        
        it("should handle nested Eval structures") {
            val nestedEval = Eval.later {
                Eval.later {
                    Eval.later { "deeply nested" }
                }
            }
            
            // 需要多次value()调用来获取最终值
            nestedEval.value().value().value() shouldBe "deeply nested"
        }
        
        it("should work with data processing pipeline") {
            val pipeline = Eval.later { (1..100).toList() }
                .map { list -> list.filter { it % 2 == 0 } }
                .map { list -> list.map { it * 2 } }
                .map { list -> list.sum() }
            
            pipeline.value() shouldBe 5100 // 2+4+6+...+200 = 5100
        }
        
        it("should handle resource management pattern") {
            var resourceAcquired = false
            var resourceReleased = false
            
            fun withResource(block: (String) -> String): Eval<String> {
                return Eval.later {
                    resourceAcquired = true
                    val resource = "test-resource"
                    try {
                        block(resource)
                    } finally {
                        resourceReleased = true
                    }
                }
            }
            
            val result = withResource { resource ->
                "processed-$resource"
            }
            
            // 资源管理应该在访问时才执行
            resourceAcquired shouldBe false
            resourceReleased shouldBe false
            
            result.value() shouldBe "processed-test-resource"
            resourceAcquired shouldBe true
            resourceReleased shouldBe true
        }
    }
})

/**
 * 辅助函数：测量执行时间
 */
private inline fun measureTimeMillis(block: () -> Unit): Long {
    val start = System.currentTimeMillis()
    block()
    return System.currentTimeMillis() - start
}