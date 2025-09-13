package org.zgq.optics

import arrow.optics.Lens
import arrow.optics.copy

/**
 * Lens透镜示例
 * 
 * Lens是Arrow Optics中用于聚焦和操作不可变数据结构中特定字段的工具。
 * 它提供了一种函数式的方式来访问和更新嵌套数据结构，而不需要破坏不可变性。
 */
object LensExamples {
    
    // 示例数据类：用户信息
    data class User(
        val id: Long,
        val name: String,
        val email: String,
        val profile: Profile
    )
    
    // 示例数据类：用户档案
    data class Profile(
        val age: Int,
        val address: Address,
        val preferences: Preferences
    )
    
    // 示例数据类：地址
    data class Address(
        val street: String,
        val city: String,
        val country: String,
        val zipCode: String
    )
    
    // 示例数据类：偏好设置
    data class Preferences(
        val theme: String,
        val language: String,
        val notifications: Boolean
    )
    
    // 定义Lens：聚焦User的name字段
    val userNameLens: Lens<User, String> = Lens(
        get = { user -> user.name },
        set = { user, newName -> user.copy(name = newName) }
    )
    
    // 定义Lens：聚焦User的email字段
    val userEmailLens: Lens<User, String> = Lens(
        get = { user -> user.email },
        set = { user, newEmail -> user.copy(email = newEmail) }
    )
    
    // 定义Lens：聚焦User的profile字段
    val userProfileLens: Lens<User, Profile> = Lens(
        get = { user -> user.profile },
        set = { user, newProfile -> user.copy(profile = newProfile) }
    )
    
    // 定义Lens：聚焦Profile的age字段
    val profileAgeLens: Lens<Profile, Int> = Lens(
        get = { profile -> profile.age },
        set = { profile, newAge -> profile.copy(age = newAge) }
    )
    
    // 定义Lens：聚焦Profile的address字段
    val profileAddressLens: Lens<Profile, Address> = Lens(
        get = { profile -> profile.address },
        set = { profile, newAddress -> profile.copy(address = newAddress) }
    )
    
    // 定义Lens：聚焦Address的city字段
    val addressCityLens: Lens<Address, String> = Lens(
        get = { address -> address.city },
        set = { address, newCity -> address.copy(city = newCity) }
    )
    
    // 定义Lens：聚焦Address的country字段
    val addressCountryLens: Lens<Address, String> = Lens(
        get = { address -> address.country },
        set = { address, newCountry -> address.copy(country = newCountry) }
    )
    
    // 定义Lens：聚焦Preferences的theme字段
    val preferencesThemeLens: Lens<Preferences, String> = Lens(
        get = { preferences -> preferences.theme },
        set = { preferences, newTheme -> preferences.copy(theme = newTheme) }
    )
    
    /**
     * 基本Lens操作示例
     */
    fun basicLensOperations() {
        val user = User(
            id = 1L,
            name = "Alice",
            email = "alice@example.com",
            profile = Profile(
                age = 30,
                address = Address("123 Main St", "New York", "USA", "10001"),
                preferences = Preferences("dark", "en", true)
            )
        )
        
        println("=== 基本Lens操作 ===")
        
        // get操作：获取字段值
        val currentName = userNameLens.get(user)
        val currentEmail = userEmailLens.get(user)
        println("当前姓名: $currentName")
        println("当前邮箱: $currentEmail")
        
        // set操作：设置新值
        val userWithNewName = userNameLens.set(user, "Bob")
        val userWithNewEmail = userEmailLens.set(user, "bob@example.com")
        
        println("更新姓名后: ${userWithNewName.name}")
        println("更新邮箱后: ${userWithNewEmail.email}")
        
        // modify操作：基于当前值进行修改
        val userWithModifiedName = userNameLens.modify(user) { currentName ->
            currentName.uppercase()
        }
        println("修改姓名为大写: ${userWithModifiedName.name}")
    }
    
    /**
     * Lens组合操作示例
     */
    fun lensComposition() {
        val user = User(
            id = 1L,
            name = "Charlie",
            email = "charlie@example.com",
            profile = Profile(
                age = 25,
                address = Address("456 Oak Ave", "Los Angeles", "USA", "90210"),
                preferences = Preferences("light", "zh", false)
            )
        )
        
        println("\n=== Lens组合操作 ===")
        
        // 组合Lens：User -> Profile -> Age
        val userAgeLens = userProfileLens compose profileAgeLens
        
        // 获取嵌套字段值
        val currentAge = userAgeLens.get(user)
        println("当前年龄: $currentAge")
        
        // 设置嵌套字段值
        val userWithNewAge = userAgeLens.set(user, 26)
        println("更新年龄后: ${userWithNewAge.profile.age}")
        
        // 组合更深层的Lens：User -> Profile -> Address -> City
        val userCityLens = userProfileLens compose profileAddressLens compose addressCityLens
        
        val currentCity = userCityLens.get(user)
        println("当前城市: $currentCity")
        
        val userWithNewCity = userCityLens.set(user, "San Francisco")
        println("更新城市后: ${userWithNewCity.profile.address.city}")
    }
    
