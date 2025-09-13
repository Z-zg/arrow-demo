package org.zgq.optics

import arrow.optics.Lens
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.zgq.optics.LensExamples.Address
import org.zgq.optics.LensExamples.Preferences
import org.zgq.optics.LensExamples.Profile
import org.zgq.optics.LensExamples.User
import org.zgq.optics.LensExamples.addressCityLens
import org.zgq.optics.LensExamples.addressCountryLens
import org.zgq.optics.LensExamples.preferencesThemeLens
import org.zgq.optics.LensExamples.profileAddressLens
import org.zgq.optics.LensExamples.profileAgeLens
import org.zgq.optics.LensExamples.userEmailLens
import org.zgq.optics.LensExamples.userNameLens
import org.zgq.optics.LensExamples.userProfileLens

/**
 * Lens透镜完整测试用例
 *
 * 测试Arrow Optics Lens的所有核心功能，包括：
 * - 基本操作（get, set, modify）
 * - Lens组合和嵌套访问
 * - 链式操作和批量更新
 * - 实际应用场景
 * - 性能和类型安全性
 */
class LensTest : StringSpec({
    
    // 测试数据
    val testUser = User(
        id = 1L,
        name = "TestUser",
        email = "test@example.com",
        profile = Profile(
            age = 25,
            address = Address("123 Test St", "TestCity", "TestCountry", "12345"),
            preferences = Preferences("dark", "en", true)
        )
    )
    
    "Lens get操作应该正确获取字段值" {
        userNameLens.get(testUser) shouldBe "TestUser"
        userEmailLens.get(testUser) shouldBe "test@example.com"
        profileAgeLens.get(testUser.profile) shouldBe 25
        addressCityLens.get(testUser.profile.address) shouldBe "TestCity"
    }
    
    "Lens set操作应该正确设置新值并保持不可变性" {
        val originalUser = testUser
        val updatedUser = userNameLens.set(testUser, "NewName")
        
        // 验证新值被设置
        updatedUser.name shouldBe "NewName"
        
        // 验证原对象未被修改（不可变性）
        originalUser.name shouldBe "TestUser"
        originalUser shouldNotBe updatedUser
        
        // 验证其他字段保持不变
        updatedUser.id shouldBe originalUser.id
        updatedUser.email shouldBe originalUser.email
        updatedUser.profile shouldBe originalUser.profile
    }
    
    "Lens set操作应该正确处理嵌套字段" {
        val updatedProfile = profileAgeLens.set(testUser.profile, 30)
        updatedProfile.age shouldBe 30
        updatedProfile.address shouldBe testUser.profile.address
        updatedProfile.preferences shouldBe testUser.profile.preferences
        
        val updatedAddress = addressCityLens.set(testUser.profile.address, "NewCity")
        updatedAddress.city shouldBe "NewCity"
        updatedAddress.street shouldBe testUser.profile.address.street
        updatedAddress.country shouldBe testUser.profile.address.country
        updatedAddress.zipCode shouldBe testUser.profile.address.zipCode
    }
    
    "Lens modify操作应该基于当前值进行修改" {
        val modifiedUser = userNameLens.modify(testUser) { currentName ->
            currentName.uppercase()
        }
        
        modifiedUser.name shouldBe "TESTUSER"
        
        // 测试数值修改
        val modifiedProfile = profileAgeLens.modify(testUser.profile) { currentAge ->
            currentAge + 5
        }
        
        modifiedProfile.age shouldBe 30
    }
    
    "Lens modify操作应该支持复杂的修改逻辑" {
        val modifiedUser = userEmailLens.modify(testUser) { currentEmail ->
            currentEmail.replace("@example.com", "@newdomain.com")
        }
        
        modifiedUser.email shouldBe "test@newdomain.com"
        
        // 测试条件修改
        val conditionalModify = profileAgeLens.modify(testUser.profile) { age ->
            if (age < 30) age + 1 else age
        }
        
        conditionalModify.age shouldBe 26 // 25 + 1
    }
    
    "Lens组合应该正确访问嵌套字段" {
        // 组合Lens：User -> Profile -> Age
        val userAgeLens = userProfileLens compose profileAgeLens
        
        userAgeLens.get(testUser) shouldBe 25
        
        val userWithNewAge = userAgeLens.set(testUser, 35)
        userWithNewAge.profile.age shouldBe 35
        
        // 验证其他字段未被影响
        userWithNewAge.name shouldBe testUser.name
        userWithNewAge.email shouldBe testUser.email
        userWithNewAge.profile.address shouldBe testUser.profile.address
    }
    
    "深层Lens组合应该正确工作" {
        // 组合Lens：User -> Profile -> Address -> City
        val userCityLens = userProfileLens compose profileAddressLens compose addressCityLens
        
        userCityLens.get(testUser) shouldBe "TestCity"
        
        val userWithNewCity = userCityLens.set(testUser, "NewCity")
        userWithNewCity.profile.address.city shouldBe "NewCity"
        
        // 验证其他嵌套字段未被影响
        userWithNewCity.profile.address.street shouldBe testUser.profile.address.street
        userWithNewCity.profile.address.country shouldBe testUser.profile.address.country
        userWithNewCity.profile.age shouldBe testUser.profile.age
    }
    
    "多层Lens组合的modify操作应该正确工作" {
        val userCountryLens = userProfileLens compose profileAddressLens compose addressCountryLens
        
        val modifiedUser = userCountryLens.modify(testUser) { country ->
            country.uppercase()
        }
        
        modifiedUser.profile.address.country shouldBe "TESTCOUNTRY"
        
        // 验证原始数据未被修改
        testUser.profile.address.country shouldBe "TestCountry"
    }
    
    "多个Lens操作应该正确组合" {
        val updatedUser = testUser
            .let { userNameLens.set(it, "UpdatedName") }
            .let { userEmailLens.set(it, "updated@example.com") }
            .let { (userProfileLens compose profileAgeLens).set(it, 30) }
        
        updatedUser.name shouldBe "UpdatedName"
        updatedUser.email shouldBe "updated@example.com"
        updatedUser.profile.age shouldBe 30
        
        // 验证未修改的字段保持不变
        updatedUser.id shouldBe testUser.id
        updatedUser.profile.address shouldBe testUser.profile.address
        updatedUser.profile.preferences shouldBe testUser.profile.preferences
    }
    
    "链式modify操作应该正确工作" {
        val modifiedUser = testUser
            .let { userNameLens.modify(it) { name -> name.lowercase() } }
            .let { (userProfileLens compose profileAgeLens).modify(it) { age -> age * 2 } }
        
        modifiedUser.name shouldBe "testuser"
        modifiedUser.profile.age shouldBe 50
    }
    
    "Lens应该保持引用透明性" {
        val lens = userNameLens
        val user = testUser
        val newName = "NewName"
        
        // 多次调用应该产生相同结果
        val result1 = lens.set(user, newName)
        val result2 = lens.set(user, newName)
        
        result1 shouldBe result2
        result1.name shouldBe newName
        result2.name shouldBe newName
    }
    
    "Lens get和set应该满足Lens法则" {
        val lens = userNameLens
        val user = testUser
        val newName = "NewName"
        
        // 法则1：get(set(s, a)) == a
        val setResult = lens.set(user, newName)
        lens.get(setResult) shouldBe newName
        
        // 法则2：set(s, get(s)) == s
        val currentName = lens.get(user)
        val setCurrentResult = lens.set(user, currentName)
        setCurrentResult shouldBe user
        
        // 法则3：set(set(s, a), b) == set(s, b)
        val firstSet = lens.set(user, "FirstName")
        val secondSet = lens.set(firstSet, "SecondName")
        val directSet = lens.set(user, "SecondName")
        secondSet shouldBe directSet
    }
    
    "嵌套Lens应该正确处理复杂数据结构" {
        val complexUser = User(
            id = 2L,
            name = "ComplexUser",
            email = "complex@example.com",
            profile = Profile(
                age = 40,
                address = Address("456 Complex Ave", "ComplexCity", "ComplexCountry", "67890"),
                preferences = Preferences("light", "zh", false)
            )
        )
        
        // 定义preferences lens
        val profilePreferencesLens: Lens<Profile, Preferences> = Lens(
            get = { profile -> profile.preferences },
            set = { profile, preferences -> profile.copy(preferences = preferences) }
        )
        
        // 测试多个嵌套字段的修改
        val step1 = userNameLens.set(complexUser, "UpdatedComplex")
        val step2 = (userProfileLens compose profileAgeLens).set(step1, 45)
        val step3 = (userProfileLens compose profileAddressLens compose addressCityLens).set(step2, "NewComplexCity")
        val updatedUser = (userProfileLens compose profilePreferencesLens compose preferencesThemeLens).set(step3, "dark")
        
        updatedUser.name shouldBe "UpdatedComplex"
        updatedUser.profile.age shouldBe 45
        updatedUser.profile.address.city shouldBe "NewComplexCity"
        updatedUser.profile.preferences.theme shouldBe "dark"
        
        // 验证未修改的字段
        updatedUser.id shouldBe complexUser.id
        updatedUser.email shouldBe complexUser.email
        updatedUser.profile.address.street shouldBe complexUser.profile.address.street
        updatedUser.profile.preferences.language shouldBe complexUser.profile.preferences.language
    }
    
    "Lens应该支持函数式编程模式" {
        // 创建一个函数来更新用户年龄
        fun incrementAge(user: User): User = 
            (userProfileLens compose profileAgeLens).modify(user) { it + 1 }
        
        // 创建一个函数来标准化邮箱
        fun normalizeEmail(user: User): User = 
            userEmailLens.modify(user) { it.lowercase() }
        
        // 组合函数
        val processUser = { user: User ->
            user
                .let(::incrementAge)
                .let(::normalizeEmail)
        }
        
        val testUserWithUpperEmail = testUser.copy(email = "TEST@EXAMPLE.COM")
        val processedUser = processUser(testUserWithUpperEmail)
        
        processedUser.profile.age shouldBe 26
        processedUser.email shouldBe "test@example.com"
    }
    
    "Lens应该正确处理边界情况" {
        // 测试空字符串
        val userWithEmptyName = userNameLens.set(testUser, "")
        userWithEmptyName.name shouldBe ""
        
        // 测试特殊字符
        val userWithSpecialName = userNameLens.set(testUser, "用户@#$%")
        userWithSpecialName.name shouldBe "用户@#$%"
        
        // 测试数值边界
        val userWithZeroAge = (userProfileLens compose profileAgeLens).set(testUser, 0)
        userWithZeroAge.profile.age shouldBe 0
        
        val userWithNegativeAge = (userProfileLens compose profileAgeLens).set(testUser, -1)
        userWithNegativeAge.profile.age shouldBe -1
    }
    
    "Lens性能应该优于手动嵌套复制" {
        // 这个测试主要验证Lens的正确性，实际性能测试需要基准测试工具
        val iterations = 1000
        
        // 使用Lens的方式
        val lensResults = (1..iterations).map { i ->
            (userProfileLens compose profileAddressLens compose addressCityLens)
                .set(testUser, "City$i")
        }
        
        // 验证结果正确性
        lensResults.size shouldBe iterations
        lensResults.first().profile.address.city shouldBe "City1"
        lensResults.last().profile.address.city shouldBe "City1000"
        
        // 验证原始对象未被修改
        testUser.profile.address.city shouldBe "TestCity"
    }
    
    "自定义Lens应该正确工作" {
        // 创建一个自定义Lens来访问用户的全名（姓名 + ID）
        val userFullNameLens: Lens<User, String> = Lens(
            get = { user -> "${user.name}#${user.id}" },
            set = { user, fullName ->
                val parts = fullName.split("#")
                if (parts.size == 2) {
                    user.copy(
                        name = parts[0],
                        id = parts[1].toLongOrNull() ?: user.id
                    )
                } else {
                    user.copy(name = fullName)
                }
            }
        )
        
        // 测试get操作
        userFullNameLens.get(testUser) shouldBe "TestUser#1"
        
        // 测试set操作
        val updatedUser = userFullNameLens.set(testUser, "NewUser#2")
        updatedUser.name shouldBe "NewUser"
        updatedUser.id shouldBe 2L
        
        // 测试部分更新
        val partialUpdate = userFullNameLens.set(testUser, "OnlyName")
        partialUpdate.name shouldBe "OnlyName"
        partialUpdate.id shouldBe testUser.id // 保持原ID
    }
})