package org.timestamp.backend.model

import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("users")
data class User(
    @Id
    val id: String,
    val name: String,
    val email: String,

    @ManyToMany(mappedBy = "users")
    val events: MutableSet<Event> = mutableSetOf()
)
