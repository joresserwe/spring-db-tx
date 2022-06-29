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
class TxLevelTest : FunSpec() {

    private val log = KotlinLogging.logger {}

    @Autowired
    private lateinit var service: LevelService

    init {
        this.test("Transaction 적용 우선 순위 테스트") {
            service.write()
            service.read()
        }
    }

    @TestConfiguration
    class TxLevelTestConfig {
        @Bean
        fun levelService() = LevelService()
    }

    @Transactional(readOnly = true)
    class LevelService {
        private val log = KotlinLogging.logger {}

        @Transactional(readOnly = false)
        fun write() {
            log.info { "call write" }
            printTxInfo()
        }

        fun read() {
            log.info { "call read" }
            printTxInfo()
        }

        private fun printTxInfo() {
            val isTxActive = TransactionSynchronizationManager.isActualTransactionActive()
            log.info { "tx active=$isTxActive" }
            val isTxReadOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly()
            log.info { "tx readonly=$isTxReadOnly" }
        }
    }
}
