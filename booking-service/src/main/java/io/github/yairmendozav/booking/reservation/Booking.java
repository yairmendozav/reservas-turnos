package io.github.yairmendozav.booking.reservation;

import io.github.yairmendozav.booking.reservation.error.InvalidBookingPeriodException;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "customer_name", nullable = false, length = 120)
    private String customerName;

    @Column(name = "customer_email", nullable = false, length = 254)
    private String customerEmail;

    @Column(name = "professional_id", nullable = false)
    private UUID professionalId;

    @Column(name = "service_id", nullable = false)
    private UUID serviceId;

    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;

    @Column(name = "ends_at", nullable = false)
    private Instant endsAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BookingStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Booking() {
        // Constructor requerido por JPA.
    }

    public Booking(
        String customerName,
        String customerEmail,
        UUID professionalId,
        UUID serviceId,
        Instant startsAt,
        Instant endsAt) {

        if (startsAt == null || endsAt == null
                || !endsAt.isAfter(startsAt)) {
            throw new InvalidBookingPeriodException();
        }

        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.professionalId = professionalId;
        this.serviceId = serviceId;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.status = BookingStatus.CONFIRMED;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public UUID getProfessionalId() {
        return professionalId;
    }

    public UUID getServiceId() {
        return serviceId;
    }

    public Instant getStartsAt() {
        return startsAt;
    }

    public Instant getEndsAt() {
        return endsAt;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
