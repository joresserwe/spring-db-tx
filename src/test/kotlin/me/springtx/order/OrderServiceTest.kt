package me.springtx.order

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull

@SpringBootTest
internal class OrderServiceTest : DescribeSpec() {

    @Autowired
    private lateinit var orderService: OrderService

    @Autowired
    private lateinit var orderRepository: OrderRepository

    init {
        this.describe("결재 예외 테스트") {
            this.context("결재가 정상적으로 된다면 된다면") {
                val order = Order("정상")
                shouldNotThrowAny { orderService.order(order) }
                val findOrder = orderRepository.findByIdOrNull(order.id)
                this.it("결재 상태가 완료가 돼야 한다 (commit)") {
                    findOrder?.payStatus shouldBe "완료"
                }
            }
            this.context("결재 중 예외가 발생한다면") {
                val order = Order("예외")
                shouldThrow<RuntimeException> { orderService.order(order) } // Runtime 예외
                val findOrder = orderRepository.findByIdOrNull(order.id)
                this.it("order가 없어야 한다 (rollback)") {
                    findOrder.shouldBeNull()
                }
            }
            this.context("결재 중 비지니스 예외(잔고부족)가 발생한다면") {
                val order = Order("잔고부족")
                shouldThrow<NotEnoughMoneyException> { orderService.order(order) } // Checked 예외
                val findOrder = orderRepository.findByIdOrNull(order.id)
                this.it("Order는 Commit 된다.") {
                    findOrder.shouldNotBeNull()
                }
                this.it("결재 상태가 대기가 돼야 한다.") {
                    findOrder?.payStatus shouldBe "대기"
                }
            }
        }
    }
}
