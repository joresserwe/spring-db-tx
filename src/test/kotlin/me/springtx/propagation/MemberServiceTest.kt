package me.springtx.propagation

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.UnexpectedRollbackException

@SpringBootTest
internal class MemberServiceTest : DescribeSpec() {
    @Autowired
    private lateinit var memberService: MemberService

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Autowired
    private lateinit var logRepository: LogRepository


    init {
        this.describe("Transaction 전파 테스트") {
            /**
             * memberService   @Transactional:OFF
             * memberRepository   @Transactional:ON
             * logRepository   @Transactional:ON
             */
            this.context("외부 Transaction OFF - 성공") {
                val username = "outerTxOff_success"
                memberService.joinV1(username)
                this.it("member, log: commit") {
                    memberRepository.find(username).shouldNotBeNull()
                    logRepository.find(username).shouldNotBeNull()
                }
            }
            /**
             * memberService   @Transactional:OFF
             * memberRepository   @Transactional:ON
             * logRepository   @Transactional:ON - Exception
             */
            this.context("외부 Transaction OFF - Log 예외") {
                val username = "로그예외_outerTxOff_fail"
                // Log 저장시 예외가 발생한다.
                shouldThrowExactly<RuntimeException> { memberService.joinV1(username) }
                this.it("member: commit") {
                    memberRepository.find(username).shouldNotBeNull()
                }
                this.it("log: rollback") {
                    logRepository.find(username).shouldBeNull()
                }
            }
            /**
             * memberService   @Transactional:ON
             * memberRepository   @Transactional:OFF
             * logRepository   @Transactional:OFF
             */
            this.context("외부 Transaction ON - 성공") {
                val username = "outerTxOn_success"
                memberService.joinV1(username)
                this.it("member, log: commit") {
                    memberRepository.find(username).shouldNotBeNull()
                    logRepository.find(username).shouldNotBeNull()
                }
            }
            /**
             * memberService   @Transactional:ON
             * memberRepository   @Transactional:ON
             * logRepository   @Transactional:ON
             */
            this.context("외부 내부 Transaction ON - 성공") {
                val username = "outerTxOn_innerTxOn_success"
                memberService.joinV1(username)
                this.it("member, log: commit") {
                    memberRepository.find(username).shouldNotBeNull()
                    logRepository.find(username).shouldNotBeNull()
                }
            }
            /**
             * memberService   @Transactional:ON
             * memberRepository   @Transactional:ON
             * logRepository   @Transactional:ON - Exception
             */
            this.context("외부 내부 Transaction ON - 실패") {
                val username = "로그예외_outerTxOn_innerTxOn_fail"
                // Log 저장시 예외가 발생한다.
                shouldThrowExactly<RuntimeException> { memberService.joinV1(username) }
                this.it("member, log: rollback") {
                    memberRepository.find(username).shouldBeNull()
                    logRepository.find(username).shouldBeNull()
                }
            }

            /**
             * memberService   @Transactional:ON
             * memberRepository   @Transactional:ON
             * logRepository   @Transactional:ON - Exception
             */
            this.context("외부 내부 Transaction ON - Log 실패 - TryCatch 복구") {
                val username = "로그예외_recoverException_fail"
                // Log 저장시 예외가 발생 -> Service에서 복구 (try-catch)
                shouldThrowExactly<UnexpectedRollbackException> { memberService.joinV2(username) }
                this.it("member, log: rollback : 복구를 해도 rollback이 된다.") {
                    memberRepository.find(username).shouldBeNull()
                    logRepository.find(username).shouldBeNull()
                }
            }
            /**
             * memberService   @Transactional:ON
             * memberRepository   @Transactional:ON
             * logRepository   @Transactional:ON(REQUIRES_NEW) - Exception
             */
            this.context("외부 내부 Transaction ON - Log 실패 - TryCatch 복구 - 성공") {
                val username = "로그예외_recoverException_success"
                // Log 저장시 예외가 발생 -> Service에서 복구 (try-catch)
                memberService.joinV2(username)
                this.it("member : commit") {
                    memberRepository.find(username).shouldNotBeNull()
                }
                this.it("log: rollback") {
                    logRepository.find(username).shouldBeNull()
                }
            }
        }
    }
}
