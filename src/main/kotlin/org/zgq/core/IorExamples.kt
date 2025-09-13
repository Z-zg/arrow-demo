package org.zgq.core

import arrow.core.Ior

/**
 * Ior特性示例
 *
 * Ior（Inclusive Or）是Arrow中的包含性或数据类型，它可以表示三种状态：
 * - Left(A): 只有左值
 * - Right(B): 只有右值
 * - Both(A, B): 同时包含左值和右值
 *
 * Ior常用于需要同时携带警告信息和结果值的场景， 或者需要累积部分错误但仍能产生结果的情况。
 */
object IorExamples {

    /** 创建Ior实例的不同方式 */
    fun creationExamples() {
        // 创建Left - 只有左值（通常表示错误或警告）
        val leftOnly: Ior<String, Int> = Ior.Left("Error occurred")

        // 创建Right - 只有右值（通常表示成功结果）
        val rightOnly: Ior<String, Int> = Ior.Right(42)

        // 创建Both - 同时包含左值和右值（警告+结果）
        val both: Ior<String, Int> = Ior.Both("Warning: deprecated method", 42)

        println("Left only: $leftOnly")
        println("Right only: $rightOnly")
        println("Both: $both")
    }

    /** Ior的基本操作和检查 */
    fun basicOperations() {
        val leftOnly: Ior<String, Int> = Ior.Left("Error")
        val rightOnly: Ior<String, Int> = Ior.Right(100)
        val both: Ior<String, Int> = Ior.Both("Warning", 100)

        // 检查Ior的类型
        println("leftOnly.isLeft(): ${leftOnly.isLeft()}")
        println("leftOnly.isRight(): ${leftOnly.isRight()}")
        println("leftOnly.isBoth(): ${leftOnly.isBoth()}")

        println("rightOnly.isLeft(): ${rightOnly.isLeft()}")
        println("rightOnly.isRight(): ${rightOnly.isRight()}")
        println("rightOnly.isBoth(): ${rightOnly.isBoth()}")

        println("both.isLeft(): ${both.isLeft()}")
        println("both.isRight(): ${both.isRight()}")
        println("both.isBoth(): ${both.isBoth()}")

        // 获取左值和右值
        println("leftOnly.leftOrNull(): ${leftOnly.leftOrNull()}")
        println("leftOnly.getOrNull(): ${leftOnly.getOrNull()}")

        println("rightOnly.leftOrNull(): ${rightOnly.leftOrNull()}")
        println("rightOnly.getOrNull(): ${rightOnly.getOrNull()}")

        println("both.leftOrNull(): ${both.leftOrNull()}")
        println("both.getOrNull(): ${both.getOrNull()}")
    }

    /** 使用map进行右值转换 */
    fun mapOperations() {
        val leftOnly: Ior<String, Int> = Ior.Left("Error")
        val rightOnly: Ior<String, Int> = Ior.Right(5)
        val both: Ior<String, Int> = Ior.Both("Warning", 5)

        // map操作：只对Right和Both的右值部分应用函数
        val mappedLeft = leftOnly.map { it * 2 }
        val mappedRight = rightOnly.map { it * 2 }
        val mappedBoth = both.map { it * 2 }

        println("Original left: $leftOnly")
        println("Mapped left: $mappedLeft")

        println("Original right: $rightOnly")
        println("Mapped right: $mappedRight")

        println("Original both: $both")
        println("Mapped both: $mappedBoth")

        // 链式map操作
        val chainResult = rightOnly.map { it * 2 }.map { it + 1 }.map { "Result: $it" }

        println("Chain result: $chainResult")
    }

    /** 使用mapLeft进行左值转换 */
    fun mapLeftOperations() {
        val leftOnly: Ior<String, Int> = Ior.Left("Error")
        val rightOnly: Ior<String, Int> = Ior.Right(42)
        val both: Ior<String, Int> = Ior.Both("Warning", 42)

        // mapLeft操作：只对Left和Both的左值部分应用函数
        val mappedLeft = leftOnly.mapLeft { "Mapped: $it" }
        val mappedRight = rightOnly.mapLeft { "Mapped: $it" }
        val mappedBoth = both.mapLeft { "Mapped: $it" }

        println("Original left: $leftOnly")
        println("Mapped left: $mappedLeft")

        println("Original right: $rightOnly")
        println("Mapped right: $mappedRight")

        println("Original both: $both")
        println("Mapped both: $mappedBoth")
    }

