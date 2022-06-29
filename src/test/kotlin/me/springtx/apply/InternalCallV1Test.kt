package me.springtx.apply

import io.kotest.core.spec.style.FunSpec
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronizationManager

@SpringBootTest
class InternalCallV1Test : FunSpec() {
    private val log = KotlinLogging.logger {}

    init {
        this.test("printProxy") {
            log.info { "callService class=${callService::class}" }
        }
        this.test("callInternal") {
            callService.internal()
        }
        this.test("callExternal") {
            callService.external()
        }
    }

    @Autowired
    private lateinit var callService: CallService

    @TestConfiguration
    class InternalCallV1TestConfig {
        @Bean
        fun callService() = CallService()
    }

    open class CallService {

        private val log = KotlinLogging.logger {}

        open fun external() {
            log.info { "call external" }
            printTxInfo()
            internal()
        }

        @Transactional
        open fun internal() {
            log.info { "call internal" }
            printTxInfo()
        }

        private fun printTxInfo() {
            val isTxActive = TransactionSynchronizationManager.isActualTransactionActive()
            log.info { "tx active=$isTxActive" }
        }
    }
}
