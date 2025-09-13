package org.zgq.core

import arrow.core.NonEmptyList
import arrow.core.nel
import arrow.core.toNonEmptyListOrNull

/**
 * NonEmptyList (Nel) 特性示例
 * 
 * NonEmptyList是Arrow中的非空列表类型，保证至少包含一个元素。
 * 它提供了类型安全的方式来处理不能为空的列表，避免空列表相关的运行时错误。
 */
object NelExamples {
    
    /**
     * 创建NonEmptyList实例的不同方式
     */
    fun creationExamples() {
        // 使用nel()扩展函数创建单元素列表
        val singleElement = 1.nel()
        
        // 从普通列表创建（可能失败）
        val fromList = listOf(1, 2, 3).toNonEmptyListOrNull()
        val fromEmptyList = emptyList<Int>().toNonEmptyListOrNull()
        
        // 使用NonEmptyList构造函数
        val constructed = NonEmptyList(1, listOf(2, 3, 4))
        
        println("Single element: $singleElement")
        println("From list: $fromList")
        println("From empty list: $fromEmptyList")
        println("Constructed: $constructed")
    }
    
    /**
     * NonEmptyList的基本操作
     */
    fun basicOperations() {
        val nel = NonEmptyList("first", listOf("second", "third"))
        
        // 访问头部和尾部
        println("Head: ${nel.head}")
        println("Tail: ${nel.tail}")
        println("Size: ${nel.size}")
        
        // 检查元素
        println("Contains 'second': ${"second" in nel}")
        println("Contains 'fourth': ${"fourth" in nel}")
        
        // 转换为普通列表
        println("To list: ${nel.toList()}")
        
        // 获取所有元素
        println("All elements: ${nel.all}")
    }
    
    /**
     * 使用map进行函数式转换
     */
    fun mapOperations() {
        val numbers = NonEmptyList(1, listOf(2, 3, 4, 5))
        
        // map操作：对每个元素应用函数
        val doubled = numbers.map { it * 2 }
        val strings = numbers.map { "Number: $it" }
        
        println("Original: $numbers")
        println("Doubled: $doubled")
        println("Strings: $strings")
        
        // 链式map操作
        val result = numbers
            .map { it * 2 }
            .map { it + 1 }
            .map { "Result: $it" }
        
        println("Chain result: $result")
    }
    
    /**
     * 使用flatMap进行单子操作
     */
    fun flatMapOperations() {
        val numbers = NonEmptyList(1, listOf(2, 3))
        
        // flatMap用于展平嵌套结构
        val expanded = numbers.flatMap { n ->
            NonEmptyList(n, listOf(n * 10))
        }
        
        println("Original: $numbers")
        println("Expanded: $expanded")
        
        // 链式flatMap操作
        val chainResult = numbers
            .flatMap { NonEmptyList(it, listOf(it + 10)) }
            .flatMap { NonEmptyList(it, listOf(it * 2)) }
        
        println("Chain flatMap result: $chainResult")
    }
    
    /**
     * 列表连接操作
     */
    fun concatOperations() {
        val list1 = NonEmptyList(1, listOf(2, 3))
        val list2 = NonEmptyList(4, listOf(5, 6))
        val list3 = NonEmptyList(7, listOf(8, 9))
        
        // 连接两个NonEmptyList
        val concatenated = list1 + list2
        
        println("List1: $list1")
        println("List2: $list2")
        println("Concatenated: $concatenated")
        
        // 连接多个列表
        val multiConcat = list1 + list2 + list3
        println("Multi concatenated: $multiConcat")
        
        // 添加单个元素
        val withElement = list1 + 10
        println("With element: $withElement")
        
        // 前置元素
        val prepended = 0.nel() + list1
        println("Prepended: $prepended")
    }
    
    /**
     * 列表反转操作
     */
    fun reverseOperations() {
        val original = NonEmptyList(1, listOf(2, 3, 4, 5))
        val reversed = original.reversed()
        
        println("Original: $original")
        println("Reversed: $reversed")
        
        // 反转后再反转应该得到原列表
        val doubleReversed = reversed.toNonEmptyListOrNull()?.reversed()
        println("Double reversed: $doubleReversed")
        println("Original equals double reversed: ${original.toList() == doubleReversed}")
    }
    
    /**
     * 去重操作
     */
    fun distinctOperations() {
        val withDuplicates = NonEmptyList(1, listOf(2, 2, 3, 3, 3, 4, 4, 4, 4))
        val distinct = withDuplicates.distinct()
        
        println("With duplicates: $withDuplicates")
        println("Distinct: $distinct")
        
        // 字符串去重
        val stringList = NonEmptyList("apple", listOf("banana", "apple", "cherry", "banana"))
        val distinctStrings = stringList.distinct()
        
        println("String list: $stringList")
        println("Distinct strings: $distinctStrings")
    }
    
    /**
     * 排序操作
     */
    fun sortOperations() {
        val unsorted = NonEmptyList(5, listOf(2, 8, 1, 9, 3))
        val sorted = unsorted.sorted()
        val sortedDesc = unsorted.sortedDescending()
        
        println("Unsorted: $unsorted")
        println("Sorted: $sorted")
        println("Sorted descending: $sortedDesc")
        
        // 自定义排序
        val strings = NonEmptyList("apple", listOf("pie", "a", "elephant"))
        val sortedByLength = strings.sortedBy { it.length }
        
        println("Strings: $strings")
        println("Sorted by length: $sortedByLength")
    }
    
