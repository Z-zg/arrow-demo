package org.zgq.core

import arrow.core.Eval

/**
 * Eval特性示例
 * 
 * Eval是Arrow中用于惰性求值和栈安全递归的数据类型。
 * 它提供了三种求值策略：Now（立即求值）、Later（惰性求值，有记忆化）、Always（惰性求值，无记忆化）。
 * Eval特别适用于处理可能导致栈溢出的递归计算。
 */
object EvalExamples {
    
    /**
     * Eval的三种创建方式
     */
    fun creationExamples() {
        println("=== Eval Creation Examples ===")
        
        // Now: 立即求值，值会被立即计算并缓存
        val now = Eval.now(42)
        println("Eval.now(42): $now")
        
        // Later: 惰性求值，第一次访问时计算，结果会被缓存（记忆化）
        val later = Eval.later { 
            println("Computing later value...")
            21 * 2 
        }
        println("Eval.later created (not computed yet)")
        
        // Always: 惰性求值，每次访问都重新计算，不缓存结果
        val always = Eval.always { 
            println("Computing always value...")
            System.currentTimeMillis().toInt() % 1000
        }
        println("Eval.always created (not computed yet)")
        
        // 访问值来触发计算
        println("Later value (first access): ${later.value()}")
        println("Later value (second access): ${later.value()}")
        
        println("Always value (first access): ${always.value()}")
        Thread.sleep(1) // 确保时间戳不同
        println("Always value (second access): ${always.value()}")
    }
    
    /**
     * 演示记忆化（Memoization）特性
     */
    fun memoizationExamples() {
        println("\n=== Memoization Examples ===")
        
        var laterComputeCount = 0
        var alwaysComputeCount = 0
        
        val laterEval = Eval.later {
            laterComputeCount++
            println("Later computation #$laterComputeCount")
            "Later result"
        }
        
        val alwaysEval = Eval.always {
            alwaysComputeCount++
            println("Always computation #$alwaysComputeCount")
            "Always result"
        }
        
        // Later: 只计算一次，后续访问使用缓存值
        println("Later - First access: ${laterEval.value()}")
        println("Later - Second access: ${laterEval.value()}")
        println("Later - Third access: ${laterEval.value()}")
        
        println()
        
        // Always: 每次访问都重新计算
        println("Always - First access: ${alwaysEval.value()}")
        println("Always - Second access: ${alwaysEval.value()}")
        println("Always - Third access: ${alwaysEval.value()}")
        
        println("Later compute count: $laterComputeCount")
        println("Always compute count: $alwaysComputeCount")
    }
    
    /**
     * 使用map进行函数式转换
     */
    fun mapOperations() {
        println("\n=== Map Operations ===")
        
        val baseEval = Eval.later {
            println("Computing base value...")
            10
        }
        
        // map操作是惰性的，不会立即触发计算
        val mappedEval = baseEval.map { value ->
            println("Mapping value: $value")
            value * 2
        }
        
        println("Map operation defined (not computed yet)")
        
        // 只有在访问最终值时才会触发整个计算链
        println("Final result: ${mappedEval.value()}")
        
        // 链式map操作
        val chainedEval = baseEval
            .map { it * 2 }
            .map { it + 5 }
            .map { "Result: $it" }
        
        println("Chained result: ${chainedEval.value()}")
    }
    
    /**
     * 使用flatMap进行单子操作
     */
    fun flatMapOperations() {
        println("\n=== FlatMap Operations ===")
        
        val baseEval = Eval.later { 5 }
        
        // flatMap用于组合多个Eval计算
        val flatMappedEval = baseEval.flatMap { value ->
            Eval.later {
                println("FlatMap computation with value: $value")
                value * value
            }
        }
        
        println("FlatMap operation defined")
        println("FlatMap result: ${flatMappedEval.value()}")
        
        // 复杂的flatMap链
        val complexEval = baseEval
            .flatMap { x -> Eval.later { x * 2 } }
            .flatMap { x -> Eval.later { x + 3 } }
            .flatMap { x -> Eval.later { x.toString() } }
        
        println("Complex flatMap result: ${complexEval.value()}")
    }
    
    /**
     * 栈安全的递归计算
     */
    fun stackSafeRecursion() {
        println("\n=== Stack Safe Recursion ===")
        
        // 传统递归可能导致栈溢出的阶乘计算
        fun factorialUnsafe(n: Long): Long {
            return if (n <= 1) 1 else n * factorialUnsafe(n - 1)
        }
        
        // 使用Eval实现栈安全的阶乘计算
        fun factorialSafe(n: Long): Eval<Long> {
            return if (n <= 1) {
                Eval.now(1)
            } else {
                Eval.defer { factorialSafe(n - 1) }.map { it * n }
            }
        }
        
        // 栈安全的斐波那契数列
        fun fibonacciSafe(n: Long): Eval<Long> {
            return when {
                n <= 0 -> Eval.now(0)
                n == 1L -> Eval.now(1)
                else -> Eval.defer { fibonacciSafe(n - 1) }.flatMap { a ->
                    Eval.defer { fibonacciSafe(n - 2) }.map { b ->
                        a + b
                    }
                }
            }
        }
        
        // 测试小数值
        println("Factorial 5: ${factorialSafe(5).value()}")
        println("Fibonacci 10: ${fibonacciSafe(10).value()}")
        
        // 测试较大数值（传统递归可能栈溢出的情况）
        try {
            println("Factorial 20: ${factorialSafe(20).value()}")
        } catch (e: Exception) {
            println("Factorial calculation failed: ${e.message}")
        }
    }
    
