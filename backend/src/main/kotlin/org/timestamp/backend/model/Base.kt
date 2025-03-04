package org.timestamp.backend.model

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import java.time.OffsetDateTime

@MappedSuperclass
abstract class Base(
    @Column(name = "created_at")
    var createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: OffsetDateTime = OffsetDateTime.now(),
) {
    @PrePersist
    fun onPrePersist() {
        createdAt = OffsetDateTime.now()
        updatedAt = OffsetDateTime.now()
    }

    @PreUpdate
    fun onPreUpdate() {
        updatedAt = OffsetDateTime.now()
    }
}