    /**
     * 过滤和查找操作
     */
    fun filterOperations() {
        val numbers = NonEmptyList(1, listOf(2, 3, 4, 5, 6, 7, 8, 9, 10))
        
        // 注意：filter可能返回空列表，所以返回List而不是NonEmptyList
        val evenNumbers = numbers.filter { it % 2 == 0 }
        val largeNumbers = numbers.filter { it > 5 }
        
        println("Numbers: $numbers")
        println("Even numbers: $evenNumbers")
        println("Large numbers: $largeNumbers")
        
        // 查找操作
        val firstEven = numbers.find { it % 2 == 0 }
        val firstLarge = numbers.find { it > 15 }
        
        println("First even: $firstEven")
        println("First large: $firstLarge")
    }
    
    /**
     * 折叠和聚合操作
     */
    fun foldOperations() {
        val numbers = NonEmptyList(1, listOf(2, 3, 4, 5))
        
        // reduce操作（NonEmptyList保证至少有一个元素，所以reduce是安全的）
        val sum = numbers.reduce { acc, n -> acc + n }
        val product = numbers.reduce { acc, n -> acc * n }
        
        println("Numbers: $numbers")
        println("Sum: $sum")
        println("Product: $product")
        
        // fold操作
        val foldSum = numbers.foldLeft(0) { acc, n -> acc + n }
        val concatenated = numbers.foldLeft("") { acc, n -> acc + n }
        
        println("Fold sum: $foldSum")
        println("Concatenated: $concatenated")
    }
    
    /**
     * 分组和分区操作
     */
    fun groupingOperations() {
        val numbers = NonEmptyList(1, listOf(2, 3, 4, 5, 6, 7, 8, 9, 10))
        
        // 分组
        val grouped = numbers.groupBy { it % 3 }
        println("Numbers: $numbers")
        println("Grouped by mod 3: $grouped")
        
        // 分区
        val (evens, odds) = numbers.partition { it % 2 == 0 }
        println("Evens: $evens")
        println("Odds: $odds")
    }
    
    /**
     * 实际应用场景：处理用户输入验证
     */
    fun validationExample() {
        data class ValidationError(val field: String, val message: String)
        
        fun validateName(name: String): List<ValidationError> {
            val errors = mutableListOf<ValidationError>()
            if (name.isBlank()) errors.add(ValidationError("name", "Name cannot be blank"))
            if (name.length < 2) errors.add(ValidationError("name", "Name must be at least 2 characters"))
            return errors
        }
        
        fun validateEmail(email: String): List<ValidationError> {
            val errors = mutableListOf<ValidationError>()
            if (!email.contains("@")) errors.add(ValidationError("email", "Email must contain @"))
            if (email.length < 5) errors.add(ValidationError("email", "Email too short"))
            return errors
        }
        
        fun validateUser(name: String, email: String): List<ValidationError> {
            return validateName(name) + validateEmail(email)
        }
        
        // 测试验证
        val validUser = validateUser("John Doe", "john@example.com")
        val invalidUser = validateUser("", "bad")
        
        println("Valid user errors: $validUser")
        println("Invalid user errors: $invalidUser")
        
        // 如果有错误，创建NonEmptyList
        val errorsNel = invalidUser.toNonEmptyListOrNull()
        println("Errors as Nel: $errorsNel")
    }
    
    /**
     * 实际应用场景：处理配置选项
     */
    fun configurationExample() {
        data class ServerConfig(val host: String, val port: Int)
        
        // 默认服务器配置（至少有一个）
        val defaultServers = NonEmptyList(
            ServerConfig("localhost", 8080),
            listOf(ServerConfig("backup.example.com", 8080))
        )
        
        // 添加额外服务器
        fun addServer(servers: NonEmptyList<ServerConfig>, newServer: ServerConfig): NonEmptyList<ServerConfig> {
            return servers + newServer
        }
        
        // 获取主服务器（总是存在）
        fun getPrimaryServer(servers: NonEmptyList<ServerConfig>): ServerConfig = servers.head
        
        // 获取备用服务器
        fun getBackupServers(servers: NonEmptyList<ServerConfig>): List<ServerConfig> = servers.tail
        
        val servers = addServer(defaultServers, ServerConfig("cache.example.com", 6379))
        
        println("All servers: $servers")
        println("Primary server: ${getPrimaryServer(servers)}")
        println("Backup servers: ${getBackupServers(servers)}")
    }
    
    /**
     * 演示所有示例
     */
    fun runAllExamples() {
        println("=== NonEmptyList Creation Examples ===")
        creationExamples()
        
        println("\n=== Basic Operations ===")
        basicOperations()
        
        println("\n=== Map Operations ===")
        mapOperations()
        
        println("\n=== FlatMap Operations ===")
        flatMapOperations()
        
        println("\n=== Concat Operations ===")
        concatOperations()
        
        println("\n=== Reverse Operations ===")
        reverseOperations()
        
        println("\n=== Distinct Operations ===")
        distinctOperations()
        
        println("\n=== Sort Operations ===")
        sortOperations()
        
        println("\n=== Filter Operations ===")
        filterOperations()
        
        println("\n=== Fold Operations ===")
        foldOperations()
        
        println("\n=== Grouping Operations ===")
        groupingOperations()
        
        println("\n=== Validation Example ===")
        validationExample()
        
        println("\n=== Configuration Example ===")
        configurationExample()
    }
}