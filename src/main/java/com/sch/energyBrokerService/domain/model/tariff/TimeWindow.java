package com.sch.energyBrokerService.domain.model.tariff;

import java.time.Instant;

public record TimeWindow(Instant from, Instant to) {

    public TimeWindow {
        if (from == null || to == null || !from.isBefore(to)) {
            throw new IllegalArgumentException("Invalid time window");
        }
    }

    public boolean overlaps(TimeWindow other) {
        return from.isBefore(other.to()) && to.isAfter(other.from());
    }
}
