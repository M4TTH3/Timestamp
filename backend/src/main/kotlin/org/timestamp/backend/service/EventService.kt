package org.timestamp.backend.service

import com.graphhopper.GraphHopper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.codec.ServerSentEvent
import org.springframework.stereotype.Service
import org.timestamp.backend.config.*
import org.timestamp.backend.model.*
import org.timestamp.backend.repository.TimestampEventLinkRepository
import org.timestamp.backend.repository.TimestampEventRepository
import org.timestamp.backend.repository.TimestampUserRepository
import org.timestamp.backend.util.SseHub
import org.timestamp.backend.util.getNotification
import org.timestamp.backend.util.updateUserEvent
import org.timestamp.shared.dto.*
import org.timestamp.shared.util.utcNow
import java.util.*


@Service
class EventService(
    private val db: TimestampEventRepository,
    private val userDb: TimestampUserRepository,
    private val eventLinkDb: TimestampEventLinkRepository,
    private val graphHopper: GraphHopper,
    private val sseHub: SseHub<EventDTO, EventStreamType>
) {

    fun registerSseEvents(firebaseUser: FirebaseUser): Flow<ServerSentEvent<EventDTO>> {
        val events = db.findAllEventsByUser(firebaseUser.uid).map {
            Pair(
                it.toDTO(),
                EventStreamType.ADD
            )
        }

        return sseHub.registerFlow(firebaseUser.uid, *events.toTypedArray())
    }

    /**
     * Get events by user ID. We only return events the user isn't part of yet.
     * We will also filter out events that are more than 30 minutes old.
     * Return an event DTO with many fields obscured
     */
    fun getEventByLinkId(firebaseUser: FirebaseUser, id: UUID): EventDTO {
        val user = userDb.findByIdOrNull(firebaseUser.uid) ?: throw UserNotFoundException()

        val threshold = utcNow().minusMinutes(30)
        val link = eventLinkDb.findByIdOrNull(id) ?: throw EventLinkNotFoundException()
        if (link.createdAt.isBefore(threshold)) throw EventLinkExpiredException()

        val event = link.event!!

        // Check if the user is already part of the event
        if (event.userEvents.firstOrNull { it.id.userId == user.id } != null) throw BadRequestException()
        return event.toHiddenDTO()
    }

    /**
     * Create an event with the given user as the creator.
     */
    fun createEvent(user: User, eventDTO: EventDTO): EventDTO {
        val user = userDb.findByIdOrNull(user.id) ?: throw EventNotFoundException()
        val event = eventDTO.toEvent()
        event.creator = user.id

        val userEvent = UserEvent(user = user, event = event)
        graphHopper.updateUserEvent(userEvent.updateTravelMode(eventDTO, user.id))
        event.userEvents.add(userEvent)
        return db.save(event).toDTO()
    }

    /**
     * Update an event with the given user. The user must be the creator of the event.
     * We will verify the event info by getting the DB version and modifying that.
     * Returns true if the event was updated, false otherwise.
     */
     fun updateEvent(firebaseUser: FirebaseUser, event: EventDTO): EventDTO {
        val item: Event = db.findByIdOrNull(event.id) ?: throw EventNotFoundException()

        if (item.creator != firebaseUser.uid) throw ForbiddenException()

        item.latitude = event.latitude
        item.longitude = event.longitude
        item.address = event.address
        item.arrival = event.arrival
        item.description = event.description
        item.name = event.name

        // Update the travel mode of the current user
        item.userEvents.first { it.id.userId == firebaseUser.uid }.updateTravelMode(event, firebaseUser.uid)

        return db.save(item).sendDTO(firebaseUser.uid, EventStreamType.UPDATE)
    }

    /**
     * Update the travel mode of the user in the event. The user must be part of the event.
     * @return the updated event
     */
    fun updateEventTravelMode(firebaseUser: FirebaseUser, eventId: Long, travelMode: TravelMode?): EventDTO {
        val item: Event = db.findByIdOrNull(eventId) ?: throw EventNotFoundException()

        val userEvent = item.userEvents.firstOrNull {
            it.id.userId == firebaseUser.uid
        } ?: throw UserNotFoundException()
        graphHopper.updateUserEvent(userEvent.updateTravelMode(travelMode))

        return db.save(item).sendDTO(firebaseUser.uid, EventStreamType.UPDATE)
    }

    /**
     * Join an event with the given user. The user must exist in the database.
     */
    fun joinEvent(firebaseUser: FirebaseUser, eventLinkId: UUID, travelMode: TravelMode?): EventDTO {
        val user = userDb.findByIdOrNull(firebaseUser.uid)?: throw UserNotFoundException()
        val link = eventLinkDb.findByIdOrNull(eventLinkId) ?: throw EventLinkNotFoundException()
        val threshold = utcNow().minusMinutes(30)

        if (link.createdAt.isBefore(threshold)) throw EventLinkExpiredException()

        val event = link.event!!
        if (user.id in event.userEvents.map { it.id.userId }) throw BadRequestException()

        val userEvent = UserEvent(user = user, event = event)
        graphHopper.updateUserEvent(userEvent.updateTravelMode(travelMode))
        event.userEvents.add(userEvent)

        return db.save(event).sendDTO(user.id, EventStreamType.ADD)
    }

    fun getEventLink(firebaseUser: FirebaseUser, id: Long): EventLinkDTO {
        val event = db.findByIdOrNull(id) ?: throw EventLinkNotFoundException()

        // Can only get the link if you are the creator
        if (event.creator != firebaseUser.uid) throw ForbiddenException()

        return eventLinkDb.save(EventLink(event = event)).toDTO()
    }

    /**
     * Delete an event. The user must exist in the database.
     * If the user is the creator of the event, the event will be deleted.
     * If the user is NOT the creator, the user will be removed from the event.
     * Returns true if the event was deleted, false otherwise.
     */
    fun deleteEvent(id: Long, firebaseUser: FirebaseUser): Boolean {
        val item: Event = db.findByIdOrNull(id) ?: return false

        if (item.creator == firebaseUser.uid) {
            db.deleteById(id)
            return true
        }

        db.delete(item)
        item.sendDTO(firebaseUser.uid, EventStreamType.DELETE)
        return true
    }

    fun kickUser(eventId: Long, deleteUID: String, firebaseUser: FirebaseUser): Boolean {
        val event = db.findByIdOrNull(eventId) ?: return false
        if (event.creator != firebaseUser.uid) return false

        val userEvent = event.userEvents.firstOrNull { it.id.userId == deleteUID } ?: return false

        event.userEvents.remove(userEvent)
        db.save(event)
        event.sendDTO(firebaseUser.uid, EventStreamType.DELETE)
        return true
    }

    /**
     * For now, we are only showing the closest event upcoming. We can change this later.
     * We will return the event and the distance to the event.
     */
    fun getNotifications(firebaseUser: FirebaseUser): NotificationDTO? {
        val event = db.findNextEventByUser(firebaseUser.uid) ?: return null
        val userEvent = event.userEvents.firstOrNull { it.id.userId == firebaseUser.uid }
        userEvent ?: throw InternalServerErrorException()

        return graphHopper.getNotification(userEvent)
    }

    /**
     * Convert an EventDTO to an Event object. Don't include any
     * join table information, as we will manually update those.
     */
    private fun EventDTO.toEvent(): Event {
        return Event(
            id = id,
            name = name,
            description = description,
            latitude = latitude,
            longitude = longitude,
            address = address,
            arrival = arrival,
            creator = creator
        )
    }

    /**
     * Update the travel method of the userEvent based on the source event.
     * This will update the travel method of the userEvent based on the source event.
     */
    private fun UserEvent.updateTravelMode(
        src: EventDTO,
        userId: String
    ): UserEvent {
        val user = src.users.firstOrNull { it.id == userId } ?: return this

        // Only update if the user is in the src event
        return this.updateTravelMode(user.travelMode)
    }

    private fun UserEvent.updateTravelMode(
        travelMode: TravelMode?
    ): UserEvent {
        this.travelMode = travelMode
        return this
    }

    private fun Event.sendDTO(sender: String, type: EventStreamType): EventDTO {
        val event = this.toDTO()
        sseHub.scope.launch {
            event.users.forEach {
                // Send it to everyone but the sender
                if (it.id != sender) sseHub.send(it.id, event, type)
            }
        }

        return event
    }
}