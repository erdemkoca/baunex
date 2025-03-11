package ch.baunex.user.model

import io.quarkus.hibernate.orm.panache.PanacheEntity
import jakarta.persistence.*

@Entity
@Table(name = "users")
class UserModel() : PanacheEntity() {

    @Column(nullable = false, unique = true)
    lateinit var email: String

    @Column(nullable = false)
    lateinit var password: String

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    lateinit var role: Role

    constructor(email: String, password: String, role: Role) : this() {
        this.email = email
        this.password = password
        this.role = role
    }
}
