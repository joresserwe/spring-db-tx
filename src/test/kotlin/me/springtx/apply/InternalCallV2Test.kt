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
class InternalCallV2Test : FunSpec() {
    private val log = KotlinLogging.logger {}

    init {
        this.test("printProxy") {
            log.info { "callService class=${callService::class}" }
        }

        this.test("callExternalV2") {
            callService.external()
        }
    }

    @Autowired
    private lateinit var callService: CallService

    @TestConfiguration
    class InternalCallV1TestConfig {
        @Bean
        fun callService() = CallService(internalService())

        @Bean
        fun internalService() = InternalService()
    }

    class CallService(
        private val internalService: InternalService
    ) {

        private val log = KotlinLogging.logger {}

        fun external() {
            log.info { "call external" }
            printTxInfo()
            internalService.internal()
        }

        private fun printTxInfo() {
            val isTxActive = TransactionSynchronizationManager.isActualTransactionActive()
            log.info { "tx active=$isTxActive" }
        }
    }

    open class InternalService {

        private val log = KotlinLogging.logger {}

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