    /** 使用bimap同时转换左值和右值 */
    fun bimapOperations() {
        val leftOnly: Ior<String, Int> = Ior.Left("Error")
        val rightOnly: Ior<String, Int> = Ior.Right(42)
        val both: Ior<String, Int> = Ior.Both("Warning", 42)

        // bimap操作：同时对左值和右值应用不同的函数
        val bimappedLeft = leftOnly.bimap({ "Left: $it" }, { it * 2 })
        val bimappedRight = rightOnly.bimap({ "Left: $it" }, { it * 2 })
        val bimappedBoth = both.bimap({ "Left: $it" }, { it * 2 })

        println("Original left: $leftOnly")
        println("Bimapped left: $bimappedLeft")

        println("Original right: $rightOnly")
        println("Bimapped right: $bimappedRight")

        println("Original both: $both")
        println("Bimapped both: $bimappedBoth")
    }

    /** 使用fold进行值提取和处理 */
    fun foldOperations() {
        val leftOnly: Ior<String, Int> = Ior.Left("Error")
        val rightOnly: Ior<String, Int> = Ior.Right(42)
        val both: Ior<String, Int> = Ior.Both("Warning", 42)

        // fold操作：提供三个函数分别处理Left、Right、Both情况
        val foldLeft =
                leftOnly.fold(
                        { "Error: $it" },
                        { "Success: $it" },
                        { left, right -> "Warning: $left, Result: $right" }
                )

        val foldRight =
                rightOnly.fold(
                        { "Error: $it" },
                        { "Success: $it" },
                        { left, right -> "Warning: $left, Result: $right" }
                )

        val foldBoth =
                both.fold(
                        { "Error: $it" },
                        { "Success: $it" },
                        { left, right -> "Warning: $left, Result: $right" }
                )

        println("Fold left: $foldLeft")
        println("Fold right: $foldRight")
        println("Fold both: $foldBoth")
    }

    /** 使用pad操作进行类型转换（手动实现） */
    fun padOperations() {
        val leftOnly: Ior<String, Int> = Ior.Left("Error")
        val rightOnly: Ior<String, Int> = Ior.Right(42)
        val both: Ior<String, Int> = Ior.Both("Warning", 42)

        // padLeft: 将Right转换为Both，Left和Both保持不变
        fun <A, B> Ior<A, B>.padLeft(defaultLeft: A): Ior<A, B> =
                when (this) {
                    is Ior.Left -> this
                    is Ior.Right -> Ior.Both(defaultLeft, this.value)
                    is Ior.Both -> this
                }

        // padRight: 将Left转换为Both，Right和Both保持不变
        fun <A, B> Ior<A, B>.padRight(defaultRight: B): Ior<A, B> =
                when (this) {
                    is Ior.Left -> Ior.Both(this.value, defaultRight)
                    is Ior.Right -> this
                    is Ior.Both -> this
                }

        val paddedLeft = rightOnly.padLeft("Default warning")
        println("Original right: $rightOnly")
        println("Padded left: $paddedLeft")

        val paddedRight = leftOnly.padRight(0)
        println("Original left: $leftOnly")
        println("Padded right: $paddedRight")

        // 对Both应用pad操作不会改变
        val bothPaddedLeft = both.padLeft("Extra warning")
        val bothPaddedRight = both.padRight(999)
        println("Both padded left: $bothPaddedLeft")
        println("Both padded right: $bothPaddedRight")
    }

