package de.wigeogis.pmedian.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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
