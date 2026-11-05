package io.github.yairmendozav.booking.reservation;

import io.github.yairmendozav.booking.reservation.dto.CreateBookingRequest;
import io.github.yairmendozav.booking.reservation.dto.BookingResponse;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/bookings")
public class BookingController {
    private final BookingService bookingService;

    public BookingController(BookingService bookingService){
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<Map<String, UUID>> create(
            @Valid @RequestBody CreateBookingRequest request) {

        Booking booking = bookingService.create(
                request.customerName(),
                request.customerEmail(),
                request.professionalId(),
                request.serviceId(),
                request.startsAt(),
                request.endsAt()
        );

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(booking.getId())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(Map.of("id", booking.getId()));
    }
    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> findById(
            @PathVariable("id") UUID id) {

        return bookingService.findById(id)
                .map(BookingResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
