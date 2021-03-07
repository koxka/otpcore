package hu.otp.ticketing.core.client;

import hu.otp.ticketing.ticket.endpoint.rest.EventsApi;
import hu.otp.ticketing.ticket.endpoint.rest.model.DetailedEventDetailsResponse;
import hu.otp.ticketing.ticket.endpoint.rest.model.ReservationRequest;
import hu.otp.ticketing.ticket.endpoint.rest.model.ReservationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Component
public class TicketClient {
    private final EventsApi eventsApi;

    /**
     * Sends the reservation request with all info to TICKET module.
     *
     * @param userId user id
     * @param token  token
     * @param rq     reservation request
     * @return ReservationResponse
     */
    public ReservationResponse reserve(String userId, String token, ReservationRequest rq) {
        return eventsApi.reserve(userId, token, rq);
    }

    /**
     * Gets event info for given event ID.
     *
     * @param userId  user id
     * @param token   token
     * @param eventId event id
     * @return EventsDetailsResponse
     */
    public DetailedEventDetailsResponse getEventInfo(String userId, String token, BigDecimal eventId) {
        return eventsApi.getDetailedEvent(userId, token, eventId);
    }
}
