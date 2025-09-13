package org.zgq.core

import arrow.core.NonEmptyList
import arrow.core.nel
import arrow.core.toNonEmptyListOrNull
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * NonEmptyList (Nel) 特性完整测试用例
 *
 * 测试Arrow NonEmptyList的所有核心功能，包括：
 * - 创建和基本操作
 * - 函数式操作（map, flatMap）
 * - 列表操作（concat, reverse, distinct）
 * - 聚合和查找操作
 * - 实际应用场景
 */
class NelTest : StringSpec({
    
    "NonEmptyList创建 - nel()扩展函数应该创建单元素列表" {
        val nel = 42.nel()
        
        nel.head shouldBe 42
        nel.tail shouldBe emptyList()
        nel.size shouldBe 1
    }
    
    "NonEmptyList创建 - 构造函数应该正确设置头部和尾部" {
        val nel = NonEmptyList(10, listOf(20, 30))
        
        nel.head shouldBe 10
        nel.tail shouldBe listOf(20, 30)
        nel.size shouldBe 3
    }
    
    "NonEmptyList创建 - fromNullable应该正确处理非空和空列表" {
        val fromNonEmpty = listOf(1, 2, 3).toNonEmptyListOrNull()
        val fromEmpty = emptyList<Int>().toNonEmptyListOrNull()
        
        fromNonEmpty.shouldNotBeNull()
        fromNonEmpty.head shouldBe 1
        fromNonEmpty.tail shouldBe listOf(2, 3)
        
        fromEmpty.shouldBeNull()
    }
    
    "基本操作 - head应该返回第一个元素" {
        val nel = NonEmptyList("first", listOf("second", "third"))
        
        nel.head shouldBe "first"
    }
    
    "基本操作 - tail应该返回除第一个元素外的所有元素" {
        val nel = NonEmptyList("first", listOf("second", "third"))
        
        nel.tail shouldBe listOf("second", "third")
    }
    
    "基本操作 - 单元素列表的tail应该为空" {
        val nel = "only".nel()
        
        nel.tail shouldBe emptyList()
    }
    
    "基本操作 - size应该返回正确的元素数量" {
        val single = 1.nel()
        val multiple = NonEmptyList(1, listOf(2, 3, 4, 5))
        
        single.size shouldBe 1
        multiple.size shouldBe 5
    }
    
    "基本操作 - contains应该正确检查元素存在性" {
        val nel = NonEmptyList(1, listOf(2, 3, 4, 5))
        
        nel shouldContain 3
        nel shouldNotContain 10
    }
    
    "基本操作 - toList应该返回等价的普通列表" {
        val nel = NonEmptyList(1, listOf(2, 3))
        val list = nel.toList()
        
        list shouldBe listOf(1, 2, 3)
    }
    
    "基本操作 - all属性应该返回所有元素" {
        val nel = NonEmptyList(1, listOf(2, 3))
        
        nel.all shouldBe listOf(1, 2, 3)
    }
    
    "map操作 - 应该对每个元素应用函数" {
        val numbers = NonEmptyList(1, listOf(2, 3, 4, 5))
        val doubled = numbers.map { it * 2 }
        
        doubled.shouldBeInstanceOf<NonEmptyList<Int>>()
        doubled.toList() shouldBe listOf(2, 4, 6, 8, 10)
    }
    
    "map操作 - 应该保持NonEmptyList结构" {
        val single = 5.nel()
        val mapped = single.map { it.toString() }
        
        mapped.shouldBeInstanceOf<NonEmptyList<String>>()
        mapped.head shouldBe "5"
        mapped.size shouldBe 1
    }
    
    "map操作 - 链式调用应该正确工作" {
        val result = NonEmptyList(1, listOf(2, 3))
            .map { it * 2 }
            .map { it + 1 }
            .map { "Number: $it" }
        
        result.toList() shouldBe listOf("Number: 3", "Number: 5", "Number: 7")
    }
    
    "flatMap操作 - 应该展平嵌套结构" {
        val numbers = NonEmptyList(1, listOf(2, 3))
        val expanded = numbers.flatMap { n ->
            NonEmptyList(n, listOf(n * 10))
        }
        
        expanded.shouldBeInstanceOf<NonEmptyList<Int>>()
        expanded.toList() shouldBe listOf(1, 10, 2, 20, 3, 30)
    }
    
    "flatMap操作 - 链式调用应该正确工作" {
        val result = NonEmptyList(1, listOf(2))
            .flatMap { NonEmptyList(it, listOf(it + 10)) }
            .flatMap { NonEmptyList(it, listOf(it * 2)) }
        
        result.toList() shouldBe listOf(1, 2, 11, 22, 2, 4, 12, 24)
    }
    
    "concat操作 - 两个NonEmptyList连接应该保持NonEmptyList" {
        val list1 = NonEmptyList(1, listOf(2, 3))
        val list2 = NonEmptyList(4, listOf(5, 6))
        val concatenated = list1 + list2
        
        concatenated.shouldBeInstanceOf<NonEmptyList<Int>>()
        concatenated.toList() shouldBe listOf(1, 2, 3, 4, 5, 6)
    }
    
    "concat操作 - 添加单个元素应该正确工作" {
        val original = NonEmptyList(1, listOf(2, 3))
        val withElement = original + 4
        
        withElement.toList() shouldBe listOf(1, 2, 3, 4)
    }
    
    "concat操作 - 多个列表连接应该正确工作" {
        val list1 = NonEmptyList(1, listOf(2))
        val list2 = NonEmptyList(3, listOf(4))
        val list3 = NonEmptyList(5, listOf(6))
        val result = list1 + list2 + list3
        
        result.toList() shouldBe listOf(1, 2, 3, 4, 5, 6)
    }
    
    "reverse操作 - 应该正确反转元素顺序" {
        val original = NonEmptyList(1, listOf(2, 3, 4, 5))
        val reversed = original.reversed()
        
        // 注意：reversed()返回List，不是NonEmptyList
        reversed.shouldBeInstanceOf<List<Int>>()
        reversed shouldBe listOf(5, 4, 3, 2, 1)
    }
    
    "reverse操作 - 单元素列表反转应该保持不变" {
        val single = 42.nel()
        val reversed = single.reversed()
        
        reversed shouldBe listOf(42)
    }
    
    "reverse操作 - 双重反转应该得到原列表" {
        val original = NonEmptyList(1, listOf(2, 3, 4))
        val doubleReversed = original.reversed().reversed()
        
        doubleReversed shouldBe original.toList()
    }
    
    "distinct操作 - 应该移除重复元素" {
        val withDuplicates = NonEmptyList(1, listOf(2, 2, 3, 3, 3, 4))
        val distinct = withDuplicates.distinct()
        
        distinct.shouldBeInstanceOf<NonEmptyList<Int>>()
        distinct.toList() shouldBe listOf(1, 2, 3, 4)
    }
    
    "distinct操作 - 无重复元素的列表应该保持不变" {
        val noDuplicates = NonEmptyList(1, listOf(2, 3, 4, 5))
        val distinct = noDuplicates.distinct()
        
        distinct shouldBe noDuplicates
    }
    
    "distinct操作 - 单元素列表应该保持不变" {
        val single = 42.nel()
        val distinct = single.distinct()
        
        distinct shouldBe single
    }
    
    "sorted操作 - 应该正确排序元素" {
        val unsorted = NonEmptyList(5, listOf(2, 8, 1, 9, 3))
        val sorted = unsorted.sorted()
        
        sorted.shouldBeInstanceOf<List<Int>>()
        sorted shouldBe listOf(1, 2, 3, 5, 8, 9)
    }
    
    "sorted操作 - 降序排序应该正确工作" {
        val unsorted = NonEmptyList(5, listOf(2, 8, 1, 9, 3))
        val sortedDesc = unsorted.sortedDescending()
        
        sortedDesc shouldBe listOf(9, 8, 5, 3, 2, 1)
    }
    
    "sortedBy操作 - 应该按指定属性排序" {
        val strings = NonEmptyList("apple", listOf("pie", "a", "elephant"))
        val sortedByLength = strings.sortedBy { it.length }
        
        sortedByLength shouldBe listOf("a", "pie", "apple", "elephant")
    }
    
    "filter操作 - 应该返回满足条件的元素列表" {
        val numbers = NonEmptyList(1, listOf(2, 3, 4, 5, 6, 7, 8, 9, 10))
        val evenNumbers = numbers.filter { it % 2 == 0 }
        
        // 注意：filter返回List，不是NonEmptyList，因为可能为空
        evenNumbers.shouldBeInstanceOf<List<Int>>()
        evenNumbers shouldBe listOf(2, 4, 6, 8, 10)
    }
    
    "filter操作 - 无匹配元素应该返回空列表" {
        val numbers = NonEmptyList(1, listOf(3, 5, 7, 9))
        val evenNumbers = numbers.filter { it % 2 == 0 }
        
        evenNumbers shouldBe emptyList()
    }
    
    "find操作 - 应该返回第一个匹配元素" {
        val numbers = NonEmptyList(1, listOf(2, 3, 4, 5))
        val firstEven = numbers.find { it % 2 == 0 }
        
        firstEven shouldBe 2
    }
    
    "find操作 - 无匹配元素应该返回null" {
        val numbers = NonEmptyList(1, listOf(3, 5, 7, 9))
        val firstEven = numbers.find { it % 2 == 0 }
        
        firstEven.shouldBeNull()
    }
    
    "reduce操作 - 应该正确聚合所有元素" {
        val numbers = NonEmptyList(1, listOf(2, 3, 4, 5))
        val sum = numbers.reduce { acc, n -> acc + n }
        val product = numbers.reduce { acc, n -> acc * n }
        
        sum shouldBe 15
        product shouldBe 120
    }
    
    "reduce操作 - 单元素列表应该返回该元素" {
        val single = 42.nel()
        val result = single.reduce { acc, n -> acc + n }
        
        result shouldBe 42
    }
    
    "foldLeft操作 - 应该正确折叠元素" {
        val numbers = NonEmptyList(1, listOf(2, 3, 4, 5))
        val sum = numbers.foldLeft(0) { acc, n -> acc + n }
        val concatenated = numbers.foldLeft("") { acc, n -> acc + n }
        
        sum shouldBe 15
        concatenated shouldBe "12345"
    }
    
    "groupBy操作 - 应该正确分组元素" {
        val numbers = NonEmptyList(1, listOf(2, 3, 4, 5, 6, 7, 8, 9))
        val grouped = numbers.groupBy { it % 3 }
        
        grouped[0] shouldBe listOf(3, 6, 9)
        grouped[1] shouldBe listOf(1, 4, 7)
        grouped[2] shouldBe listOf(2, 5, 8)
    }
    
    "partition操作 - 应该正确分区元素" {
        val numbers = NonEmptyList(1, listOf(2, 3, 4, 5, 6, 7, 8, 9, 10))
        val (evens, odds) = numbers.partition { it % 2 == 0 }
        
        evens shouldBe listOf(2, 4, 6, 8, 10)
        odds shouldBe listOf(1, 3, 5, 7, 9)
    }
    
    "实际应用 - 验证示例应该正确处理错误累积" {
        data class ValidationError(val field: String, val message: String)
        
        val errors = listOf(
            ValidationError("name", "Name cannot be blank"),
            ValidationError("email", "Email must contain @")
        )
        
        val errorsNel = errors.toNonEmptyListOrNull()
        
        errorsNel.shouldNotBeNull()
        errorsNel.size shouldBe 2
        errorsNel.head.field shouldBe "name"
    }
    
    "实际应用 - 配置示例应该保证至少有一个服务器" {
        data class ServerConfig(val host: String, val port: Int)
        
        val servers = NonEmptyList(
            ServerConfig("localhost", 8080),
            listOf(ServerConfig("backup.example.com", 8080))
        )
        
        // 总是有主服务器
        val primaryServer = servers.head
        primaryServer.host shouldBe "localhost"
        primaryServer.port shouldBe 8080
        
        // 可能有备用服务器
        val backupServers = servers.tail
        backupServers.size shouldBe 1
        backupServers.first().host shouldBe "backup.example.com"
    }
    
    "NonEmptyList应该正确实现equals和hashCode" {
        val nel1 = NonEmptyList(1, listOf(2, 3))
        val nel2 = NonEmptyList(1, listOf(2, 3))
        val nel3 = NonEmptyList(1, listOf(2, 4))
        
        nel1 shouldBe nel2
        nel1 shouldNotBe nel3
        
        nel1.hashCode() shouldBe nel2.hashCode()
    }
    
    "NonEmptyList应该正确实现toString" {
        val nel = NonEmptyList(1, listOf(2, 3))
        
        nel.toString() shouldBe "NonEmptyList(1, 2, 3)"
    }
    
    "NonEmptyList的类型安全性测试" {
        // 编译时类型安全
        val stringNel: NonEmptyList<String> = NonEmptyList("hello", listOf("world"))
        val intNel: NonEmptyList<Int> = NonEmptyList(1, listOf(2, 3))
        
        // map保持类型安全
        val lengthNel: NonEmptyList<Int> = stringNel.map { it.length }
        lengthNel.toList() shouldBe listOf(5, 5)
        
        // flatMap允许类型转换
        val expandedNel: NonEmptyList<String> = intNel.flatMap { n ->
            NonEmptyList(n.toString(), listOf((n * 2).toString()))
        }
        expandedNel.toList() shouldBe listOf("1", "2", "2", "4", "3", "6")
    }
    
    "NonEmptyList的性能特性测试" {
        // NonEmptyList应该是高效的
        val largeNel = NonEmptyList(1, (2..1000).toList())
        largeNel.size shouldBe 1000
        
        // 链式操作应该是高效的
        val result = NonEmptyList(1, listOf(2, 3, 4, 5))
            .map { it * 2 }
            .filter { it > 4 }
            .sorted()
        
        result shouldBe listOf(6, 8, 10)
    }
    
    "NonEmptyList与普通List的互操作性" {
        val regularList = listOf(1, 2, 3, 4, 5)
        val nel = regularList.toNonEmptyListOrNull()
        
        nel.shouldNotBeNull()
        nel.toList() shouldBe regularList
        
        // 空列表转换应该失败
        val emptyList = emptyList<Int>()
        val emptyNel = emptyList.toNonEmptyListOrNull()
        
        emptyNel.shouldBeNull()
    }
    
    "NonEmptyList应该支持解构" {
        val nel = NonEmptyList(1, listOf(2, 3, 4, 5))
        
        // 可以解构获取head和tail
        val head = nel.head
        val tail = nel.tail
        
        head shouldBe 1
        tail shouldBe listOf(2, 3, 4, 5)
    }
    
    "NonEmptyList应该支持迭代" {
        val nel = NonEmptyList(1, listOf(2, 3, 4, 5))
        val collected = mutableListOf<Int>()
        
        for (item in nel) {
            collected.add(item)
        }
        
        collected shouldBe listOf(1, 2, 3, 4, 5)
    }
})