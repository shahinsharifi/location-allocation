package de.wigeogis.pmedian.optimizer.logger;

import de.wigeogis.pmedian.database.dto.SessionDto;
import de.wigeogis.pmedian.database.entity.Session;
import de.wigeogis.pmedian.optimizer.model.BasicGenome;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.uncommons.watchmaker.framework.PopulationData;
import org.uncommons.watchmaker.framework.islands.IslandEvolutionObserver;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Log4j2
public class EvolutionLogger<T extends BasicGenome> implements IslandEvolutionObserver<T> {

  private final double MIN_MUTATION_RATE = 0.005;
  private final double MAX_MUTATION_RATE = 0.1; // Adjust as needed
  private final double INIT_MUTATION_RATE = 0.005;

  private final int STAGNATION_THRESHOLD =
      100; // Number of generations with minimal decrease before adapting mutation rate
  private final double MIN_IMPROVEMENT = 1.0; // Minimal decrease to reset stagnation counter

  private double lastBestFitness =
      Double.POSITIVE_INFINITY; // For minimization, start with a very high value
  private int stagnationCounter = 0;
  private double currentMutationRate = INIT_MUTATION_RATE;

  private Map<Integer, Double> progress = new HashMap<>();


  private final UUID sessionId;
  private final ApplicationEventPublisher publisher;

  public EvolutionLogger(UUID sessionId, ApplicationEventPublisher publisher) {
    this.sessionId = sessionId;
    this.publisher = publisher;
  }


  public void populationUpdate(PopulationData<? extends T> data) {

    double currentBestFitness = data.getMeanFitness();

    if (lastBestFitness - currentBestFitness >= MIN_IMPROVEMENT) {
      stagnationCounter = 0;
    } else {
      stagnationCounter++; // Reset if there's sufficient decrease
    }

    if (currentMutationRate >= 0.09) {
      currentMutationRate -= 0.005;
      stagnationCounter = 0;
    } else if (stagnationCounter >= STAGNATION_THRESHOLD) {
      currentMutationRate += 0.001; // Increase mutation rate to escape potential local optima
    }else{
      currentMutationRate -= 0.001;
    }

    // Ensure mutation rate is within bounds
    currentMutationRate =
        Math.max(MIN_MUTATION_RATE, Math.min(MAX_MUTATION_RATE, currentMutationRate));

    lastBestFitness = currentBestFitness;

    if (data.getGenerationNumber() % 100 == 0) {
      writeProgress(data, currentMutationRate);
      publishMutationRate(currentMutationRate);
    }
  }

  private void publishMutationRate(double mutationRate) {
    MutationRateEvent event = new MutationRateEvent(this, mutationRate, sessionId);
    publisher.publishEvent(event);
  }

  public void islandPopulationUpdate(int islandIndex, PopulationData<? extends T> populationData) {
    // Do nothing.
  }

  public void writeProgress(PopulationData<? extends T> data, double currentMutationRate) {
    progress.put(data.getGenerationNumber(), data.getBestCandidateFitness());
    List<BasicGenome> bestCandidate = (List<BasicGenome>) data.getBestCandidate();
    int numberOfFacilities =
        bestCandidate.stream().collect(Collectors.groupingBy(BasicGenome::getRegionId)).size();
    log.info(
        "Generation "
            + data.getGenerationNumber()
            + " [\u001B[33mMutation Rate: "
            + String.format("%.3f", currentMutationRate)
            + "\u001B[0m]: "
            + "("
            + numberOfFacilities
            + " facilities) -> "
            + data.getBestCandidateFitness());
  }

  public Map<Integer, Double> getProgress() {
    return new TreeMap<>(progress);
  }
}
