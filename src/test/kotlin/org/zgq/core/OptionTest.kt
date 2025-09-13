package org.zgq.core

import arrow.core.Option
import arrow.core.Some
import arrow.core.none
import arrow.core.some
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * Option特性完整测试用例
 *
 * 测试Arrow Option的所有核心功能，包括：
 * - 创建和基本操作
 * - 函数式操作（map, flatMap, filter）
 * - 值提取和处理
 * - 组合操作
 * - 实际应用场景
 */
class OptionTest :
        StringSpec({
            "Option创建 - Some应该包含值" {
                val some = Some("test")
                some.isSome() shouldBe true
                some.isNone() shouldBe false
                some.getOrNull() shouldBe "test"
            }

            "Option创建 - None应该为空" {
                val none = none<String>()
                none.isSome() shouldBe false
                none.isNone() shouldBe true
                none.getOrNull() shouldBe null
            }

            "Option创建 - 使用扩展函数some()" {
                val some = 42.some()
                some.isSome() shouldBe true
                some.getOrNull() shouldBe 42
            }

            "Option创建 - fromNullable应该正确处理null和非null值" {
                val fromValue = Option.fromNullable("hello")
                val fromNull = Option.fromNullable(null)

                fromValue.isSome() shouldBe true
                fromValue.getOrNull() shouldBe "hello"

                fromNull.isNone() shouldBe true
                fromNull.getOrNull() shouldBe null
            }

            "fold应该在Some时返回值，在None时返回默认值" {
                val some = "value".some()
                val none = none<String>()

                some.fold({ "default" }) { it } shouldBe "value"
                none.fold({ "default" }) { it } shouldBe "default"
            }

            "map操作 - Some应该应用函数" {
                val some = 5.some()
                val result = some.map { it * 2 }

                result.shouldBeInstanceOf<Some<Int>>()
                result.getOrNull() shouldBe 10
            }

            "map操作 - None应该保持None" {
                val none = none<Int>()
                val result = none.map { it * 2 }

                result.isNone() shouldBe true
            }

            "map操作 - 链式调用应该正确工作" {
                val result = 3.some().map { it * 2 }.map { it + 1 }.map { it.toString() }

                result.getOrNull() shouldBe "7"
            }

            "flatMap操作 - 应该避免嵌套Option" {
                val some = 10.some()
                val result = some.flatMap { n -> if (n > 5) (n * 2).some() else none() }

                result.getOrNull() shouldBe 20
            }

            "flatMap操作 - 链式调用应该正确工作" {
                val result =
                        5.some().flatMap { if (it > 0) it.some() else none() }.flatMap {
                            if (it < 10) (it * 2).some() else none()
                        }

                result.getOrNull() shouldBe 10
            }

            "flatMap操作 - 任何一步返回None都应该导致最终结果为None" {
                val result =
                        15.some().flatMap { if (it > 0) it.some() else none() }.flatMap {
                            if (it < 10) (it * 2).some() else none()
                        } // 这里会返回None

                result.isNone() shouldBe true
            }

            "filter操作 - 满足条件应该保持Some" {
                val even = 8.some()
                val filtered = even.filter { it % 2 == 0 }

                filtered.getOrNull() shouldBe 8
            }

            "filter操作 - 不满足条件应该变为None" {
                val odd = 7.some()
                val filtered = odd.filter { it % 2 == 0 }

                filtered.isNone() shouldBe true
            }

            "filter操作 - None应该保持None" {
                val none = none<Int>()
                val filtered = none.filter { it % 2 == 0 }

                filtered.isNone() shouldBe true
            }

            "fold操作 - Some应该执行ifSome分支" {
                val some = "hello".some()
                val result = some.fold(ifEmpty = { "empty" }, ifSome = { "value: $it" })

                result shouldBe "value: hello"
            }

            "fold操作 - None应该执行ifEmpty分支" {
                val none = none<String>()
                val result = none.fold(ifEmpty = { "empty" }, ifSome = { "value: $it" })

                result shouldBe "empty"
            }

            "实际应用 - 安全除法应该正确处理除零情况" {
                val validDivision = OptionExamples.safeDivision(10, 2)
                val invalidDivision = OptionExamples.safeDivision(10, 0)
                validDivision.getOrNull() shouldBe 5.0
                invalidDivision.isNone() shouldBe true
            }

            "实际应用 - 安全字符串解析应该正确处理有效和无效输入" {
                val validParse = OptionExamples.safeParseInt("123")
                val invalidParse = OptionExamples.safeParseInt("abc")

                validParse.getOrNull() shouldBe 123
                invalidParse.isNone() shouldBe true
            }

            "实际应用 - 复杂计算应该正确组合多个操作" {
                val validCalculation = OptionExamples.complexCalculation("1", "2", "3")
                val invalidCalculation = OptionExamples.complexCalculation("1", "x", "3")

                validCalculation.getOrNull() shouldBe 6
                invalidCalculation.isNone() shouldBe true
            }

            "Option应该正确实现equals和hashCode" {
                val some1 = "test".some()
                val some2 = "test".some()
                val some3 = "other".some()
                val none1 = none<String>()
                val none2 = none<String>()

                some1 shouldBe some2
                some1 shouldNotBe some3
                some1 shouldNotBe none1
                none1 shouldBe none2

                some1.hashCode() shouldBe some2.hashCode()
                none1.hashCode() shouldBe none2.hashCode()
            }

            "Option应该正确实现toString" {
                val some = "test".some()
                val none = none<String>()

                some.toString() shouldBe "Option.Some(test)"
                none.toString() shouldBe "Option.None"
            }

            "Option的类型安全性测试" {
                // 编译时类型安全
                val stringOption: Option<String> = "hello".some()
                val intOption: Option<Int> = 42.some()

                // map保持类型安全
                val lengthOption: Option<Int> = stringOption.map { it.length }
                lengthOption.getOrNull() shouldBe 5

                // flatMap允许类型转换
                val doubleOption: Option<Double> = intOption.flatMap { (it * 2.5).some() }
                doubleOption.getOrNull() shouldBe 105.0
            }

            "Option的性能特性测试" {
                // Option应该是轻量级的
                val options = (1..1000).map { it.some() }
                options.size shouldBe 1000

                // 链式操作应该是惰性的
                val result = 1.some().map { it + 1 }.map { it * 2 }.map { it - 1 }.getOrNull()

                result shouldBe 3
            }
        })