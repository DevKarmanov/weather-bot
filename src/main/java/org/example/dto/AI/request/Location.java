package org.example.dto.AI.request;

import java.util.Objects;

public class Location {
    private final String city;
    private final String country;

    public Location(String city, String country) {
        this.city = city;
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    @Override
    public String toString() {
        return "Location{" +
                "city='" + city + '\'' +
                ", country='" + country + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Location location = (Location) object;
        return Objects.equals(city, location.city) && Objects.equals(country, location.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(city, country);
    }
}
