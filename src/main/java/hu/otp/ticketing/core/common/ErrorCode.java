package hu.otp.ticketing.core.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ErrorCode {
    SUCCESS(0L),
    GENERAL_ERROR(20000L),
    NO_SUCH_EVENT(20001L),
    NO_SUCH_SEAT_OR_SEAT_ALREADY_TAKEN(20002L),
    EVENT_IN_THE_PAST(20003L),
    INSUFFICIENT_VESPENE_GAS(20004L),
    NO_SUCH_CARD(20005L),
    ERROR_DURING_TICKET_CALL(20006L),
    ERROR_DURING_RESERVATION_CALL(20007L),
    UNAUTHENTICATED_USER(20008L);

    private final Long code;

    ErrorCode(long l) {
        this.code = l;
    }
}

