package me.springtx.propagation

import mu.KotlinLogging
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager

@Repository
class MemberRepository(private val em: EntityManager) {

    private val log = KotlinLogging.logger {}

    @Transactional
    fun save(member: Member) {
        log.info { "member 저장" }
        em.persist(member)
    }

    fun find(username: String): Member? {
        return em.createQuery("select m from Member m where m.username= :username", Member::class.java)
            .setParameter("username", username)
            .resultList.firstOrNull()
    }
}
