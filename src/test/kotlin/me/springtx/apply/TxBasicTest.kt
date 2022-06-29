package me.springtx.apply

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import mu.KotlinLogging
import org.springframework.aop.support.AopUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronizationManager

@SpringBootTest
internal class TxBasicTest : FunSpec() {

    private val log = KotlinLogging.logger {}

    @Autowired
    private lateinit var basicService: BasicService

    init {
        this.test("BasicService는 Proxy가 적용됐다.") {
            log.info { "aop class=${basicService::class}" }
            AopUtils.isAopProxy(basicService) shouldBe true
        }

        this.test("Tx Test") {
            basicService.tx()
            basicService.nonTx()
        }
    }

    @TestConfiguration
    class TxApplyBasicConfig {
        @Bean
        fun basicService() = BasicService()
    }

    open class BasicService {
        private val log = KotlinLogging.logger {}

        @Transactional
        open fun tx() {
            log.info { "call tx" }
            val txActive = TransactionSynchronizationManager.isActualTransactionActive()
            log.info { "tx active=$txActive" }
        }

        open fun nonTx() {
            log.info { "call nonTx" }
            val txActive = TransactionSynchronizationManager.isActualTransactionActive()
            log.info { "tx active=$txActive" }
        }
    }
}
