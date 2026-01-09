package com.sch.energyBrokerService.domain.model.supplier;

public class Supplier {

    private final SupplierId id;
    private final String name;

    public Supplier(SupplierId id, String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Supplier name is required");
        }
        this.id = id;
        this.name = name;
    }

    public SupplierId id() {
        return id;
    }

    public String name() {
        return name;
    }
}
