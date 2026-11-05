package com.sch.energyBrokerService.application.service.cpo.command;

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

import com.sch.energyBrokerService.adapter.in.web.dto.Request.CreateCpoRequest;
import com.sch.energyBrokerService.application.exception.EntityNotFoundException;
import com.sch.energyBrokerService.domain.model.cpo.Cpo;
import com.sch.energyBrokerService.domain.model.cpo.CpoId;
import com.sch.energyBrokerService.domain.model.cpo.repository.CpoRepository;
import com.sch.energyBrokerService.domain.model.geo.Coordinate;

class CpoCommandHandlerTest {

    private final CpoRepository repository = mock(CpoRepository.class);
    private final CpoCommandHandler handler = new CpoCommandHandler(repository);

    @Test
    void createsAndSavesACpoWithTheProvidedExternalId() {
        when(repository.findByExternalId("CPO-001")).thenReturn(Optional.empty());
        when(repository.save(any(Cpo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cpo result = handler.create(new CreateCpoRequest("Charge Spa", 45.0, 9.0, " CPO-001 "));

        assertThat(result.name()).isEqualTo("Charge Spa");
        assertThat(result.externalId()).isEqualTo("CPO-001");
        assertThat(result.location().latitude()).isEqualTo(45.0);
        verify(repository).save(any(Cpo.class));
    }

    @Test
    void rejectsAnExternalIdOwnedByAnotherCpo() {
        Cpo existing = new Cpo(
                CpoId.newId(), "CPO-001", "Existing CPO",
                new Coordinate(45, 9));
        when(repository.findByExternalId("CPO-001")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> handler.create(new CreateCpoRequest("New CPO", 45.0, 9.0, "CPO-001")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cpo externalId already in use");

        verify(repository, never()).save(any());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "  "})
    void createsACpoWithoutLookingUpAnAbsentExternalId(String externalId) {
        when(repository.save(any(Cpo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cpo result = handler.create(new CreateCpoRequest("Charge Spa", 45.0, 9.0, externalId));

        assertThat(result.externalId()).isNull();
        verify(repository, never()).findByExternalId(any());
        verify(repository).save(any(Cpo.class));
    }

    @Test
    void permitsUpdatingTheCpoThatAlreadyOwnsItsExternalId() {
        UUID value = UUID.randomUUID();
        CpoId id = new CpoId(value);
        Cpo current = new Cpo(
                id, "CPO-001", "Old Name",
                new Coordinate(45, 9));
        when(repository.findById(id)).thenReturn(Optional.of(current));
        when(repository.findByExternalId("CPO-001")).thenReturn(Optional.of(current));
        when(repository.save(any(Cpo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cpo result = handler.update(value, new CreateCpoRequest("New Name", 46.0, 10.0, "CPO-001"));

        assertThat(result.id()).isEqualTo(id);
        assertThat(result.name()).isEqualTo("New Name");
        verify(repository).save(any(Cpo.class));
    }

    @Test
    void rejectsUpdatingToAnExternalIdOwnedByAnotherCpo() {
        UUID value = UUID.randomUUID();
        CpoId id = new CpoId(value);
        Cpo current = new Cpo(id, "CPO-001", "Current CPO", new Coordinate(45, 9));
        Cpo other = new Cpo(CpoId.newId(), "CPO-002", "Other CPO", new Coordinate(46, 10));
        when(repository.findById(id)).thenReturn(Optional.of(current));
        when(repository.findByExternalId("CPO-002")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> handler.update(
                value, new CreateCpoRequest("Updated CPO", 45.0, 9.0, "CPO-002")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cpo externalId already in use");

        verify(repository, never()).save(any());
    }

    @Test
    void updatesAnUnknownCpoWithNotFoundAndDoesNotSave() {
        UUID value = UUID.randomUUID();
        when(repository.findById(new CpoId(value))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.update(
                value, new CreateCpoRequest("Charge Spa", 45.0, 9.0, null)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(value.toString());

        verify(repository, never()).save(any());
    }

    @Test
    void deletesAnExistingCpo() {
        Cpo cpo = new Cpo(CpoId.newId(), null, "Charge Spa", new Coordinate(45, 9));
        when(repository.findById(cpo.id())).thenReturn(Optional.of(cpo));

        handler.delete(cpo.id().value());

        verify(repository).deleteById(cpo.id());
    }

    @Test
    void doesNotDeleteAnUnknownCpo() {
        UUID value = UUID.randomUUID();
        when(repository.findById(new CpoId(value))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.delete(value))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(value.toString());

        verify(repository, never()).deleteById(any());
    }
}
