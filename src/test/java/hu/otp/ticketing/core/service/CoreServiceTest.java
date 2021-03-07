package hu.otp.ticketing.core.service;

import hu.otp.ticketing.core.client.TicketClient;
import hu.otp.ticketing.core.common.HeaderConstants;
import hu.otp.ticketing.core.common.exception.UnauthorizedException;
import hu.otp.ticketing.core.endpoint.rest.model.ChargeRequest;
import hu.otp.ticketing.core.endpoint.rest.model.ChargeResponse;
import hu.otp.ticketing.core.endpoint.rest.model.GeneralSuccessResponse;
import hu.otp.ticketing.core.persistence.entity.BankCard;
import hu.otp.ticketing.core.persistence.entity.Token;
import hu.otp.ticketing.core.persistence.entity.User;
import hu.otp.ticketing.core.persistence.repository.UserRepository;
import hu.otp.ticketing.ticket.endpoint.rest.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;

import static hu.otp.ticketing.core.common.ErrorCode.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoreServiceTest {
    public static final String USER_ID = "userId";
    public static final String TOKEN = "token";
    public static final String CARD_ID = "cardId";
    public static final BigDecimal AMOUNT = BigDecimal.valueOf(100);
    public static final BigDecimal EVENT_ID = BigDecimal.ONE;
    public static final String SEAT_ID = "seatID";
    @Mock
    private UserRepository userRepository;
    @Mock
    private HttpServletRequest rq;
    @Mock
    private TicketClient ticketClient;

    private CoreService coreService;

    @BeforeEach
    void setup() {
        MockHttpServletRequest rq = new MockHttpServletRequest();
        rq.addHeader(HeaderConstants.USER_ID, USER_ID);
        rq.addHeader(HeaderConstants.TOKEN, TOKEN);
        coreService = new CoreService(userRepository, rq, ticketClient);
    }

    @Test
    void charge_happy() {
        ChargeRequest rq = getChargeRequest();

        when(userRepository.findByUserId(USER_ID)).thenReturn(happyUser());
        when(ticketClient.getEventInfo(USER_ID, TOKEN, EVENT_ID)).thenReturn(happyInfo());
        when(ticketClient.reserve(eq(USER_ID), eq(TOKEN), any(ReservationRequest.class))).thenReturn(successResp());

        ChargeResponse charge = coreService.charge(rq);

        assertTrue(charge.getInfo().getSuccess());
    }

    @Test
    void charge_sad1() {
        ChargeRequest rq = getChargeRequest();
        when(userRepository.findByUserId(USER_ID)).thenReturn(null);

        assertThrows(UnauthorizedException.class, () -> coreService.charge(rq));
    }

    @Test
    void charge_sad2() {
        ChargeRequest rq = getChargeRequest();

        User user = happyUser();
        user.getCards().get(0).setCardId("nocard");
        when(userRepository.findByUserId(USER_ID)).thenReturn(user);

        ChargeResponse charge = coreService.charge(rq);

        assertFalse(charge.getInfo().getSuccess());
        assertEquals(NO_SUCH_CARD.getCode(), charge.getInfo().getState().getCode());
    }

    @Test
    void charge_sad3() {
        ChargeRequest rq = getChargeRequest();

        when(userRepository.findByUserId(USER_ID)).thenReturn(happyUser());

        DetailedEventDetailsResponse rp = new DetailedEventDetailsResponse();
        Info info = new Info();
        info.setSuccess(false);
        rp.setInfo(info);
        State state = new State();
        state.setCode(123L);
        info.setState(state);
        when(ticketClient.getEventInfo(USER_ID, TOKEN, EVENT_ID)).thenReturn(rp);

        ChargeResponse charge = coreService.charge(rq);

        assertFalse(charge.getInfo().getSuccess());
        assertEquals(123L, charge.getInfo().getState().getCode());
    }

    @Test
    void charge_sad4() {
        ChargeRequest rq = getChargeRequest();

        when(userRepository.findByUserId(USER_ID)).thenReturn(happyUser());

        DetailedEventDetailsResponse response = happyInfo();
        response.getEvent().setStartTimeStamp(OffsetDateTime.now().minusDays(1L));
        when(ticketClient.getEventInfo(USER_ID, TOKEN, EVENT_ID)).thenReturn(response);

        ChargeResponse charge = coreService.charge(rq);

        assertFalse(charge.getInfo().getSuccess());
        assertEquals(EVENT_IN_THE_PAST.getCode(), charge.getInfo().getState().getCode());
    }

    @Test
    void charge_sad5() {
        ChargeRequest rq = getChargeRequest();

        when(userRepository.findByUserId(USER_ID)).thenReturn(happyUser());

        DetailedEventDetailsResponse response = happyInfo();
        response.setSeats(null);
        when(ticketClient.getEventInfo(USER_ID, TOKEN, EVENT_ID)).thenReturn(response);

        ChargeResponse charge = coreService.charge(rq);

        assertFalse(charge.getInfo().getSuccess());
        assertEquals(NO_SUCH_SEAT_OR_SEAT_ALREADY_TAKEN.getCode(), charge.getInfo().getState().getCode());
    }

    @Test
    void charge_sad6() {
        ChargeRequest rq = getChargeRequest();

        when(userRepository.findByUserId(USER_ID)).thenReturn(happyUser());

        DetailedEventDetailsResponse response = happyInfo();
        response.getSeats().get(0).setReserved(true);
        when(ticketClient.getEventInfo(USER_ID, TOKEN, EVENT_ID)).thenReturn(response);

        ChargeResponse charge = coreService.charge(rq);

        assertFalse(charge.getInfo().getSuccess());
        assertEquals(NO_SUCH_SEAT_OR_SEAT_ALREADY_TAKEN.getCode(), charge.getInfo().getState().getCode());
    }

    @Test
    void charge_sad7() {
        ChargeRequest rq = getChargeRequest();

        User user = happyUser();
        user.getCards().get(0).setAmount(BigDecimal.ONE);
        when(userRepository.findByUserId(USER_ID)).thenReturn(user);
        when(ticketClient.getEventInfo(USER_ID, TOKEN, EVENT_ID)).thenReturn(happyInfo());

        ChargeResponse charge = coreService.charge(rq);

        assertFalse(charge.getInfo().getSuccess());
        assertEquals(INSUFFICIENT_VESPENE_GAS.getCode(), charge.getInfo().getState().getCode());
    }


    @Test
    void charge_sad8() {
        ChargeRequest rq = getChargeRequest();

        when(userRepository.findByUserId(USER_ID)).thenReturn(happyUser());
        when(ticketClient.getEventInfo(USER_ID, TOKEN, EVENT_ID)).thenReturn(happyInfo());
        when(ticketClient.reserve(eq(USER_ID), eq(TOKEN), any(ReservationRequest.class))).thenReturn(errorResp(456L));

        ChargeResponse charge = coreService.charge(rq);

        assertFalse(charge.getInfo().getSuccess());
        assertEquals(ERROR_DURING_RESERVATION_CALL.getCode(), charge.getInfo().getState().getCode());
    }

    @Test
    void isAuthenticated_happy() {
        when(userRepository.findByUserId(USER_ID)).thenReturn(happyUser());

        GeneralSuccessResponse authenticated = coreService.isAuthenticated();
        assertTrue(authenticated.getInfo().getSuccess());
        assertEquals(SUCCESS.getCode(), authenticated.getInfo().getState().getCode());
        assertEquals(SUCCESS.name(), authenticated.getInfo().getState().getReason());
    }

    @Test
    void isAuthenticated_sad() {
        when(userRepository.findByUserId(USER_ID)).thenReturn(null);

        GeneralSuccessResponse authenticated = coreService.isAuthenticated();
        assertFalse(authenticated.getInfo().getSuccess());
        assertEquals(UNAUTHENTICATED_USER.getCode(), authenticated.getInfo().getState().getCode());
        assertEquals(UNAUTHENTICATED_USER.name(), authenticated.getInfo().getState().getReason());
    }

    @Test
    void isAuthenticated_sad2() {
        User user = happyUser();
        Token token = new Token();
        token.setToken("asdf");
        user.setTokens(Collections.singletonList(token));
        when(userRepository.findByUserId(USER_ID)).thenReturn(user);

        GeneralSuccessResponse authenticated = coreService.isAuthenticated();
        assertFalse(authenticated.getInfo().getSuccess());
        assertEquals(UNAUTHENTICATED_USER.getCode(), authenticated.getInfo().getState().getCode());
        assertEquals(UNAUTHENTICATED_USER.name(), authenticated.getInfo().getState().getReason());
    }

    private ChargeRequest getChargeRequest() {
        ChargeRequest rq = new ChargeRequest();
        rq.setCardId(CARD_ID);
        rq.setEventId(BigDecimal.ONE);
        rq.setSeatId(SEAT_ID);
        return rq;
    }

    private User happyUser() {
        User user = new User();
        BankCard card = new BankCard();
        card.setCardId(CARD_ID);
        card.setAmount(AMOUNT);
        user.setCards(Collections.singletonList(card));
        Token token = new Token();
        token.setToken(TOKEN);
        user.setTokens(Collections.singletonList(token));
        return user;
    }

    private DetailedEventDetailsResponse happyInfo() {
        DetailedEventDetailsResponse response = new DetailedEventDetailsResponse();
        Info info = new Info();
        info.setSuccess(true);
        response.setInfo(info);
        Event event = new Event();
        event.startTimeStamp(OffsetDateTime.now().plusDays(1L));
        response.setEvent(event);
        Seat seat = new Seat();
        seat.setPrice(AMOUNT.subtract(BigDecimal.TEN));
        seat.setReserved(false);
        seat.setId(SEAT_ID);
        response.setSeats(Collections.singletonList(seat));
        return response;
    }

    private ReservationResponse successResp() {
        ReservationResponse response = new ReservationResponse();
        Info info = new Info();
        info.setSuccess(true);
        response.setInfo(info);
        return response;
    }

    private ReservationResponse errorResp(long code) {
        ReservationResponse rp = new ReservationResponse();
        Info info = new Info();
        info.setSuccess(false);
        rp.setInfo(info);
        State state = new State();
        state.setCode(code);
        info.setState(state);
        return rp;
    }
}