    /**
     * 多个Lens操作的链式调用
     */
    fun chainedLensOperations() {
        val user = User(
            id = 1L,
            name = "David",
            email = "david@example.com",
            profile = Profile(
                age = 35,
                address = Address("789 Pine St", "Seattle", "USA", "98101"),
                preferences = Preferences("auto", "es", true)
            )
        )
        
        println("\n=== 链式Lens操作 ===")
        
        // 使用多个Lens操作进行字段更新
        val updatedUser = user
            .let { userNameLens.set(it, "David Smith") }
            .let { userEmailLens.set(it, "david.smith@example.com") }
            .let { (userProfileLens compose profileAgeLens).set(it, 36) }
        
        println("原始用户: $user")
        println("更新后用户: $updatedUser")
    }
    
    /**
     * Lens的实际应用场景：表单数据更新
     */
    fun formDataUpdate() {
        val user = User(
            id = 1L,
            name = "Eve",
            email = "eve@example.com",
            profile = Profile(
                age = 28,
                address = Address("321 Elm St", "Boston", "USA", "02101"),
                preferences = Preferences("dark", "fr", false)
            )
        )
        
        println("\n=== 表单数据更新场景 ===")
        
        // 模拟表单更新：用户修改了个人信息
        val formUpdates = mapOf(
            "name" to "Eve Johnson",
            "email" to "eve.johnson@example.com",
            "age" to "29",
            "city" to "Cambridge"
        )
        
        // 使用Lens安全地更新数据
        var updatedUser = user
        
        formUpdates["name"]?.let { newName ->
            updatedUser = userNameLens.set(updatedUser, newName)
        }
        
        formUpdates["email"]?.let { newEmail ->
            updatedUser = userEmailLens.set(updatedUser, newEmail)
        }
        
        formUpdates["age"]?.toIntOrNull()?.let { newAge ->
            updatedUser = (userProfileLens compose profileAgeLens).set(updatedUser, newAge)
        }
        
        formUpdates["city"]?.let { newCity ->
            updatedUser = (userProfileLens compose profileAddressLens compose addressCityLens)
                .set(updatedUser, newCity)
        }
        
        println("表单更新前: $user")
        println("表单更新后: $updatedUser")
    }
    
    /**
     * Lens的性能优势：避免深度复制
     */
    fun performanceComparison() {
        val user = User(
            id = 1L,
            name = "Frank",
            email = "frank@example.com",
            profile = Profile(
                age = 40,
                address = Address("654 Maple Dr", "Denver", "USA", "80201"),
                preferences = Preferences("light", "de", true)
            )
        )
        
        println("\n=== 性能比较：传统方式 vs Lens ===")
        
        // 传统方式：需要手动处理嵌套复制
        val traditionalUpdate = user.copy(
            profile = user.profile.copy(
                address = user.profile.address.copy(
                    city = "Boulder"
                )
            )
        )
        
        // Lens方式：简洁且类型安全
        val lensUpdate = (userProfileLens compose profileAddressLens compose addressCityLens)
            .set(user, "Boulder")
        
        println("传统方式结果: ${traditionalUpdate.profile.address.city}")
        println("Lens方式结果: ${lensUpdate.profile.address.city}")
        println("结果相等: ${traditionalUpdate == lensUpdate}")
    }
    
    /**
     * 自定义Lens修改函数
     */
    fun customModifyFunctions() {
        val user = User(
            id = 1L,
            name = "grace",
            email = "GRACE@EXAMPLE.COM",
            profile = Profile(
                age = 22,
                address = Address("987 Cedar Ln", "miami", "usa", "33101"),
                preferences = Preferences("dark", "en", true)
            )
        )
        
        println("\n=== 自定义修改函数 ===")
        
        // 自定义修改函数：标准化姓名格式
        fun standardizeName(name: String): String {
            return name.lowercase().split(" ").joinToString(" ") { word ->
                word.replaceFirstChar { it.uppercase() }
            }
        }
        
        // 自定义修改函数：标准化邮箱格式
        fun standardizeEmail(email: String): String {
            return email.lowercase()
        }
        
        // 自定义修改函数：标准化城市名称
        fun standardizeCity(city: String): String {
            return city.lowercase().split(" ").joinToString(" ") { word ->
                word.replaceFirstChar { it.uppercase() }
            }
        }
        
        // 应用自定义修改函数
        val standardizedUser = user
            .let { userNameLens.modify(it, ::standardizeName) }
            .let { userEmailLens.modify(it, ::standardizeEmail) }
            .let { (userProfileLens compose profileAddressLens compose addressCityLens).modify(it, ::standardizeCity) }
        
        println("原始用户: $user")
        println("标准化后: $standardizedUser")
    }
    
    /**
     * 演示所有示例
     */
    fun runAllExamples() {
        basicLensOperations()
        lensComposition()
        chainedLensOperations()
        formDataUpdate()
        performanceComparison()
        customModifyFunctions()
    }
}