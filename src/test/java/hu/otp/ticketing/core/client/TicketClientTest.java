package hu.otp.ticketing.core.client;

import hu.otp.ticketing.ticket.endpoint.rest.EventsApi;
import hu.otp.ticketing.ticket.endpoint.rest.model.ReservationRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TicketClientTest {
    public static final String USER_ID = "userId";
    public static final String TOKEN = "token";
    public static final BigDecimal EVENT_ID = BigDecimal.ONE;
    @Mock
    private EventsApi eventsApi;

    @InjectMocks
    private TicketClient ticketClient;

    @Test
    void getEventInfo() {
        ticketClient.getEventInfo(USER_ID, TOKEN, EVENT_ID);
        verify(eventsApi).getDetailedEvent(USER_ID, TOKEN, EVENT_ID);
    }

    @Test
    void reserve() {
        ReservationRequest rq = new ReservationRequest();
        ticketClient.reserve(USER_ID, TOKEN, rq);
        verify(eventsApi).reserve(USER_ID, TOKEN, rq);
    }
}
