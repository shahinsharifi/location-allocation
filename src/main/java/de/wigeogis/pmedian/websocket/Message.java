package de.wigeogis.pmedian.websocket;

import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public record Message(
    UUID sessionId,
    MessageType type,
    MessageSubject subject,
    String message,
    Map<String, Object> data) {}
