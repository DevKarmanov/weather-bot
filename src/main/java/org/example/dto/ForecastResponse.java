package org.example.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ForecastResponse {
    public Forecast forecast;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Forecast {
        public List<ForecastDay> forecastday;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ForecastDay {
        public String date;
        public List<Hour> hour;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Hour {
        public String time;
        public float temp_c;
        public Condition condition;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Condition {
        public String text;
    }
}