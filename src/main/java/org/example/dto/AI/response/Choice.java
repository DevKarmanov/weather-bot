package org.example.dto.AI.response;

import org.example.dto.AI.Message;

public class Choice {
    private Object logprobs;
    private String finish_reason;
    private String native_finish_reason;
    private int index;
    private Message message;

    public Object getLogprobs() {
        return logprobs;
    }

    public void setLogprobs(Object logprobs) {
        this.logprobs = logprobs;
    }

    public String getFinish_reason() {
        return finish_reason;
    }

    public void setFinish_reason(String finish_reason) {
        this.finish_reason = finish_reason;
    }

    public String getNative_finish_reason() {
        return native_finish_reason;
    }

    public void setNative_finish_reason(String native_finish_reason) {
        this.native_finish_reason = native_finish_reason;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
}