    /** 使用unwrap操作提取值（手动实现） */
    fun unwrapOperations() {
        val leftOnly: Ior<String, Int> = Ior.Left("Error")
        val rightOnly: Ior<String, Int> = Ior.Right(42)
        val both: Ior<String, Int> = Ior.Both("Warning", 42)

        // unwrap: 将Ior转换为Pair，Left变为(left, null)，Right变为(null, right)，Both变为(left, right)
        fun <A, B> Ior<A, B>.unwrap(): Pair<A?, B?> =
                this.fold(
                        { Pair(it, null) },
                        { Pair(null, it) },
                        { left, right -> Pair(left, right) }
                )

        val unwrappedLeft = leftOnly.unwrap()
        val unwrappedRight = rightOnly.unwrap()
        val unwrappedBoth = both.unwrap()

        println("Unwrapped left: $unwrappedLeft")
        println("Unwrapped right: $unwrappedRight")
        println("Unwrapped both: $unwrappedBoth")
    }

    /** 使用swap交换左值和右值 */
    fun swapOperations() {
        val leftOnly: Ior<String, Int> = Ior.Left("Error")
        val rightOnly: Ior<String, Int> = Ior.Right(42)
        val both: Ior<String, Int> = Ior.Both("Warning", 42)

        // swap操作：交换左值和右值的位置
        val swappedLeft = leftOnly.swap()
        val swappedRight = rightOnly.swap()
        val swappedBoth = both.swap()

        println("Original left: $leftOnly")
        println("Swapped left: $swappedLeft")

        println("Original right: $rightOnly")
        println("Swapped right: $swappedRight")

        println("Original both: $both")
        println("Swapped both: $swappedBoth")
    }

    /** 实际应用场景：带警告的计算 */
    fun calculationWithWarnings(numbers: List<Int>): Ior<List<String>, Double> {
        val warnings = mutableListOf<String>()

        if (numbers.isEmpty()) {
            return Ior.Left(listOf("Empty list provided"))
        }

        if (numbers.any { it < 0 }) {
            warnings.add("Negative numbers found, using absolute values")
        }

        if (numbers.size < 3) {
            warnings.add("Small sample size, results may not be reliable")
        }

        val processedNumbers = numbers.map { kotlin.math.abs(it) }
        val average = processedNumbers.average()

        return if (warnings.isEmpty()) {
            Ior.Right(average)
        } else {
            Ior.Both(warnings, average)
        }
    }

    /** 实际应用场景：配置解析 */
    fun parseConfiguration(config: Map<String, String>): Ior<List<String>, Configuration> {
        val warnings = mutableListOf<String>()

        val host =
                config["host"]
                        ?: run {
                            warnings.add("Host not specified, using default localhost")
                            "localhost"
                        }

        val port =
                config["port"]?.toIntOrNull()
                        ?: run {
                            warnings.add("Invalid or missing port, using default 8080")
                            8080
                        }

        val timeout =
                config["timeout"]?.toIntOrNull()
                        ?: run {
                            warnings.add("Invalid or missing timeout, using default 30s")
                            30
                        }

        if (port < 1024) {
            warnings.add("Using privileged port $port, may require admin rights")
        }

        if (timeout < 5) {
            warnings.add("Very short timeout $timeout, may cause connection issues")
        }

        val configuration = Configuration(host, port, timeout)

        return if (warnings.isEmpty()) {
            Ior.Right(configuration)
        } else {
            Ior.Both(warnings, configuration)
        }
    }

    /** 实际应用场景：数据验证和清理 */
    fun validateAndCleanData(rawData: List<String>): Ior<List<String>, List<Int>> {
        val warnings = mutableListOf<String>()
        val cleanedData = mutableListOf<Int>()

        if (rawData.isEmpty()) {
            return Ior.Left(listOf("No data provided"))
        }

        for ((index, item) in rawData.withIndex()) {
            when {
                item.isBlank() -> {
                    warnings.add("Empty item at index $index, skipping")
                }
                item.toIntOrNull() == null -> {
                    warnings.add("Invalid number '$item' at index $index, skipping")
                }
                else -> {
                    val number = item.toInt()
                    if (number < 0) {
                        warnings.add(
                                "Negative number $number at index $index, converting to positive"
                        )
                        cleanedData.add(-number)
                    } else {
                        cleanedData.add(number)
                    }
                }
            }
        }

        if (cleanedData.isEmpty()) {
            return Ior.Left(warnings + "No valid data after cleaning")
        }

        return if (warnings.isEmpty()) {
            Ior.Right(cleanedData)
        } else {
            Ior.Both(warnings, cleanedData)
        }
    }

