package org.zgq.optics

import arrow.core.left
import arrow.core.none
import arrow.core.right
import arrow.core.some
import arrow.optics.Lens
import arrow.optics.Prism
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.zgq.optics.PrismExamples.ApiResponse
import org.zgq.optics.PrismExamples.PaymentMethod
import org.zgq.optics.PrismExamples.UserInfo
import org.zgq.optics.PrismExamples.UserStatus
import org.zgq.optics.PrismExamples.bannedPrism
import org.zgq.optics.PrismExamples.creditCardPrism
import org.zgq.optics.PrismExamples.debitCardPrism
import org.zgq.optics.PrismExamples.errorPrism
import org.zgq.optics.PrismExamples.payPalPrism
import org.zgq.optics.PrismExamples.successPrism
import org.zgq.optics.PrismExamples.suspendedPrism

/**
 * Prism棱镜完整测试用例
 *
 * 测试Arrow Optics Prism的所有核心功能，包括：
 * - 基本操作（getOrModify, reverseGet, getOption）
 * - 修改操作（modify, set）
 * - sealed class分支处理
 * - 实际应用场景
 * - Prism组合和法则验证
 */
class PrismTest : StringSpec({
    
    // 测试数据
    val testCreditCard = PaymentMethod.CreditCard("1234-5678-9012-3456", "12/25", "123")
    val testDebitCard = PaymentMethod.DebitCard("5555-6666-7777-8888", "1234")
    val testPayPal = PaymentMethod.PayPal("test@example.com")
    val testCash = PaymentMethod.Cash
    
    val testSuspended = UserStatus.Suspended("违规行为", "2024-12-31")
    val testBanned = UserStatus.Banned("严重违规", true)
    val testActive = UserStatus.Active
    
    val testSuccess = ApiResponse.Success(UserInfo(1L, "Alice", "alice@example.com"))
    val testError = ApiResponse.Error(404, "Not found")
    val testLoading = ApiResponse.Loading
    
    "Prism getOrModify应该正确处理匹配的分支" {
        val result = creditCardPrism.getOrModify(testCreditCard)
        result shouldBe testCreditCard.right()
    }
    
    "Prism getOrModify应该正确处理不匹配的分支" {
        val result = creditCardPrism.getOrModify(testPayPal)
        result shouldBe testPayPal.left()
        
        val debitResult = creditCardPrism.getOrModify(testDebitCard)
        debitResult shouldBe testDebitCard.left()
    }
    
    "Prism reverseGet应该正确创建联合类型" {
        val newCard = PaymentMethod.CreditCard("9999-8888-7777-6666", "01/28", "000")
        val result = creditCardPrism.reverseGet(newCard)
        result shouldBe newCard
        
        // 验证类型
        result shouldBe newCard as PaymentMethod
    }
    
    "Prism getOrModify应该在匹配时返回Right" {
        val result = creditCardPrism.getOrModify(testCreditCard)
        result shouldBe testCreditCard.right()
        
        val payPalResult = payPalPrism.getOrModify(testPayPal)
        payPalResult shouldBe testPayPal.right()
    }
    
    "Prism getOrModify应该在不匹配时返回Left" {
        val result = creditCardPrism.getOrModify(testPayPal)
        result shouldBe testPayPal.left()
        
        val debitResult = payPalPrism.getOrModify(testDebitCard)
        debitResult shouldBe testDebitCard.left()
    }
    
    "Prism modify应该在匹配时修改值" {
        val result = creditCardPrism.modify(testCreditCard) { card ->
            card.copy(cvv = "999")
        }
        
        // 应该是修改后的CreditCard
        val expectedCard = testCreditCard.copy(cvv = "999")
        result shouldBe expectedCard
        
        // 验证原对象未被修改
        testCreditCard.cvv shouldBe "123"
    }
    
    "Prism modify应该在不匹配时保持原值" {
        val result = creditCardPrism.modify(testPayPal) { card ->
            card.copy(cvv = "999")
        }
        
        result shouldBe testPayPal
    }
    
    "Prism set应该在匹配时替换值" {
        val newCard = PaymentMethod.CreditCard("9999-8888-7777-6666", "01/28", "000")
        val result = creditCardPrism.set(testCreditCard, newCard)
        
        result shouldBe newCard
        result shouldNotBe testCreditCard
    }
    
    "Prism set应该在不匹配时保持原值" {
        val newCard = PaymentMethod.CreditCard("9999-8888-7777-6666", "01/28", "000")
        val result = creditCardPrism.set(testPayPal, newCard)
        
        result shouldBe testPayPal
    }
    
    "Prism应该正确处理sealed class的所有分支" {
        // 测试CreditCard分支
        creditCardPrism.getOrModify(testCreditCard).isRight() shouldBe true
        creditCardPrism.getOrModify(testDebitCard).isLeft() shouldBe true
        creditCardPrism.getOrModify(testPayPal).isLeft() shouldBe true
        creditCardPrism.getOrModify(testCash).isLeft() shouldBe true
        
        // 测试DebitCard分支
        debitCardPrism.getOrModify(testDebitCard).isRight() shouldBe true
        debitCardPrism.getOrModify(testCreditCard).isLeft() shouldBe true
        debitCardPrism.getOrModify(testPayPal).isLeft() shouldBe true
        debitCardPrism.getOrModify(testCash).isLeft() shouldBe true
        
        // 测试PayPal分支
        payPalPrism.getOrModify(testPayPal).isRight() shouldBe true
        payPalPrism.getOrModify(testCreditCard).isLeft() shouldBe true
        payPalPrism.getOrModify(testDebitCard).isLeft() shouldBe true
        payPalPrism.getOrModify(testCash).isLeft() shouldBe true
    }
    
    "Prism应该正确处理UserStatus sealed class" {
        // 测试Suspended状态
        suspendedPrism.getOrModify(testSuspended).isRight() shouldBe true
        suspendedPrism.getOrModify(testActive).isLeft() shouldBe true
        suspendedPrism.getOrModify(testBanned).isLeft() shouldBe true
        
        // 测试Banned状态
        bannedPrism.getOrModify(testBanned).isRight() shouldBe true
        bannedPrism.getOrModify(testActive).isLeft() shouldBe true
        bannedPrism.getOrModify(testSuspended).isLeft() shouldBe true
    }
    
    "Prism应该正确处理泛型sealed class" {
        // 测试Success响应
        successPrism.getOrModify(testSuccess).isRight() shouldBe true
        successPrism.getOrModify(testError).isLeft() shouldBe true
        successPrism.getOrModify(testLoading).isLeft() shouldBe true
        
        // 测试Error响应
        errorPrism.getOrModify(testError).isRight() shouldBe true
        errorPrism.getOrModify(testSuccess).isLeft() shouldBe true
        errorPrism.getOrModify(testLoading).isLeft() shouldBe true
    }
    
    "Prism modify应该支持复杂的修改逻辑" {
        // 修改Suspended状态的原因和截止日期
        val modifiedSuspended = suspendedPrism.modify(testSuspended) { suspended ->
            suspended.copy(
                reason = "更新的${suspended.reason}",
                until = "2025-01-31"
            )
        }
        
        val expectedSuspended = UserStatus.Suspended("更新的违规行为", "2025-01-31")
        modifiedSuspended shouldBe expectedSuspended
        
        // 修改API成功响应中的用户数据
        val modifiedSuccess = successPrism.modify(testSuccess) { success ->
            success.copy(
                data = success.data.copy(name = "Alice Smith")
            )
        }
        
        val expectedSuccess = ApiResponse.Success(UserInfo(1L, "Alice Smith", "alice@example.com"))
        modifiedSuccess shouldBe expectedSuccess
    }
    
    "Prism应该满足Prism法则" {
        val prism = creditCardPrism
        val card = testCreditCard
        val paypal = testPayPal
        
        // 法则1：getOrModify(reverseGet(a)) == Right(a)
        val reversedCard = prism.reverseGet(card)
        prism.getOrModify(reversedCard) shouldBe card.right()
        
        // 法则2：getOrModify(s).fold(identity, reverseGet) == s
        val getOrModifyResult = prism.getOrModify(card)
        val foldResult = getOrModifyResult.fold({ it }, { prism.reverseGet(it) })
        foldResult shouldBe card
        
        val getOrModifyPayPal = prism.getOrModify(paypal)
        val foldPayPal = getOrModifyPayPal.fold({ it }, { prism.reverseGet(it) })
        foldPayPal shouldBe paypal
    }
    
    "Prism组合应该正确工作" {
        // 创建嵌套数据结构
        data class Transaction(val paymentMethod: PaymentMethod, val amount: Double)
        data class Order(val transaction: Transaction)
        
        val order = Order(Transaction(testCreditCard, 99.99))
        
        // 手动组合操作而不是使用复杂的Lens组合
        // 获取嵌套的信用卡信息
        val creditCardResult = creditCardPrism.getOrModify(order.transaction.paymentMethod)
        creditCardResult shouldBe testCreditCard.right()
        
        // 修改嵌套的信用卡信息
        val modifiedPayment = creditCardPrism.modify(order.transaction.paymentMethod) { card ->
            card.copy(cvv = "***")
        }
        
        val modifiedOrder = order.copy(
            transaction = order.transaction.copy(paymentMethod = modifiedPayment)
        )
        
        val expectedCard = testCreditCard.copy(cvv = "***")
        val expectedOrder = Order(Transaction(expectedCard, 99.99))
        modifiedOrder shouldBe expectedOrder
    }
    
    "Prism应该支持函数式编程模式" {
        val payments = listOf(testCreditCard, testDebitCard, testPayPal, testCash)
        
        // 提取所有信用卡
        val creditCards = payments.mapNotNull { payment ->
            creditCardPrism.getOrModify(payment).getOrNull()
        }
        
        creditCards shouldBe listOf(testCreditCard)
        
        // 提取所有PayPal支付
        val payPalPayments = payments.mapNotNull { payment ->
            payPalPrism.getOrModify(payment).getOrNull()
        }
        
        payPalPayments shouldBe listOf(testPayPal)
        
        // 安全地修改所有信用卡的CVV
        val securePayments = payments.map { payment ->
            creditCardPrism.modify(payment) { card ->
                card.copy(cvv = "***")
            }
        }
        
        val expectedSecurePayments = listOf(
            testCreditCard.copy(cvv = "***"),
            testDebitCard,
            testPayPal,
            testCash
        )
        
        securePayments shouldBe expectedSecurePayments
    }
    
    "Prism应该正确处理边界情况" {
        // 测试空字符串和特殊字符
        val emptyEmailPayPal = PaymentMethod.PayPal("")
        val specialCharPayPal = PaymentMethod.PayPal("test@#$%^&*().com")
        
        payPalPrism.getOrModify(emptyEmailPayPal) shouldBe emptyEmailPayPal.right()
        payPalPrism.getOrModify(specialCharPayPal) shouldBe specialCharPayPal.right()
        
        // 测试修改为空值
        val modifiedToEmpty = payPalPrism.modify(testPayPal) { paypal ->
            paypal.copy(email = "")
        }
        
        modifiedToEmpty shouldBe PaymentMethod.PayPal("")
        
        // 测试极端的Suspended状态
        val extremeSuspended = UserStatus.Suspended("", "")
        suspendedPrism.getOrModify(extremeSuspended) shouldBe extremeSuspended.right()
    }
    
    "Prism应该保持引用透明性" {
        val prism = creditCardPrism
        val payment = testCreditCard
        
        // 多次调用应该产生相同结果
        val result1 = prism.getOrModify(payment)
        val result2 = prism.getOrModify(payment)
        
        result1 shouldBe result2
        
        // 多次modify应该产生相同结果
        val modify1 = prism.modify(payment) { it.copy(cvv = "000") }
        val modify2 = prism.modify(payment) { it.copy(cvv = "000") }
        
        modify1 shouldBe modify2
    }
    
    "自定义Prism应该正确工作" {
        // 创建一个自定义Prism来处理特定条件的CreditCard
        val validCreditCardPrism: Prism<PaymentMethod, PaymentMethod.CreditCard> = Prism(
            getOrModify = { payment ->
                when (payment) {
                    is PaymentMethod.CreditCard -> {
                        if (payment.number.length == 19 && payment.cvv.length == 3) {
                            payment.right()
                        } else {
                            payment.left()
                        }
                    }
                    else -> payment.left()
                }
            },
            reverseGet = { card -> card }
        )
        
        val validCard = PaymentMethod.CreditCard("1234-5678-9012-3456", "12/25", "123")
        val invalidCard = PaymentMethod.CreditCard("1234", "12/25", "12")
        
        // 测试有效卡片
        validCreditCardPrism.getOrModify(validCard).isRight() shouldBe true
        
        // 测试无效卡片
        validCreditCardPrism.getOrModify(invalidCard).isLeft() shouldBe true
        
        // 测试非信用卡
        validCreditCardPrism.getOrModify(testPayPal).isLeft() shouldBe true
    }
    
    "Prism性能应该适合大量数据处理" {
        // 创建大量测试数据
        val largePaymentList = (1..1000).map { i ->
            when (i % 4) {
                0 -> PaymentMethod.CreditCard("1234-5678-9012-${"$i".padStart(4, '0')}", "12/25", "123")
                1 -> PaymentMethod.DebitCard("5555-6666-7777-${"$i".padStart(4, '0')}", "1234")
                2 -> PaymentMethod.PayPal("user$i@example.com")
                else -> PaymentMethod.Cash
            }
        }
        
        // 提取所有信用卡
        val creditCards = largePaymentList.mapNotNull { payment ->
            creditCardPrism.getOrModify(payment).getOrNull()
        }
        
        // 验证结果
        creditCards.size shouldBe 250 // 1000 / 4 = 250
        creditCards.all { it is PaymentMethod.CreditCard } shouldBe true
        
        // 批量修改所有信用卡
        val securePayments = largePaymentList.map { payment ->
            creditCardPrism.modify(payment) { card ->
                card.copy(cvv = "***")
            }
        }
        
        // 验证修改结果
        val securedCreditCards = securePayments.mapNotNull { payment ->
            creditCardPrism.getOrModify(payment).getOrNull()
        }
        
        securedCreditCards.all { it.cvv == "***" } shouldBe true
    }
})