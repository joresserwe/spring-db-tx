package me.springtx.apply

import io.kotest.core.spec.style.FunSpec
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.event.EventListener
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronizationManager
import javax.annotation.PostConstruct

@SpringBootTest
class InitTxTest : FunSpec() {

    @TestConfiguration
    class InitTxTestConfig {
        @Bean
        fun hello() = Hello()
    }

    @Autowired
    private lateinit var hello: Hello

    init {
        this.test("초기화 코드는 Bean 초기화 시점에 호출된다.") {
        }
    }

    @Transactional
    class Hello {
        private val log = KotlinLogging.logger {}

        @PostConstruct
        fun initV1() {
            val isActive = TransactionSynchronizationManager.isActualTransactionActive()
            log.info { "Hello init @PostConstruct tx active=$isActive" }
        }

        @EventListener(ApplicationReadyEvent::class)
        fun initV2() {
            val isActive = TransactionSynchronizationManager.isActualTransactionActive()
            log.info { "Hello init ApplicationReadyEvent tx active=$isActive" }
        }
    }
}

