package ch.baunex.user

import io.quarkus.hibernate.orm.panache.PanacheEntity
import jakarta.persistence.*

@Entity
@Table(name = "roles")
class RoleModel() : PanacheEntity() {  // <-- Add empty constructor here
    @Column(nullable = false, unique = true)
    lateinit var name: String

    // Additional constructor (optional)
    constructor(name: String) : this() {
        this.name = name
    }
}