package com.sch.energyBrokerService.dev;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.sch.energyBrokerService.adapter.out.persistence.repository.JpaAgreementRepository;
import com.sch.energyBrokerService.adapter.out.persistence.repository.JpaCpoRepository;
import com.sch.energyBrokerService.adapter.out.persistence.repository.JpaEmspRepository;

class DataInitializerTest {

    private final CpoCsvLoader cpoCsvLoader = mock(CpoCsvLoader.class);
    private final EmspLoader emspLoader = mock(EmspLoader.class);
    private final AgreementGenerator agreementGenerator = mock(AgreementGenerator.class);
    private final JpaAgreementRepository agreementRepository = mock(JpaAgreementRepository.class);
    private final JpaCpoRepository cpoRepository = mock(JpaCpoRepository.class);
    private final JpaEmspRepository emspRepository = mock(JpaEmspRepository.class);
    private final DataInitializer initializer = new DataInitializer(
            cpoCsvLoader,
            emspLoader,
            agreementGenerator,
            agreementRepository,
            cpoRepository,
            emspRepository);

    @Test
    void summarizesTheResultsOfEachInitializationStep() {
        when(cpoCsvLoader.load()).thenReturn(2);
        when(emspLoader.load()).thenReturn(3);
        when(agreementGenerator.generate()).thenReturn(4);

        assertThat(initializer.init()).containsExactly(
                Map.entry("cpos", 2),
                Map.entry("emsps", 3),
                Map.entry("agreements", 4));
    }

    @Test
    void resetClearsEveryRepositoryAndReturnsZeroCounts() {
        assertThat(initializer.reset()).containsExactly(
                Map.entry("cpos", 0),
                Map.entry("emsps", 0),
                Map.entry("agreements", 0));

        verify(agreementRepository).deleteAllInBatch();
        verify(cpoRepository).deleteAllInBatch();
        verify(emspRepository).deleteAllInBatch();
    }
}
