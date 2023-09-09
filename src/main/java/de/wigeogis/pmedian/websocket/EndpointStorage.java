package de.wigeogis.pmedian.websocket;


import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class EndpointStorage {

    private List<String> sessionTransactionMap = new ArrayList<>();

    public void addSession(String sessionId) {
        this.sessionTransactionMap.add(sessionId);
    }

    public void removeSession(String sessionId) {
        this.sessionTransactionMap.remove(sessionId);
    }

}