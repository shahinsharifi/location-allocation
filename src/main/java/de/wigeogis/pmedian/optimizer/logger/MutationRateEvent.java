package de.wigeogis.pmedian.optimizer.logger;

import java.util.UUID;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class MutationRateEvent extends ApplicationEvent {
  private final UUID sessionId;
  private final double mutationRate;

  public MutationRateEvent(Object source, double mutationRate, UUID sessionId) {
    super(source);
    this.mutationRate = mutationRate;
    this.sessionId = sessionId;
  }

}

