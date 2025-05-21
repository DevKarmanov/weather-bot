package org.example.dto.AI.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OpenRouterResponse {
    private String id;
    private String provider;
    private String model;
    private String object;
    private long created;
    private List<Choice> choices;
    private Usage usage;

    @Override
    public String toString() {
        return "OpenRouterResponse{" +
                "id='" + id + '\'' +
                ", provider='" + provider + '\'' +
                ", model='" + model + '\'' +
                ", object='" + object + '\'' +
                ", created=" + created +
                ", choices=" + choices +
                ", usage=" + usage +
                '}';
    }
}
