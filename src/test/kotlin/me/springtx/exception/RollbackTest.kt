package me.springtx.exception

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
class RollbackTest : FunSpec() {

    @Autowired
    private lateinit var service: RollbackService

    @TestConfiguration
    class RollbackTestConfig {
        @Bean
        fun rollbackService() = RollbackService()
    }

    init {
        context("Checked/Runtime Exception에 따른 Rollback 확인") {
            this.test("runtim xception") {
                shouldThrow<RuntimeException> { service.runtimeException() }
            }
            this.test("checked Exception") {
                shouldThrow<MyException> { service.checkedException() }
            }
            this.test("rollback for checked Exception") {
                shouldThrow<MyException> { service.rollbackFor() }
            }
        }
    }

    @Transactional
    class RollbackService {

        private val log = KotlinLogging.logger {}

        fun runtimeException() {
            log.info { "call runtimeException" }
            throw RuntimeException()
        }

        fun checkedException() {
            log.info { "call checkedException" }
            throw MyException()
        }

        @Transactional(rollbackFor = [MyException::class])
        fun rollbackFor() {
            log.info { "call checkedException" }
            throw MyException()
        }
    }

    class MyException : Exception()
}
