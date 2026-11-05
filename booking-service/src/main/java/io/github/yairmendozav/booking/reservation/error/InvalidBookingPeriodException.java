package io.github.yairmendozav.booking.reservation.error;

public class InvalidBookingPeriodException extends RuntimeException {

    public InvalidBookingPeriodException() {
        super("El fin de la reserva debe ser posterior al inicio.");
    }
}