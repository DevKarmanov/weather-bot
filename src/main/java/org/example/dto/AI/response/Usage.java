package org.example.dto.AI.response;

public class Usage {
    private int prompt_tokens;
    private int completion_tokens;
    private int total_tokens;
    private Object prompt_tokens_details;

    public int getPrompt_tokens() {
        return prompt_tokens;
    }

    public void setPrompt_tokens(int prompt_tokens) {
        this.prompt_tokens = prompt_tokens;
    }

    public int getCompletion_tokens() {
        return completion_tokens;
    }

    public void setCompletion_tokens(int completion_tokens) {
        this.completion_tokens = completion_tokens;
    }

    public int getTotal_tokens() {
        return total_tokens;
    }

    public void setTotal_tokens(int total_tokens) {
        this.total_tokens = total_tokens;
    }

    public Object getPrompt_tokens_details() {
        return prompt_tokens_details;
    }

    public void setPrompt_tokens_details(Object prompt_tokens_details) {
        this.prompt_tokens_details = prompt_tokens_details;
    }
}
