package com.sch.energyBrokerService.application.service.emsp.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.sch.energyBrokerService.adapter.in.web.dto.Request.CreateEmspRequest;
import com.sch.energyBrokerService.application.exception.EntityNotFoundException;
import com.sch.energyBrokerService.domain.model.emsp.Emsp;
import com.sch.energyBrokerService.domain.model.emsp.EmspId;
import com.sch.energyBrokerService.domain.model.emsp.repository.EmspRepository;

class EmspCommandHandlerTest {

    private final EmspRepository repository = mock(EmspRepository.class);
    private final EmspCommandHandler handler = new EmspCommandHandler(repository);

    @Test
    void createsAndSavesAnEmspWithTheProvidedExternalId() {
        when(repository.findByExternalId("EMSP-001")).thenReturn(Optional.empty());
        when(repository.save(any(Emsp.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Emsp result = handler.create(new CreateEmspRequest("Mobility Spa", " EMSP-001 "));

        assertThat(result.name()).isEqualTo("Mobility Spa");
        assertThat(result.externalId()).isEqualTo("EMSP-001");
        verify(repository).save(any(Emsp.class));
    }

    @Test
    void rejectsAnExternalIdOwnedByAnotherEmsp() {
        Emsp existing = new Emsp(EmspId.newId(), "EMSP-001", "Existing eMSP");
        when(repository.findByExternalId("EMSP-001")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> handler.create(new CreateEmspRequest("New eMSP", "EMSP-001")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Emsp externalId already in use");

        verify(repository, never()).save(any());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "  "})
    void createsAnEmspWithoutLookingUpAnAbsentExternalId(String externalId) {
        when(repository.save(any(Emsp.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Emsp result = handler.create(new CreateEmspRequest("Mobility Spa", externalId));

        assertThat(result.externalId()).isNull();
        verify(repository, never()).findByExternalId(any());
        verify(repository).save(any(Emsp.class));
    }

    @Test
    void updatesAnExistingEmspAndPreservesItsId() {
        Emsp current = new Emsp(EmspId.newId(), "EMSP-001", "Old Name");
        when(repository.findById(current.id())).thenReturn(Optional.of(current));
        when(repository.findByExternalId("EMSP-002")).thenReturn(Optional.empty());
        when(repository.save(any(Emsp.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Emsp result = handler.update(
                current.id().value(), new CreateEmspRequest("New Name", "EMSP-002"));

        assertThat(result.id()).isEqualTo(current.id());
        assertThat(result.name()).isEqualTo("New Name");
        assertThat(result.externalId()).isEqualTo("EMSP-002");
        verify(repository).save(any(Emsp.class));
    }

    @Test
    void allowsAnEmspToKeepItsOwnExternalIdDuringUpdate() {
        Emsp current = new Emsp(EmspId.newId(), "EMSP-001", "Old Name");
        when(repository.findById(current.id())).thenReturn(Optional.of(current));
        when(repository.findByExternalId("EMSP-001")).thenReturn(Optional.of(current));
        when(repository.save(any(Emsp.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Emsp result = handler.update(
                current.id().value(), new CreateEmspRequest("New Name", "EMSP-001"));

        assertThat(result.id()).isEqualTo(current.id());
        assertThat(result.name()).isEqualTo("New Name");
        verify(repository).save(any(Emsp.class));
    }

    @Test
    void rejectsUpdatingToAnExternalIdOwnedByAnotherEmsp() {
        Emsp current = new Emsp(EmspId.newId(), "EMSP-001", "Current eMSP");
        Emsp other = new Emsp(EmspId.newId(), "EMSP-002", "Other eMSP");
        when(repository.findById(current.id())).thenReturn(Optional.of(current));
        when(repository.findByExternalId("EMSP-002")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> handler.update(
                current.id().value(), new CreateEmspRequest("Updated eMSP", "EMSP-002")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Emsp externalId already in use");

        verify(repository, never()).save(any());
    }

    @Test
    void updatesAnUnknownEmspWithNotFoundAndDoesNotSave() {
        UUID value = UUID.randomUUID();
        when(repository.findById(new EmspId(value))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.update(
                value, new CreateEmspRequest("Mobility Spa", null)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(value.toString());

        verify(repository, never()).save(any());
    }

    @Test
    void deletesAnExistingEmsp() {
        Emsp emsp = new Emsp(EmspId.newId(), null, "Mobility Spa");
        when(repository.findById(emsp.id())).thenReturn(Optional.of(emsp));

        handler.delete(emsp.id().value());

        verify(repository).deleteById(emsp.id());
    }

    @Test
    void doesNotDeleteAnUnknownEmsp() {
        UUID value = UUID.randomUUID();
        when(repository.findById(new EmspId(value))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.delete(value))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(value.toString());

        verify(repository, never()).deleteById(any());
    }
}
