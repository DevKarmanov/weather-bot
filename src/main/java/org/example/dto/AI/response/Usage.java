package org.example.dto.AI.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Usage {
    private int prompt_tokens;
    private int completion_tokens;
    private int total_tokens;
    private Object prompt_tokens_details;

}
