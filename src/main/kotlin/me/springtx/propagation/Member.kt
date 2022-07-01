package me.springtx.propagation

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
class Member(
    val username: String
) {
    @Id
    @GeneratedValue
    val id = 0L

}
