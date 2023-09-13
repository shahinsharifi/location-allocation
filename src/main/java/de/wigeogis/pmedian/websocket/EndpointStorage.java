package de.wigeogis.pmedian.websocket;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EndpointStorage {

  private final List<String> sessionTransactionMap;

  public void addSession(String sessionId) {
    this.sessionTransactionMap.add(sessionId);
  }

  public void removeSession(String sessionId) {
    this.sessionTransactionMap.remove(sessionId);
  }
}
