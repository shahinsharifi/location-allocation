package de.wigeogis.pmedian.websocket;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.swing.text.html.Option;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@AllArgsConstructor
public class NotificationService {

  private SimpMessagingTemplate messagingTemplate;

  public void publishLog(UUID sessionId, MessageSubject subject, String log) {
    this.sendMessage(sessionId, MessageType.LOG, subject, log, null);
  }

  public void publishData(
      UUID sessionId, MessageSubject subject, Map<String, Object> data) {
    this.sendMessage(sessionId, MessageType.DATA, subject, null, data);
  }

  public void publishData(
      UUID sessionId, MessageSubject subject, String message, Map<String, Object> data) {
    this.sendMessage(sessionId, MessageType.DATA, subject, message, data);
  }

  private void sendMessage(
      UUID sessionId,
      MessageType type,
      MessageSubject subject,
      String message,
      Map<String, Object> data) {
    Message msg = new Message(sessionId, type, subject, message, data);
    messagingTemplate.convertAndSend("/topic/" + sessionId, msg);
  }
}
