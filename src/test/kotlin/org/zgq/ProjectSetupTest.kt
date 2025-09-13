package org.zgq

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import arrow.core.Some
import arrow.core.none

/**
 * 项目设置验证测试
 * 验证Arrow库和测试框架是否正确配置
 */
class ProjectSetupTest : StringSpec({
    
    "Arrow Core应该正确导入和工作" {
        val someValue = Some(42)
        val noneValue = none<Int>()
        
        someValue.isSome() shouldBe true
        noneValue.isNone() shouldBe true
    }
    
    "Kotest测试框架应该正常工作" {
        val result = "Hello, Arrow!"
        result shouldBe "Hello, Arrow!"
    }
    
    "项目基础配置验证" {
        // 验证Kotlin版本兼容性
        val kotlinVersion = KotlinVersion.CURRENT
        kotlinVersion.isAtLeast(1, 9) shouldBe true
    }
})