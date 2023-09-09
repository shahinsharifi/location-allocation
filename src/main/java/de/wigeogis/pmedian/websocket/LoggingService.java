package de.wigeogis.pmedian.websocket;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@AllArgsConstructor
public class LoggingService {

	private SimpMessagingTemplate template;

	public void sendLog(String uuid, String log) {
		this.template.convertAndSend("/topic/logs/" + uuid, log);
	}

}
