package com.sch.energyBrokerService.domain.model.geo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class CoordinateTest {

    @Test
    void createsAValidCoordinate() {
        Coordinate coordinate = new Coordinate(45.464211, 9.191383);

        assertThat(coordinate.latitude()).isEqualTo(45.464211);
        assertThat(coordinate.longitude()).isEqualTo(9.191383);
        assertThat(coordinate).isEqualTo(new Coordinate(45.464211, 9.191383));
    }

    @Test
    void rejectsLatitudeOutsideSupportedRange() {
        assertThatThrownBy(() -> new Coordinate(-90.000001, 9.191383))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Latitude");

        assertThatThrownBy(() -> new Coordinate(90.000001, 9.191383))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Latitude");
    }

    @Test
    void rejectsLongitudeOutsideSupportedRange() {
        assertThatThrownBy(() -> new Coordinate(45.464211, -180.000001))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Longitude");

        assertThatThrownBy(() -> new Coordinate(45.464211, 180.000001))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Longitude");
    }
}
