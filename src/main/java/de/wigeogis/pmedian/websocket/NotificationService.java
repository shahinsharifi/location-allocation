package de.wigeogis.pmedian.websocket;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@AllArgsConstructor
public class NotificationService {

  private SimpMessagingTemplate messagingTemplate;

  public void publishLog(UUID sessionId, MessageSubject subject, String optimizationLog) {
    if (sessionId == null || subject == null || optimizationLog == null) {
      log.info("Unable to publish log due to null parameters");
      return;
    }
    this.sendMessage(sessionId, subject, optimizationLog, null);
  }

  public void publishData(UUID sessionId, MessageSubject subject, Map<String, Object> metadata, Map<String, Object> data) {
    checkForNull(sessionId, subject);

    if (data == null) {
      log.error("Unable to publish data due to null parameters");
      return;
    }

    this.sendMessage(sessionId, subject, null, metadata, data);
  }

  public void publishData(UUID sessionId, MessageSubject subject, Map<String, Object> metadata, List<Map<String, Object>> data) {
    checkForNull(sessionId, subject);

    if (data == null) {
      log.error("Unable to publish data due to null parameters");
      return;
    }

    this.sendMessage(sessionId, subject, null, metadata, data);
  }

  private void sendMessage(UUID sessionId, MessageSubject subject, String message, Object data) {
    checkForNull(sessionId, subject);
    this.sendMessage(sessionId, subject, message, null, data);
  }

  private void sendMessage(UUID sessionId, MessageSubject subject, String message, Object metadata, Object data) {
    checkForNull(sessionId, subject);

    Message msg = new Message(sessionId, subject, message, metadata ,data);
    try {
      messagingTemplate.convertAndSend("/topic/" + sessionId, msg);
    } catch (Exception e) {
      log.error("Unable to send message", e);
    }
  }

  private void checkForNull(Object... objects) {
    for (Object object : objects) {
      if (object == null) {
        throw new IllegalArgumentException("Null argument provided");
      }
    }
  }
}