package me.springtx.order

import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrderService(
    private val orderRepository: OrderRepository
) {
    private val log = KotlinLogging.logger {}

    @Transactional
    fun order(order: Order) {
        log.info { "order 호출" }
        orderRepository.save(order)

        log.info { "결재 프로세스 진입" }
        if (order.username == "예외") {
            log.info { "시스템 예외 발생" }
            throw RuntimeException()
        } else if (order.username == "잔고부족") {
            log.info { "잔고 부족 비즈니스 예외 발생" }
            order.changePayStatus("대기")
            // Check exception
            throw NotEnoughMoneyException("잔고가 부족해요")
        } else {
            // 정상 승인
            log.info("정상 승인")
            order.changePayStatus("완료")
        }
        log.info { "결제 프로세스 완료" }
    }
}
