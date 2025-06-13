package ch.baunex.user.model

import io.quarkus.hibernate.orm.panache.PanacheEntity
import jakarta.persistence.*

@Entity
@Table(name = "roles")
class RoleModel() : PanacheEntity() {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    lateinit var name: Role

    constructor(name: Role) : this() {
        this.name = name
    }
}
