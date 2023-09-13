package de.wigeogis.pmedian.optimization.logger;

import org.springframework.context.ApplicationEvent;

public class MutationRateEvent extends ApplicationEvent {
  private final double mutationRate;

  public MutationRateEvent(Object source, double mutationRate) {
    super(source);
    this.mutationRate = mutationRate;
  }

  public double getMutationRate() {
    return mutationRate;
  }
}
