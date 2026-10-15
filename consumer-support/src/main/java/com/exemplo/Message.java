package com.exemplo;

import java.time.LocalDateTime;
import java.util.UUID;

public class Message {
    private String id;
    private String timestamp;
    private String type; // "review.product" ou "support.request"
    private String content;

    public Message() {}

    public Message(String type, String content) {
        this.id = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now().toString();
        this.type = type;
        this.content = content;
    }

    // Getters e Setters
    public String getId() { return id; }
    public String getTimestamp() { return timestamp; }
    public String getType() { return type; }
    public String getContent() { return content; }
}