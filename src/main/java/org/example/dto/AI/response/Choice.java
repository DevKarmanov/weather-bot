package org.example.dto.AI.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.dto.AI.Message;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Choice {
    private Object logprobs;
    private String finish_reason;
    private String native_finish_reason;
    private int index;
    private Message message;

}
