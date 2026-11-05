package io.github.yairmendozav.booking.reservation.error;

public class BookingConflictException extends RuntimeException {

    public BookingConflictException() {
        super("El profesional ya tiene una reserva en ese horario.");
    }
}