package ch.baunex.user

import io.quarkus.hibernate.orm.panache.PanacheEntity
import jakarta.persistence.*

@Entity
@Table(name = "users")
class UserModel() : PanacheEntity() { // <-- Add empty constructor here
    @Column(nullable = false, unique = true)
    lateinit var email: String

    @Column(nullable = false)
    lateinit var password: String

    @ManyToOne
    @JoinColumn(name = "role_id")
    lateinit var role: RoleModel

    // Additional constructor (optional)
    constructor(email: String, password: String, role: RoleModel) : this() {
        this.email = email
        this.password = password
        this.role = role
    }
}
