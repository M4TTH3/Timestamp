package org.timestamp.backend.model

import com.fasterxml.jackson.annotation.JsonBackReference
import jakarta.persistence.*
import org.timestamp.lib.dto.EventUserDTO
import org.timestamp.lib.dto.TravelMode
import java.io.Serializable
import java.time.OffsetDateTime


@Entity
@Table(name = "user_events")
class UserEvent(
    @EmbeddedId
    val id: UserEventKey = UserEventKey(),

    @Column(name = "time_est")
    var timeEst: Long? = null,

    @Column(name = "distance")
    var distance: Double? = null,

    var arrived: Boolean = false,

    @Column(name = "arrived_time")
    var arrivedTime: OffsetDateTime? = null,

    /**
     * Travel mode in a UserEvent is used for smart notifications AND overrides
     * the users own travel mode for this event.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "travel_mode")
    var travelMode: TravelMode? = null,

    @MapsId("eventId")
    @ManyToOne
    @JoinColumn(name = "event_id", referencedColumnName = "id", nullable = false)
    @JsonBackReference("event-userEvents")
    var event: Event? = null,

    @MapsId("userId")
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    @JsonBackReference("user-userEvents")
    var user: User? = null,
): Base()

/**
 * Used to create a composite key for the UserEvent table.
 * This is a join table, and takes both keys as primary key
 */
@Embeddable
data class UserEventKey(
    @Column(name = "event_id", nullable = false)
    val eventId: Long = 0L,

    @Column(name = "user_id", nullable = false)
    val userId: String = "",
): Serializable

fun UserEvent.toDTO(): EventUserDTO {
    val user = this.user!!

    return EventUserDTO(
        id = user.id,
        name = user.name,
        email = user.email,
        pfp = user.pfp,
        timeEst = this.timeEst,
        distance = this.distance,
        arrivedTime = this.arrivedTime,
        arrived = this.arrived,
        travelMode = this.travelMode
    )
}