    /** Ior的组合操作示例 */
    fun combinationOperations() {
        val result1: Ior<List<String>, Int> = Ior.Both(listOf("Warning 1"), 10)
        val result2: Ior<List<String>, Int> = Ior.Both(listOf("Warning 2"), 20)
        val result3: Ior<List<String>, Int> = Ior.Right(30)
        val error: Ior<List<String>, Int> = Ior.Left(listOf("Error occurred"))

        // 手动组合多个Ior结果
        fun combineResults(
                r1: Ior<List<String>, Int>,
                r2: Ior<List<String>, Int>,
                r3: Ior<List<String>, Int>
        ): Ior<List<String>, Int> {
            val allWarnings = mutableListOf<String>()
            val values = mutableListOf<Int>()

            // 处理第一个结果
            when (r1) {
                is Ior.Left -> return Ior.Left(r1.value)
                is Ior.Right -> values.add(r1.value)
                is Ior.Both -> {
                    allWarnings.addAll(r1.leftValue)
                    values.add(r1.rightValue)
                }
            }

            // 处理第二个结果
            when (r2) {
                is Ior.Left -> return Ior.Left(allWarnings + r2.value)
                is Ior.Right -> values.add(r2.value)
                is Ior.Both -> {
                    allWarnings.addAll(r2.leftValue)
                    values.add(r2.rightValue)
                }
            }

            // 处理第三个结果
            when (r3) {
                is Ior.Left -> return Ior.Left(allWarnings + r3.value)
                is Ior.Right -> values.add(r3.value)
                is Ior.Both -> {
                    allWarnings.addAll(r3.leftValue)
                    values.add(r3.rightValue)
                }
            }

            val sum = values.sum()
            return if (allWarnings.isEmpty()) {
                Ior.Right(sum)
            } else {
                Ior.Both(allWarnings, sum)
            }
        }

        val combinedSuccess = combineResults(result1, result2, result3)
        val combinedWithError = combineResults(result1, error, result3)

        println("Combined success: $combinedSuccess")
        println("Combined with error: $combinedWithError")
    }

    data class Configuration(val host: String, val port: Int, val timeout: Int)

    /** 演示所有示例 */
    fun runAllExamples() {
        println("=== Ior Creation Examples ===")
        creationExamples()

        println("\n=== Basic Operations ===")
        basicOperations()

        println("\n=== Map Operations ===")
        mapOperations()

        println("\n=== MapLeft Operations ===")
        mapLeftOperations()

        println("\n=== Bimap Operations ===")
        bimapOperations()

        println("\n=== Fold Operations ===")
        foldOperations()

        println("\n=== Pad Operations ===")
        padOperations()

        println("\n=== Unwrap Operations ===")
        unwrapOperations()

        println("\n=== Swap Operations ===")
        swapOperations()

        println("\n=== Practical Examples ===")
        println("Calculation with warnings (empty): ${calculationWithWarnings(emptyList())}")
        println(
                "Calculation with warnings (negative): ${calculationWithWarnings(listOf(-1, 2, -3))}"
        )
        println(
                "Calculation with warnings (normal): ${calculationWithWarnings(listOf(1, 2, 3, 4, 5))}"
        )

        val config1 = mapOf("host" to "example.com", "port" to "8080", "timeout" to "30")
        val config2 = mapOf("port" to "80", "timeout" to "5")
        println("Parse config (complete): ${parseConfiguration(config1)}")
        println("Parse config (incomplete): ${parseConfiguration(config2)}")

        val data1 = listOf("1", "2", "3", "4")
        val data2 = listOf("1", "", "invalid", "-5", "10")
        val data3 = emptyList<String>()
        println("Validate data (clean): ${validateAndCleanData(data1)}")
        println("Validate data (dirty): ${validateAndCleanData(data2)}")
        println("Validate data (empty): ${validateAndCleanData(data3)}")

        println("\n=== Combination Operations ===")
        combinationOperations()
    }
}
