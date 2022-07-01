package me.springtx.propagation

import io.kotest.core.spec.style.FunSpec
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.interceptor.DefaultTransactionAttribute
import javax.sql.DataSource

@SpringBootTest
class BasicTxTest : FunSpec() {

    private val log = KotlinLogging.logger {}

    @Autowired
    private lateinit var txManager: PlatformTransactionManager

    @TestConfiguration
    class Config {
        @Bean
        fun transactionManager(dataSource: DataSource) = DataSourceTransactionManager(dataSource)
    }

    init {
        this.context("transaction") {
            this.test("commit") {
                log.info { "transaction 시작" }
                val status = txManager.getTransaction(DefaultTransactionAttribute())

                log.info { "transaction commit 시작" }
                txManager.commit(status)
                log.info { "transaction commit 완료" }

            }

            this.test("rollback") {
                log.info { "transaction 시작" }
                val status = txManager.getTransaction(DefaultTransactionAttribute())

                log.info { "transaction rollback 시작" }
                txManager.rollback(status)
                log.info { "transaction rollback 완료" }
            }
            this.test("commit 연속 2번") {
                log.info { "tx1 시작" }
                val tx1 = txManager.getTransaction(DefaultTransactionAttribute())
                log.info { "tx1 commit" }
                txManager.commit(tx1)

                log.info { "tx2 시작" }
                val tx2 = txManager.getTransaction(DefaultTransactionAttribute())
                log.info { "tx2 commit" }
                txManager.commit(tx2)
            }
            this.test("commit rollback 연속") {
                log.info { "tx1 시작" }
                val tx1 = txManager.getTransaction(DefaultTransactionAttribute())
                log.info { "tx1 commit" }
                txManager.commit(tx1)

                log.info { "tx2 시작" }
                val tx2 = txManager.getTransaction(DefaultTransactionAttribute())
                log.info { "tx2 rollback" }
                txManager.rollback(tx2)
            }
            this.test("내부 commit") {
                log.info { "외부 시작" }
                val outer = txManager.getTransaction(DefaultTransactionAttribute())
                log.info { "outer.isNewTransaction=${outer.isNewTransaction}" }

                log.info { "내부 시작" }
                val inner = txManager.getTransaction(DefaultTransactionAttribute())
                log.info { "inner.isNewTransaction=${inner.isNewTransaction}" }
                log.info { "내부 commit" }
                txManager.commit(inner) // 아무동작 안함

                log.info { "외부 commit" }
                txManager.commit(outer)
            }

            this.test("외부 rollback") {
                log.info { "외부 시작" }
                val outer = txManager.getTransaction(DefaultTransactionAttribute())

                log.info { "내부 시작" }
                val inner = txManager.getTransaction(DefaultTransactionAttribute())
                log.info { "내부 commit" }
                txManager.commit(inner) // 아무동작 안함

                log.info { "외부 rollback" }
                txManager.rollback(outer)
            }

            this.test("내부 rollback") {
                log.info { "외부 시작" }
                val outer = txManager.getTransaction(DefaultTransactionAttribute())

                log.info { "내부 시작" }
                val inner = txManager.getTransaction(DefaultTransactionAttribute())
                log.info { "내부 rollback" }
                log.info { "outer.isRollbackOnly=${outer.isRollbackOnly}" }
                txManager.rollback(inner) // rollback-only 표시
                log.info { "내부 rollback 완료" }
                log.info { "outer.isRollbackOnly=${outer.isRollbackOnly}" }

                log.info { "외부 commit" }
                txManager.commit(outer)
            }

            this.test("내부 rollback, requires new option 사용") {
                log.info { "외부 시작" }
                val outer = txManager.getTransaction(DefaultTransactionAttribute())
                log.info { "outer.isNewTransaction()=${outer.isNewTransaction}" }

                log.info { "내부 시작" }
                val definition = DefaultTransactionAttribute()
                definition.propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRES_NEW
                val inner = txManager.getTransaction(definition)
                log.info { "inner.isNewTransaction()=${inner.isNewTransaction}" }

                log.info { "내부 rollback" }
                //txManager.rollback(inner) // rollback-only 표시

                log.info { "외부 commit" }
                txManager.commit(outer)
            }
        }
    }
}
