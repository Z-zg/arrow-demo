package org.zgq.optics

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import arrow.optics.Prism

/**
 * Prism棱镜示例
 * 
 * Prism是Arrow Optics中用于聚焦联合类型（sealed class、Either等）特定分支的工具。
 * 它提供了一种类型安全的方式来访问和更新联合类型的特定变体，支持可能失败的访问操作。
 */
object PrismExamples {
    
    // 示例sealed class：支付方式
    sealed class PaymentMethod {
        data class CreditCard(val number: String, val expiryDate: String, val cvv: String) : PaymentMethod()
        data class DebitCard(val number: String, val pin: String) : PaymentMethod()
        data class PayPal(val email: String) : PaymentMethod()
        data class BankTransfer(val accountNumber: String, val routingNumber: String) : PaymentMethod()
        object Cash : PaymentMethod()
    }
    
    // 示例sealed class：用户状态
    sealed class UserStatus {
        object Active : UserStatus()
        object Inactive : UserStatus()
        data class Suspended(val reason: String, val until: String) : UserStatus()
        data class Banned(val reason: String, val permanent: Boolean) : UserStatus()
    }
    
    // 示例sealed class：API响应
    sealed class ApiResponse<out T> {
        data class Success<T>(val data: T) : ApiResponse<T>()
        data class Error(val code: Int, val message: String) : ApiResponse<Nothing>()
        object Loading : ApiResponse<Nothing>()
        object NotFound : ApiResponse<Nothing>()
    }
    
    // 示例数据类：用户信息
    data class UserInfo(val id: Long, val name: String, val email: String)
    
    // 定义Prism：聚焦CreditCard支付方式
    val creditCardPrism: Prism<PaymentMethod, PaymentMethod.CreditCard> = Prism(
        getOrModify = { paymentMethod ->
            when (paymentMethod) {
                is PaymentMethod.CreditCard -> paymentMethod.right()
                else -> paymentMethod.left()
            }
        },
        reverseGet = { creditCard -> creditCard }
    )
    
    // 定义Prism：聚焦DebitCard支付方式
    val debitCardPrism: Prism<PaymentMethod, PaymentMethod.DebitCard> = Prism(
        getOrModify = { paymentMethod ->
            when (paymentMethod) {
                is PaymentMethod.DebitCard -> paymentMethod.right()
                else -> paymentMethod.left()
            }
        },
        reverseGet = { debitCard -> debitCard }
    )
    
    // 定义Prism：聚焦PayPal支付方式
    val payPalPrism: Prism<PaymentMethod, PaymentMethod.PayPal> = Prism(
        getOrModify = { paymentMethod ->
            when (paymentMethod) {
                is PaymentMethod.PayPal -> paymentMethod.right()
                else -> paymentMethod.left()
            }
        },
        reverseGet = { payPal -> payPal }
    )
    
    // 定义Prism：聚焦Suspended用户状态
    val suspendedPrism: Prism<UserStatus, UserStatus.Suspended> = Prism(
        getOrModify = { status ->
            when (status) {
                is UserStatus.Suspended -> status.right()
                else -> status.left()
            }
        },
        reverseGet = { suspended -> suspended }
    )
    
    // 定义Prism：聚焦Banned用户状态
    val bannedPrism: Prism<UserStatus, UserStatus.Banned> = Prism(
        getOrModify = { status ->
            when (status) {
                is UserStatus.Banned -> status.right()
                else -> status.left()
            }
        },
        reverseGet = { banned -> banned }
    )
    
    // 定义Prism：聚焦Success API响应
    val successPrism: Prism<ApiResponse<UserInfo>, ApiResponse.Success<UserInfo>> = Prism(
        getOrModify = { response ->
            when (response) {
                is ApiResponse.Success -> response.right()
                else -> response.left()
            }
        },
        reverseGet = { success -> success }
    )
    
    // 定义Prism：聚焦Error API响应
    val errorPrism: Prism<ApiResponse<UserInfo>, ApiResponse.Error> = Prism(
        getOrModify = { response ->
            when (response) {
                is ApiResponse.Error -> response.right()
                else -> response.left()
            }
        },
        reverseGet = { error -> error }
    )
    
