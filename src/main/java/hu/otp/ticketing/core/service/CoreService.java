package hu.otp.ticketing.core.service;

import hu.otp.ticketing.core.client.TicketClient;
import hu.otp.ticketing.core.common.ErrorCode;
import hu.otp.ticketing.core.common.exception.UnauthorizedException;
import hu.otp.ticketing.core.endpoint.rest.model.*;
import hu.otp.ticketing.core.persistence.entity.BankCard;
import hu.otp.ticketing.core.persistence.entity.User;
import hu.otp.ticketing.core.persistence.repository.UserRepository;
import hu.otp.ticketing.ticket.endpoint.rest.model.DetailedEventDetailsResponse;
import hu.otp.ticketing.ticket.endpoint.rest.model.ReservationRequest;
import hu.otp.ticketing.ticket.endpoint.rest.model.ReservationResponse;
import hu.otp.ticketing.ticket.endpoint.rest.model.Seat;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.Optional;

import static hu.otp.ticketing.core.common.ErrorCode.*;
import static hu.otp.ticketing.core.common.HeaderConstants.TOKEN;
import static hu.otp.ticketing.core.common.HeaderConstants.USER_ID;

@Service
@RequiredArgsConstructor
public class CoreService {
    private final UserRepository userRepository;
    private final HttpServletRequest rq;
    private final TicketClient ticketClient;

    /**
     * Does authentication.based on the {@link HttpServletRequest}'s header.
     *
     * @return authentication response.
     */
    public GeneralSuccessResponse isAuthenticated() {
        String userId = rq.getHeader(USER_ID);
        String token = rq.getHeader(TOKEN);

        User user = userRepository.findByUserId(userId);

        GeneralSuccessResponse rp = new GeneralSuccessResponse();

        //TODO: proper authentication here
        rp.setInfo(createInfo(setState(new State(), isLoggedIn(token, user) ? SUCCESS : UNAUTHENTICATED_USER)));
        return rp;
    }

    private boolean isLoggedIn(String token, User user) {
        return user != null && user.getTokens().stream().anyMatch(t -> t.getToken().equals(token));
    }

    /**
     * Sends the reservation request to TICKET if the user 1) is logged in
     * 2) has the card 3) has enough money and 4) the event is valid
     *
     * @param chargeRequest charge rq
     * @return GeneralSuccessResponse
     */
    public ChargeResponse charge(ChargeRequest chargeRequest) {
        String userId = rq.getHeader(USER_ID);
        String token = rq.getHeader(TOKEN);
        State state = new State();
        ChargeResponse chargeResponse = new ChargeResponse();
        chargeResponse.setInfo(new Info());

        User user = userRepository.findByUserId(userId);

        if (isLoggedIn(token, user)) {
            Optional<BankCard> card = user.getCards().stream().filter(c -> c.getCardId().equals(chargeRequest.getCardId())).findFirst();
            if (card.isPresent()) {
                DetailedEventDetailsResponse eventInfo = ticketClient.getEventInfo(userId, token, chargeRequest.getEventId());
                if (eventInfo.getInfo().getSuccess()) {
                    ReservationRequest rq = new ReservationRequest();
                    rq.seatId(chargeRequest.getSeatId());
                    rq.setEventId(chargeRequest.getEventId());
                    chargeResponse = makeReservation(chargeRequest, card, eventInfo, userId, token, rq);

                } else {
                    //unsuccessful
                    state.setReason(eventInfo.getInfo().getState().getReason());
                    state.setCode(eventInfo.getInfo().getState().getCode());
                    chargeResponse.getInfo().setState(state);
                }
            } else {
                // no such card
                state.setReason(NO_SUCH_CARD.name());
                state.setCode(NO_SUCH_CARD.getCode());
                chargeResponse.getInfo().setState(state);
            }
        } else {
            throw new UnauthorizedException(String.format("No such user or invalid token! %s - %s", userId, token));
        }

        chargeResponse.getInfo().setSuccess(chargeResponse.getInfo().getState().getCode() == 0L);
        return chargeResponse;
    }

    private Info createInfo(State state) {
        Info info = new Info();
        info.setSuccess(state.getCode() == 0);
        info.setState(state);
        return info;
    }

    private ChargeResponse makeReservation(ChargeRequest chargeRequest, Optional<BankCard> card,
                                           DetailedEventDetailsResponse eventInfo, String userId, String token, ReservationRequest rq) {
        State state = new State();
        ChargeResponse chargeResponse = new ChargeResponse();
        chargeResponse.setInfo(new hu.otp.ticketing.core.endpoint.rest.model.Info());
        if (eventInfo.getEvent().getStartTimeStamp().compareTo(OffsetDateTime.now()) > 0) {
            Optional<Seat> seat = CollectionUtils.emptyIfNull(eventInfo.getSeats()).stream().filter(s -> s.getId().equals(chargeRequest.getSeatId())).findFirst();
            if (seat.isPresent() && !seat.get().getReserved()) {
                if (seat.get().getPrice().compareTo(card.get().getAmount()) < 1) {
                    //TODO: convert currency, subtract money etc.
                    ReservationResponse rp = ticketClient.reserve(userId, token, rq);
                    if (rp.getInfo().getSuccess()) {
                        chargeResponse.getInfo().setState(setState(state, SUCCESS));
                    } else {
                        chargeResponse.getInfo().setState(setState(state, ERROR_DURING_RESERVATION_CALL));
                    }
                } else {
                    chargeResponse.getInfo().setState(setState(state, INSUFFICIENT_VESPENE_GAS));
                }
            } else {
                chargeResponse.getInfo().setState(setState(state, NO_SUCH_SEAT_OR_SEAT_ALREADY_TAKEN));
            }
        } else {
            chargeResponse.getInfo().setState(setState(state, EVENT_IN_THE_PAST));
        }
        return chargeResponse;
    }

    private State setState(State state, ErrorCode code) {
        state.setReason(code.name());
        state.setCode(code.getCode());
        return state;
    }

}
