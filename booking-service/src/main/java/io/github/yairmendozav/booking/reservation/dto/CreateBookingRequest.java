package io.github.yairmendozav.booking.reservation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public record CreateBookingRequest(
        @NotBlank
        @Size(max = 120)
        String customerName,

        @NotBlank
        @Email
        @Size(max = 254)
        String customerEmail,

        @NotNull
        UUID professionalId,

        @NotNull
        UUID serviceId,

        @NotNull
        @Future
        Instant startsAt,

        @NotNull
        @Future
        Instant endsAt
) {}