package me.springtx.order

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "orders")
class Order(
    val username: String, // 정상, 예외, 잔고부족
) {
    @Id
    @GeneratedValue
    val id = 0L

    lateinit var payStatus: String // 대기, 완료
        private set

    fun changePayStatus(payStatus: String) {
        this.payStatus = payStatus
    }
}
