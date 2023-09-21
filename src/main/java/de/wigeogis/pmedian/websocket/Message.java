package de.wigeogis.pmedian.websocket;

import java.util.UUID;

public record Message(
    UUID sessionId,
    MessageType type,
    MessageSubject subject,
    String message,
    Object data) {}
