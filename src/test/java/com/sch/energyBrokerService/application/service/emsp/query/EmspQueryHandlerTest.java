package com.sch.energyBrokerService.application.service.emsp.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.sch.energyBrokerService.domain.model.emsp.Emsp;
import com.sch.energyBrokerService.domain.model.emsp.EmspId;
import com.sch.energyBrokerService.domain.model.emsp.repository.EmspRepository;

class EmspQueryHandlerTest {

    private final EmspRepository repository = mock(EmspRepository.class);
    private final EmspQueryHandler handler = new EmspQueryHandler(repository);

    @Test
    void filtersNamesWithoutCaseSensitivity() {
        Emsp first = new Emsp(EmspId.newId(), null, "Mobility Spa");
        Emsp second = new Emsp(EmspId.newId(), null, "Other Provider");
        when(repository.findAll()).thenReturn(List.of(first, second));

        assertThat(handler.search("MOBILITY", null)).containsExactly(first);
    }

    @Test
    void appliesNameFilterToAnExternalIdMatch() {
        Emsp emsp = new Emsp(EmspId.newId(), "EMSP-001", "Mobility Spa");
        when(repository.findByExternalId("EMSP-001")).thenReturn(Optional.of(emsp));

        assertThat(handler.search("mobility", " EMSP-001 ")).containsExactly(emsp);
        assertThat(handler.search("other", "EMSP-001")).isEmpty();
    }

    @Test
    void returnsAllEmspResultsWhenBothFiltersAreBlank() {
        List<Emsp> emsps = List.of(
                new Emsp(EmspId.newId(), null, "Mobility Spa"),
                new Emsp(EmspId.newId(), null, "Other Provider"));
        when(repository.findAll()).thenReturn(emsps);

        assertThat(handler.search("  ", " ")).containsExactlyElementsOf(emsps);
        verify(repository).findAll();
    }

    @Test
    void findsAnEmspByItsInternalId() {
        Emsp emsp = new Emsp(EmspId.newId(), "EMSP-001", "Mobility Spa");
        when(repository.findById(emsp.id())).thenReturn(Optional.of(emsp));

        assertThat(handler.findById(emsp.id().value())).contains(emsp);
    }

    @Test
    void returnsEmptyForAnUnknownInternalId() {
        UUID id = UUID.randomUUID();
        when(repository.findById(new EmspId(id))).thenReturn(Optional.empty());

        assertThat(handler.findById(id)).isEmpty();
    }

    @Test
    void trimsAnExternalIdWhenLookingUpAnEmsp() {
        Emsp emsp = new Emsp(EmspId.newId(), "EMSP-001", "Mobility Spa");
        when(repository.findByExternalId("EMSP-001")).thenReturn(Optional.of(emsp));

        assertThat(handler.findByExternalId(" EMSP-001 ")).contains(emsp);
        assertThat(handler.findByExternalId("  ")).isEmpty();
    }

    @Test
    void returnsEmptyWithoutSearchingWhenExternalIdIsNull() {
        assertThat(handler.findByExternalId(null)).isEmpty();
    }
}
