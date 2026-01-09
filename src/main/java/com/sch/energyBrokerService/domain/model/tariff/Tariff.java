package com.sch.energyBrokerService.domain.model.tariff;

import com.sch.energyBrokerService.domain.model.geo.Coordinate;
import com.sch.energyBrokerService.domain.model.geo.GeoArea;
import com.sch.energyBrokerService.domain.model.supplier.SupplierId;

public class Tariff {

    private final TariffId id;
    private final SupplierId supplierId;
    private final TariffType type;
    private final PricePerKwh pricePerKwh;
    private final GeoArea geoArea;

    public Tariff(
            TariffId id,
            SupplierId supplierId,
            TariffType type,
            PricePerKwh pricePerKwh,
            GeoArea geoArea
    ) {
        this.id = id;
        this.supplierId = supplierId;
        this.type = type;
        this.pricePerKwh = pricePerKwh;
        this.geoArea = geoArea;
    }

    public boolean isApplicable(Coordinate coordinate, TimeWindow requestTime) {
        return geoArea.contains(coordinate);
    }

    public TariffId id() {
        return id;
    }

    public SupplierId supplierId() {
        return supplierId;
    }

    public PricePerKwh pricePerKwh() {
        return pricePerKwh;
    }

    public TariffType type() {
        return type;
    }
    
    public GeoArea geoArea() {
        return geoArea;
    }
}
