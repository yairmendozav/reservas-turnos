package io.github.yairmendozav.booking.reservation.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.dao.DataIntegrityViolationException;
import org.hibernate.exception.ConstraintViolationException;

import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class BookingExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(
            MethodArgumentNotValidException exception) {

        List<Map<String, String>> errors = exception
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> Map.of(
                        "field", error.getField(),
                        "message", error.getDefaultMessage() == null
                                ? "Valor inválido"
                                : error.getDefaultMessage()
                ))
                .toList();

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Uno o más campos no cumplen las validaciones."
        );

        problem.setTitle("Datos de entrada inválidos");
        problem.setProperty("errors", errors);

        return problem;
    }

    @ExceptionHandler(InvalidBookingPeriodException.class)
    public ProblemDetail handleInvalidBookingPeriod(
            InvalidBookingPeriodException exception) {

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );

        problem.setTitle("Horario de reserva inválido");

        return problem;
    }

    @ExceptionHandler(BookingConflictException.class)
    public ProblemDetail handleBookingConflict(
            BookingConflictException exception) {

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );

        problem.setTitle("Horario no disponible");

        return problem;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity(
            DataIntegrityViolationException exception) {

        Throwable cause = exception;

        while (cause != null) {
            if (cause instanceof ConstraintViolationException violation
                    && "ex_bookings_professional_time"
                    .equals(violation.getConstraintName())) {

                ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                        HttpStatus.CONFLICT,
                        "El profesional ya tiene una reserva en ese horario."
                );

                problem.setTitle("Horario no disponible");

                return problem;
            }

            cause = cause.getCause();
        }

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "No se pudo guardar la información."
        );

        problem.setTitle("Error de persistencia");

        return problem;
    }
}