    /**
     * 基本Prism操作示例
     */
    fun basicPrismOperations() {
        println("=== 基本Prism操作 ===")
        
        val creditCard = PaymentMethod.CreditCard("1234-5678-9012-3456", "12/25", "123")
        val payPal = PaymentMethod.PayPal("user@example.com")
        val cash = PaymentMethod.Cash
        
        // getOrModify操作：尝试获取特定分支
        val creditCardResult = creditCardPrism.getOrModify(creditCard)
        val creditCardFromPayPal = creditCardPrism.getOrModify(payPal)
        val creditCardFromCash = creditCardPrism.getOrModify(cash)
        
        println("从CreditCard获取CreditCard: $creditCardResult")
        println("从PayPal获取CreditCard: $creditCardFromPayPal")
        println("从Cash获取CreditCard: $creditCardFromCash")
        
        // reverseGet操作：从特定类型创建联合类型
        val newCreditCard = PaymentMethod.CreditCard("9876-5432-1098-7654", "06/26", "456")
        val paymentMethod = creditCardPrism.reverseGet(newCreditCard)
        println("通过reverseGet创建PaymentMethod: $paymentMethod")
    }
    
    /**
     * Prism的getOption操作示例
     */
    fun prismGetOptionOperations() {
        println("\n=== Prism getOption操作 ===")
        
        val creditCard = PaymentMethod.CreditCard("1111-2222-3333-4444", "03/27", "789")
        val debitCard = PaymentMethod.DebitCard("5555-6666-7777-8888", "1234")
        val payPal = PaymentMethod.PayPal("test@example.com")
        
        // getOrModify：安全地尝试获取特定分支，返回Either
        val creditCardOption = creditCardPrism.getOrModify(creditCard)
        val creditCardFromDebit = creditCardPrism.getOrModify(debitCard)
        val debitCardOption = debitCardPrism.getOrModify(debitCard)
        val payPalOption = payPalPrism.getOrModify(payPal)
        
        println("从CreditCard获取CreditCard Either: $creditCardOption")
        println("从DebitCard获取CreditCard Either: $creditCardFromDebit")
        println("从DebitCard获取DebitCard Either: $debitCardOption")
        println("从PayPal获取PayPal Either: $payPalOption")
    }
    
    /**
     * Prism的modify操作示例
     */
    fun prismModifyOperations() {
        println("\n=== Prism modify操作 ===")
        
        val creditCard = PaymentMethod.CreditCard("1234-5678-9012-3456", "12/25", "123")
        val payPal = PaymentMethod.PayPal("old@example.com")
        val suspended = UserStatus.Suspended("违规行为", "2024-12-31")
        
        // modify：如果匹配则修改，否则保持原样
        val modifiedCreditCard = creditCardPrism.modify(creditCard) { card ->
            card.copy(cvv = "999")
        }
        
        val attemptModifyPayPal = creditCardPrism.modify(payPal) { card ->
            card.copy(cvv = "999")
        }
        
        val modifiedSuspended = suspendedPrism.modify(suspended) { s ->
            s.copy(until = "2025-01-31")
        }
        
        println("修改CreditCard的CVV: $modifiedCreditCard")
        println("尝试修改PayPal为CreditCard: $attemptModifyPayPal")
        println("修改Suspended的截止日期: $modifiedSuspended")
    }
    
    /**
     * Prism的set操作示例
     */
    fun prismSetOperations() {
        println("\n=== Prism set操作 ===")
        
        val creditCard = PaymentMethod.CreditCard("1234-5678-9012-3456", "12/25", "123")
        val payPal = PaymentMethod.PayPal("user@example.com")
        
        val newCreditCard = PaymentMethod.CreditCard("9999-8888-7777-6666", "01/28", "000")
        
        // set：如果匹配则替换，否则保持原样
        val setCreditCard = creditCardPrism.set(creditCard, newCreditCard)
        val setPayPalAsCreditCard = creditCardPrism.set(payPal, newCreditCard)
        
        println("替换CreditCard: $setCreditCard")
        println("尝试将PayPal替换为CreditCard: $setPayPalAsCreditCard")
    }
    
    /**
     * 处理API响应的实际应用场景
     */
    fun apiResponseHandling() {
        println("\n=== API响应处理场景 ===")
        
        val successResponse: ApiResponse<UserInfo> = ApiResponse.Success(
            UserInfo(1L, "Alice", "alice@example.com")
        )
        val errorResponse: ApiResponse<UserInfo> = ApiResponse.Error(404, "User not found")
        val loadingResponse: ApiResponse<UserInfo> = ApiResponse.Loading
        
        // 处理成功响应
        val userData = successPrism.getOrModify(successResponse)
        val errorData = errorPrism.getOrModify(errorResponse)
        val loadingData = successPrism.getOrModify(loadingResponse)
        
        println("从Success响应获取数据: $userData")
        println("从Error响应获取错误: $errorData")
        println("从Loading响应获取数据: $loadingData")
        
        // 修改成功响应中的用户数据
        val modifiedSuccess = successPrism.modify(successResponse) { success ->
            success.copy(data = success.data.copy(name = "Alice Smith"))
        }
        
        println("修改成功响应中的用户名: $modifiedSuccess")
    }
    