    /**
     * 使用defer进行延迟计算
     */
    fun deferExamples() {
        println("\n=== Defer Examples ===")
        
        // defer用于延迟Eval的创建，实现真正的惰性
        val deferredEval = Eval.defer {
            println("Creating deferred Eval...")
            Eval.later {
                println("Computing deferred value...")
                "Deferred result"
            }
        }
        
        println("Deferred Eval created")
        println("Deferred result: ${deferredEval.value()}")
        
        // 使用defer实现条件计算
        fun conditionalComputation(condition: Boolean): Eval<String> {
            return Eval.defer {
                if (condition) {
                    Eval.later { "Condition was true" }
                } else {
                    Eval.later { "Condition was false" }
                }
            }
        }
        
        println("Conditional (true): ${conditionalComputation(true).value()}")
        println("Conditional (false): ${conditionalComputation(false).value()}")
    }
    
    /**
     * 实际应用：惰性数据处理管道
     */
    fun lazyDataPipeline() {
        println("\n=== Lazy Data Pipeline ===")
        
        // 模拟昂贵的数据加载操作
        fun loadData(): Eval<List<Int>> {
            return Eval.later {
                println("Loading data from database...")
                Thread.sleep(100) // 模拟IO延迟
                (1..1000).toList()
            }
        }
        
        // 模拟昂贵的数据处理操作
        fun processData(data: List<Int>): Eval<List<Int>> {
            return Eval.later {
                println("Processing data...")
                Thread.sleep(50) // 模拟计算延迟
                data.filter { it % 2 == 0 }.map { it * 2 }
            }
        }
        
        // 模拟数据聚合操作
        fun aggregateData(data: List<Int>): Eval<Int> {
            return Eval.later {
                println("Aggregating data...")
                data.sum()
            }
        }
        
        // 构建惰性数据处理管道
        val pipeline = loadData()
            .flatMap { data -> processData(data) }
            .flatMap { processed -> aggregateData(processed) }
        
        println("Pipeline created (no computation yet)")
        
        // 只有在需要结果时才执行整个管道
        val startTime = System.currentTimeMillis()
        val result = pipeline.value()
        val endTime = System.currentTimeMillis()
        
        println("Pipeline result: $result")
        println("Execution time: ${endTime - startTime}ms")
        
        // 再次访问会使用缓存的结果（如果使用Later）
        val startTime2 = System.currentTimeMillis()
        val result2 = pipeline.value()
        val endTime2 = System.currentTimeMillis()
        
        println("Second access result: $result2")
        println("Second access time: ${endTime2 - startTime2}ms")
    }
    
    /**
     * 错误处理与Eval
     */
    fun errorHandling() {
        println("\n=== Error Handling ===")
        
        // 可能抛出异常的计算
        val riskyComputation = Eval.later {
            println("Performing risky computation...")
            val random = (1..10).random()
            if (random > 5) {
                "Success: $random"
            } else {
                throw RuntimeException("Computation failed with value: $random")
            }
        }
        
        // 使用try-catch处理Eval中的异常
        try {
            val result = riskyComputation.value()
            println("Risky computation result: $result")
        } catch (e: Exception) {
            println("Caught exception: ${e.message}")
        }
        
        // 使用map进行安全的错误处理
        val safeComputation = Eval.later { "42" }.map { str ->
            try {
                str.toInt()
            } catch (e: NumberFormatException) {
                0
            }
        }
        
        println("Safe computation result: ${safeComputation.value()}")
    }
    
    /**
     * 性能比较：Eval vs 普通计算
     */
    fun performanceComparison() {
        println("\n=== Performance Comparison ===")
        
        // 昂贵的计算函数
        fun expensiveComputation(n: Int): Int {
            Thread.sleep(10) // 模拟计算延迟
            return n * n
        }
        
        // 普通计算（立即执行）
        val startTime1 = System.currentTimeMillis()
        val eagerResult = expensiveComputation(10)
        val endTime1 = System.currentTimeMillis()
        println("Eager computation result: $eagerResult, time: ${endTime1 - startTime1}ms")
        
        // 使用Eval.later（惰性执行）
        val startTime2 = System.currentTimeMillis()
        val lazyEval = Eval.later { expensiveComputation(10) }
        val creationTime = System.currentTimeMillis()
        println("Lazy Eval created, time: ${creationTime - startTime2}ms")
        
        val result = lazyEval.value()
        val endTime2 = System.currentTimeMillis()
        println("Lazy computation result: $result, total time: ${endTime2 - startTime2}ms")
        
        // 多次访问惰性计算（展示记忆化效果）
        val startTime3 = System.currentTimeMillis()
        val result2 = lazyEval.value()
        val endTime3 = System.currentTimeMillis()
        println("Second access result: $result2, time: ${endTime3 - startTime3}ms")
    }
    
    /**
     * 演示所有示例
     */
    fun runAllExamples() {
        creationExamples()
        memoizationExamples()
        mapOperations()
        flatMapOperations()
        stackSafeRecursion()
        deferExamples()
        lazyDataPipeline()
        errorHandling()
        performanceComparison()
    }
}