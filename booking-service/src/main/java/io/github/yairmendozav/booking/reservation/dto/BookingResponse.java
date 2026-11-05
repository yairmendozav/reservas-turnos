package io.github.yairmendozav.booking.reservation.dto;

import io.github.yairmendozav.booking.reservation.Booking;
import io.github.yairmendozav.booking.reservation.BookingStatus;

import java.time.Instant;
import java.util.UUID;

public record BookingResponse(
        UUID id,
        String customerName,
        String customerEmail,
        UUID professionalId,
        UUID serviceId,
        Instant startsAt,
        Instant endsAt,
        BookingStatus status,
        Instant createdAt
) {

    public static BookingResponse from(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getCustomerName(),
                booking.getCustomerEmail(),
                booking.getProfessionalId(),
                booking.getServiceId(),
                booking.getStartsAt(),
                booking.getEndsAt(),
                booking.getStatus(),
                booking.getCreatedAt()
        );
    }
}