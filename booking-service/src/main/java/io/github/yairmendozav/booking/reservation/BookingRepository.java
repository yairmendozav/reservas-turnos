package io.github.yairmendozav.booking.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface BookingRepository
        extends JpaRepository<Booking, UUID> {

    @Query("""
    SELECT COUNT(b)
    FROM Booking b
    WHERE b.professionalId = :professionalId
      AND b.status = :status
      AND b.startsAt < :endsAt
      AND b.endsAt > :startsAt
    """)
    long countOverlapping(
            @Param("professionalId") UUID professionalId,
            @Param("status") BookingStatus status,
            @Param("startsAt") Instant startsAt,
            @Param("endsAt") Instant endsAt
    );
}



