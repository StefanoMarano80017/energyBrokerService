package com.sch.energyBrokerService.adapter.in.web.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.sch.energyBrokerService.application.exception.EntityNotFoundException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapsMissingEntitiesToNotFoundResponses() {
        var response = handler.handleNotFound(new EntityNotFoundException("Cpo not found"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("error", "Cpo not found");
    }

    @Test
    void mapsInvalidStateTransitionsToConflictResponses() {
        var response = handler.handleConflict(new IllegalStateException("Cannot accept"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).containsEntry("error", "Cannot accept");
    }

    @Test
    void mapsInvalidArgumentsToBadRequestResponses() {
        var response = handler.handleBadRequest(new IllegalArgumentException("Invalid price"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("error", "Invalid price");
    }
}
