package de.wigeogis.pmedian.job;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class JobNotificationService {

  private final Map<String, Set<String>> userUUIDMap = new ConcurrentHashMap<>();
  private SimpMessagingTemplate template;

  public void sendLog(UUID uuid, String log) {
    // Iterate over userUUIDMap and find which users are interested in this UUID
    for (Map.Entry<String, Set<String>> entry : userUUIDMap.entrySet()) {
      if (entry.getValue().contains(uuid)) {
        String user = entry.getKey();
        this.template.convertAndSendToUser(user, "/queue/logs", log);
      }
    }
  }

  // Methods to manage user's UUID interests
  public void addUserUUID(String user, String uuid) {
    userUUIDMap.computeIfAbsent(user, k -> new HashSet<>()).add(uuid);
  }

  public void removeUserUUID(String user, String uuid) {
    Set<String> uuids = userUUIDMap.get(user);
    if (uuids != null) {
      uuids.remove(uuid);
    }
  }
}
