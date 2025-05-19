package org.example.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherResponse {
    private Location location;
    private Current current;
    private Forecast forecast;

    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }

    public Current getCurrent() { return current; }
    public void setCurrent(Current current) { this.current = current; }

    public Forecast getForecast() { return forecast; }
    public void setForecast(Forecast forecast) { this.forecast = forecast; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Location {
        private String name;
        private String region;
        private String country;
        private double lat;
        private double lon;
        private String tz_id;
        private String localtime;

        // Геттеры и сеттеры
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }

        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }

        public double getLat() { return lat; }
        public void setLat(double lat) { this.lat = lat; }

        public double getLon() { return lon; }
        public void setLon(double lon) { this.lon = lon; }

        public String getTz_id() { return tz_id; }
        public void setTz_id(String tz_id) { this.tz_id = tz_id; }

        public String getLocaltime() { return localtime; }
        public void setLocaltime(String localtime) { this.localtime = localtime; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Current {
        private float temp_c;
        private float temp_f;
        private Condition condition;

        public float getTemp_c() { return temp_c; }
        public void setTemp_c(float temp_c) { this.temp_c = temp_c; }

        public float getTemp_f() { return temp_f; }
        public void setTemp_f(float temp_f) { this.temp_f = temp_f; }

        public Condition getCondition() { return condition; }
        public void setCondition(Condition condition) { this.condition = condition; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Forecast {
        private List<ForecastDay> forecastday;

        public List<ForecastDay> getForecastday() { return forecastday; }
        public void setForecastday(List<ForecastDay> forecastday) { this.forecastday = forecastday; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ForecastDay {
        private String date;
        private List<Hour> hour;

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }

        public List<Hour> getHour() { return hour; }
        public void setHour(List<Hour> hour) { this.hour = hour; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Hour {
        private String time;
        private float temp_c;
        private Condition condition;

        public String getTime() { return time; }
        public void setTime(String time) { this.time = time; }

        public float getTemp_c() { return temp_c; }
        public void setTemp_c(float temp_c) { this.temp_c = temp_c; }

        public Condition getCondition() { return condition; }
        public void setCondition(Condition condition) { this.condition = condition; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Condition {
        private String text;

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }
}