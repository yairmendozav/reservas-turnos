package io.github.yairmendozav.booking.reservation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import io.github.yairmendozav.booking.reservation.error.BookingConflictException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;


@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final JdbcTemplate jdbcTemplate;

    public BookingService(
            BookingRepository bookingRepository,
            JdbcTemplate jdbcTemplate) {

        this.bookingRepository = bookingRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public Booking create(
            String customerName,
            String customerEmail,
            UUID professionalId,
            UUID serviceId,
            Instant startsAt,
            Instant endsAt) {

        Booking booking = new Booking(
                customerName,
                customerEmail,
                professionalId,
                serviceId,
                startsAt,
                endsAt
        );

        jdbcTemplate.queryForList(
                """
                SELECT pg_advisory_xact_lock(
                    hashtextextended(CAST(? AS text), 0)
                )
                """,
                professionalId.toString()
        );

        long overlapping = bookingRepository.countOverlapping(
                professionalId,
                BookingStatus.CONFIRMED,
                startsAt,
                endsAt
        );

        if (overlapping > 0) {
            throw new BookingConflictException();
        }

        return bookingRepository.save(booking);
    }

    @Transactional(readOnly = true)
    public Optional<Booking> findById(UUID id) {
        return bookingRepository.findById(id);
    }
}