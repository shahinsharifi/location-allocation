package de.wigeogis.pmedian.websocket;

import java.util.UUID;

public record Message(
    UUID sessionId,
    MessageSubject subject,
    String message,
		Object metadata,
    Object data) {}
