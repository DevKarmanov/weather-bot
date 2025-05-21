package org.example.dto.AI;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    private String role;
    private String content;
    private Object refusal;
    private Object reasoning;

}