    /**
     * 用户状态管理场景
     */
    fun userStatusManagement() {
        println("\n=== 用户状态管理场景 ===")
        
        val activeUser = UserStatus.Active
        val suspendedUser = UserStatus.Suspended("垃圾邮件", "2024-12-31")
        val bannedUser = UserStatus.Banned("严重违规", true)
        
        // 检查用户是否被暂停
        val suspendedInfo = suspendedPrism.getOrModify(suspendedUser)
        val activeSuspendedInfo = suspendedPrism.getOrModify(activeUser)
        
        println("暂停用户的暂停信息: $suspendedInfo")
        println("活跃用户的暂停信息: $activeSuspendedInfo")
        
        // 修改暂停原因
        val modifiedSuspension = suspendedPrism.modify(suspendedUser) { suspended ->
            suspended.copy(reason = "更新的暂停原因")
        }
        
        println("修改暂停原因: $modifiedSuspension")
        
        // 尝试修改活跃用户的暂停信息（应该不会改变）
        val attemptModifyActive = suspendedPrism.modify(activeUser) { suspended ->
            suspended.copy(reason = "这不会生效")
        }
        
        println("尝试修改活跃用户: $attemptModifyActive")
    }
    
    /**
     * 支付方式验证和处理
     */
    fun paymentMethodProcessing() {
        println("\n=== 支付方式处理场景 ===")
        
        val payments = listOf(
            PaymentMethod.CreditCard("1234-5678-9012-3456", "12/25", "123"),
            PaymentMethod.DebitCard("5555-6666-7777-8888", "1234"),
            PaymentMethod.PayPal("user@paypal.com"),
            PaymentMethod.BankTransfer("123456789", "987654321"),
            PaymentMethod.Cash
        )
        
        // 提取所有信用卡支付
        val creditCards = payments.mapNotNull { payment ->
            creditCardPrism.getOrModify(payment).getOrNull()
        }
        
        println("所有信用卡支付: $creditCards")
        
        // 提取所有PayPal支付
        val payPalPayments = payments.mapNotNull { payment ->
            payPalPrism.getOrModify(payment).getOrNull()
        }
        
        println("所有PayPal支付: $payPalPayments")
        
        // 更新所有信用卡的CVV（安全处理）
        val securePayments = payments.map { payment ->
            creditCardPrism.modify(payment) { card ->
                card.copy(cvv = "***")
            }
        }
        
        println("安全处理后的支付方式: $securePayments")
    }
    
    /**
     * Prism组合操作
     */
    fun prismComposition() {
        println("\n=== Prism组合操作 ===")
        
        // 创建嵌套的数据结构
        data class Transaction(val id: String, val paymentMethod: PaymentMethod, val amount: Double)
        data class Order(val orderId: String, val transaction: Transaction)
        
        val order = Order(
            "ORDER-001",
            Transaction(
                "TXN-001",
                PaymentMethod.CreditCard("1111-2222-3333-4444", "12/26", "456"),
                99.99
            )
        )
        
        // 定义Lens和Prism的组合
        val orderTransactionLens = arrow.optics.Lens(
            get = { order: Order -> order.transaction },
            set = { order: Order, transaction: Transaction -> order.copy(transaction = transaction) }
        )
        
        val transactionPaymentLens = arrow.optics.Lens(
            get = { transaction: Transaction -> transaction.paymentMethod },
            set = { transaction: Transaction, payment: PaymentMethod -> transaction.copy(paymentMethod = payment) }
        )
        
        // 组合Lens和Prism来访问订单中的信用卡信息
        val orderCreditCardPrism = orderTransactionLens compose transactionPaymentLens compose creditCardPrism
        
        // 获取订单中的信用卡信息
        val creditCardInfo = orderCreditCardPrism.getOrModify(order)
        println("订单中的信用卡信息: $creditCardInfo")
        
        // 修改订单中的信用卡CVV
        val secureOrder = orderCreditCardPrism.modify(order) { card ->
            card.copy(cvv = "***")
        }
        
        println("安全处理后的订单: $secureOrder")
    }
    
    /**
     * 演示所有示例
     */
    fun runAllExamples() {
        basicPrismOperations()
        prismGetOptionOperations()
        prismModifyOperations()
        prismSetOperations()
        apiResponseHandling()
        userStatusManagement()
        paymentMethodProcessing()
        prismComposition()
    }
}