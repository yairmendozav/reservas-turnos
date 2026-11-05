package io.github.yairmendozav.booking.reservation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class BookingConcurrencyTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17");

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldCreateOnlyOneBookingForConcurrentRequests()
            throws Exception {

        UUID professionalId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();

        Instant startsAt = Instant.now().plus(Duration.ofDays(7));
        Instant endsAt = startsAt.plus(Duration.ofMinutes(30));

        String body = """
                {
                  "customerName": "Yair",
                  "customerEmail": "yair@example.com",
                  "professionalId": "%s",
                  "serviceId": "%s",
                  "startsAt": "%s",
                  "endsAt": "%s"
                }
                """.formatted(
                professionalId,
                serviceId,
                startsAt,
                endsAt
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(
                        "http://localhost:" + port + "/bookings"
                ))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try (HttpClient client = HttpClient.newHttpClient();
             var executor = Executors.newVirtualThreadPerTaskExecutor()) {

            Callable<Integer> sendRequest = () -> {
                ready.countDown();

                if (!start.await(10, TimeUnit.SECONDS)) {
                    throw new IllegalStateException(
                            "No se recibió la señal de inicio"
                    );
                }

                return client.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                ).statusCode();
            };

            var first = executor.submit(sendRequest);
            var second = executor.submit(sendRequest);

            try {
                assertTrue(ready.await(10, TimeUnit.SECONDS));
            } finally {
                start.countDown();
            }

            List<Integer> statuses = List.of(
                    first.get(30, TimeUnit.SECONDS),
                    second.get(30, TimeUnit.SECONDS)
            ).stream().sorted().toList();

            assertEquals(List.of(201, 409), statuses);
        }

        Long savedBookings = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM bookings
                WHERE professional_id = ?
                """,
                Long.class,
                professionalId
        );

        assertEquals(Long.valueOf(1), savedBookings);
    }
}