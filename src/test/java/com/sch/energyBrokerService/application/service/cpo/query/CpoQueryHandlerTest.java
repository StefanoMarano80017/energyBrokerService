package com.sch.energyBrokerService.application.service.cpo.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.sch.energyBrokerService.domain.model.cpo.Cpo;
import com.sch.energyBrokerService.domain.model.cpo.CpoId;
import com.sch.energyBrokerService.domain.model.cpo.repository.CpoRepository;
import com.sch.energyBrokerService.domain.model.geo.Coordinate;

class CpoQueryHandlerTest {

    private final CpoRepository repository = mock(CpoRepository.class);
    private final CpoQueryHandler handler = new CpoQueryHandler(repository);

    @Test
    void searchesAllCposWhenTheExternalIdIsBlank() {
        List<Cpo> cpos = List.of(cpo("CPO-001"), cpo("CPO-002"));
        when(repository.findAll()).thenReturn(cpos);

        assertThat(handler.search("  ")).containsExactlyElementsOf(cpos);
        verify(repository).findAll();
    }

    @Test
    void trimsAnExternalIdAndReturnsAtMostItsMatchingCpo() {
        Cpo cpo = cpo("CPO-001");
        when(repository.findByExternalId("CPO-001")).thenReturn(Optional.of(cpo));

        assertThat(handler.search(" CPO-001 ")).containsExactly(cpo);
    }

    @Test
    void findsACpoByItsInternalId() {
        Cpo cpo = cpo("CPO-001");
        when(repository.findById(cpo.id())).thenReturn(Optional.of(cpo));

        assertThat(handler.findById(cpo.id().value())).contains(cpo);
    }

    @Test
    void returnsEmptyForAnUnknownInternalId() {
        UUID id = UUID.randomUUID();
        when(repository.findById(new CpoId(id))).thenReturn(Optional.empty());

        assertThat(handler.findById(id)).isEmpty();
    }

    @Test
    void returnsEmptyForAnUnknownExternalId() {
        when(repository.findByExternalId("CPO-999")).thenReturn(Optional.empty());

        assertThat(handler.findByExternalId(" CPO-999 ")).isEmpty();
    }

    @Test
    void findsACpoByItsExternalId() {
        Cpo cpo = cpo("CPO-001");
        when(repository.findByExternalId("CPO-001")).thenReturn(Optional.of(cpo));

        assertThat(handler.findByExternalId(" CPO-001 ")).contains(cpo);
        verify(repository).findByExternalId("CPO-001");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "  "})
    void returnsEmptyWithoutSearchingWhenTheExternalIdIsMissing(String externalId) {
        assertThat(handler.findByExternalId(externalId)).isEmpty();

        verify(repository, never()).findByExternalId(any());
    }

    private static Cpo cpo(String externalId) {
        return new Cpo(CpoId.newId(), externalId, "Charge Spa", new Coordinate(45, 9));
    }
}
