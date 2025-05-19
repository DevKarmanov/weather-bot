package org.example.dto.AI.request;

import org.example.dto.AI.Message;

import java.util.List;

public record ChatRequest(
        String model,
        List<Message> messages
) {}