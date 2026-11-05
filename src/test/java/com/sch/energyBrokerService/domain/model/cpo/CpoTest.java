package com.sch.energyBrokerService.domain.model.cpo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sch.energyBrokerService.domain.model.geo.Coordinate;
import org.junit.jupiter.api.Test;

class CpoTest {

    private static final Coordinate MILAN = new Coordinate(45.464211, 9.191383);

    @Test
    void keepsTheExternalIdWhenProvided() {
        Cpo cpo = new Cpo(CpoId.newId(), "CPO-001", "Charge Spa", MILAN);
        assertThat(cpo.externalId()).isEqualTo("CPO-001");
    }

    @Test
    void allowsACpoWithoutExternalId() {
        Cpo cpo = new Cpo(CpoId.newId(), null, "Charge Spa", MILAN);

        assertThat(cpo.externalId()).isNull();
        assertThat(cpo.name()).isEqualTo("Charge Spa");
    }

    @Test
    void normalizesBlankExternalIdToNull() {
        Cpo cpo = new Cpo(CpoId.newId(), "   ", "Charge Spa", MILAN);

        assertThat(cpo.externalId()).isNull();
    }

    @Test
    void trimsTheExternalId() {
        Cpo cpo = new Cpo(CpoId.newId(), "  CPO-001  ", "Charge Spa", MILAN);

        assertThat(cpo.externalId()).isEqualTo("CPO-001");
    }

    @Test
    void stillRejectsABlankName() {
        assertThatThrownBy(() -> new Cpo(CpoId.newId(), "CPO-001", " ", MILAN))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("name is required");
    }

    @Test
    void stillRejectsAMissingLocation() {
        assertThatThrownBy(() -> new Cpo(CpoId.newId(), "CPO-001", "Charge Spa", null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("location is required");
    }

    @Test
    void rejectsANullId() {
        assertThatThrownBy(() -> new Cpo(null, "CPO-001", "Charge Spa", MILAN))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("id is required");
    }

    @Test
    void rejectsANullName() {
        assertThatThrownBy(() -> new Cpo(CpoId.newId(), "CPO-001", null, MILAN))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("name is required");
    }

    @Test
    void rejectsAnEmptyName() {
        assertThatThrownBy(() -> new Cpo(CpoId.newId(), "CPO-001", "", MILAN))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("name is required");
    }

    @Test
    void normalizesEmptyExternalIdToNull() {
        Cpo cpo = new Cpo(CpoId.newId(), "", "Charge Spa", MILAN);

        assertThat(cpo.externalId()).isNull();
    }

    @Test
    void normalizesExternalIdContainingOnlyTabsAndSpacesToNull() {
        Cpo cpo = new Cpo(CpoId.newId(), " \t  ", "Charge Spa", MILAN);

        assertThat(cpo.externalId()).isNull();
    }

    @Test
    void preservesInternalSpacesInExternalId() {
        Cpo cpo = new Cpo(CpoId.newId(), " CPO 001 ", "Charge Spa", MILAN);

        assertThat(cpo.externalId()).isEqualTo("CPO 001");
    }
}
