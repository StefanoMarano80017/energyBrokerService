package com.sch.energyBrokerService.domain.model.geo;

public final class GeoArea {

    private final Coordinate center;
    private final double radiusKm;

    public GeoArea(Coordinate center, double radiusKm) {
        if (radiusKm <= 0) {
            throw new IllegalArgumentException("Radius must be positive");
        }
        this.center = center;
        this.radiusKm = radiusKm;
    }

    public Coordinate center() {
        return center;
    }

    public double radiusKm() {
        return radiusKm;
    }

    public boolean contains(Coordinate point) {
        return haversineKm(center, point) <= radiusKm;
    }

    // Formula Haversine
    private double haversineKm(Coordinate a, Coordinate b) {
        final int EARTH_RADIUS = 6371;

        double latDistance = Math.toRadians(b.latitude() - a.latitude());
        double lonDistance = Math.toRadians(b.longitude() - a.longitude());

        double sinLat = Math.sin(latDistance / 2);
        double sinLon = Math.sin(lonDistance / 2);

        double aa = sinLat * sinLat +
                Math.cos(Math.toRadians(a.latitude())) *
                Math.cos(Math.toRadians(b.latitude())) *
                sinLon * sinLon;

        double c = 2 * Math.atan2(Math.sqrt(aa), Math.sqrt(1 - aa));
        return EARTH_RADIUS * c;
    }
}
