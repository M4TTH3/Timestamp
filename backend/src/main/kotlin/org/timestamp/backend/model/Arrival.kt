package org.timestamp.backend.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.timestamp.lib.dto.ArrivalDTO

@Entity
@Table(name = "arrivals", schema = "public")
class Arrival (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    @JsonIgnore
    var event: Event? = null,

    @Column(name = "user_id")
    var userId: String = ""
): Base()

fun Arrival.toDTO(): ArrivalDTO {
    return ArrivalDTO(
        userId = userId,
        time = createdAt!!
    )
}