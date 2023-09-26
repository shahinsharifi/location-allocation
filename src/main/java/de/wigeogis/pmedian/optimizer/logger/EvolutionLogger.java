package de.wigeogis.pmedian.optimizer.logger;

import com.google.common.collect.ImmutableTable;
import de.wigeogis.pmedian.database.dto.AllocationDto;
import de.wigeogis.pmedian.database.dto.RegionDto;
import de.wigeogis.pmedian.optimizer.model.BasicGenome;
import de.wigeogis.pmedian.optimizer.util.FacilityCandidateUtil;
import de.wigeogis.pmedian.websocket.MessageSubject;
import de.wigeogis.pmedian.websocket.NotificationService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationEventPublisher;
import org.uncommons.watchmaker.framework.PopulationData;
import org.uncommons.watchmaker.framework.islands.IslandEvolutionObserver;

@Log4j2
public class EvolutionLogger<T extends BasicGenome> implements IslandEvolutionObserver<T> {

  private final double MIN_MUTATION_RATE = 0.005;
  private final double MAX_MUTATION_RATE = 0.1; // Adjust as needed
  private final double INIT_MUTATION_RATE = 0.005;

  private final int STAGNATION_THRESHOLD =
      100; // Number of generations with minimal decrease before adapting mutation rate
  private final double MIN_IMPROVEMENT = 1.0; // Minimal decrease to reset stagnation counter
  private final UUID sessionId;
  private final ApplicationEventPublisher publisher;
  private final NotificationService notificationService;
  private double lastBestFitness =
      Double.POSITIVE_INFINITY; // For minimization, start with a very high value
  private int stagnationCounter = 0;
  private double currentMutationRate = INIT_MUTATION_RATE;

  private final List<AllocationDto> allocationDtos;
  private final ImmutableTable<String, String, Double> costMatrix;

  private final Map<Integer, Double> progress = new HashMap<>();
  private final Map<String, Object> generationFitnessProgress = new HashMap<>();

  public EvolutionLogger(
      UUID sessionId,
      List<AllocationDto> allocationDtos,
      ImmutableTable<String, String, Double> costMatrix,
      ApplicationEventPublisher publisher,
      NotificationService notificationService) {
    this.sessionId = sessionId;
    this.allocationDtos = allocationDtos;
    this.costMatrix = costMatrix;
    this.publisher = publisher;
    this.notificationService = notificationService;
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
    } else {
      currentMutationRate -= 0.001;
    }

    // Ensure mutation rate is within bounds
    currentMutationRate =
        Math.max(MIN_MUTATION_RATE, Math.min(MAX_MUTATION_RATE, currentMutationRate));

    lastBestFitness = currentBestFitness;

    progress.put(data.getGenerationNumber(), data.getBestCandidateFitness());
    // generationFitnessProgress.put(String.valueOf(data.getGenerationNumber()),
    // data.getBestCandidateFitness());
    List<BasicGenome> bestCandidate = (List<BasicGenome>) data.getBestCandidate();
    int numberOfFacilities =
        bestCandidate.stream().collect(Collectors.groupingBy(BasicGenome::getRegionId)).size();

    if (data.getGenerationNumber() % 10 == 0) {
      //publishLogs(data, numberOfFacilities, currentMutationRate);
      publishProgress(data.getGenerationNumber(), data.getBestCandidateFitness());
    }
    if (data.getGenerationNumber() % 20 == 0) {
      publishOverallStandardDeviation(bestCandidate);
    }
    if (data.getGenerationNumber() % 50 == 0) {
      writeLogs(data, numberOfFacilities, currentMutationRate);
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

  private void writeLogs(
      PopulationData<? extends T> data, int numberOfFacilities, double currentMutationRate) {

    String logMessage =
        ("Generation "
            + data.getGenerationNumber()
            + " [\u001B[33mMutation Rate: "
            + String.format("%.3f", currentMutationRate)
            + "\u001B[0m]: "
            + "("
            + numberOfFacilities
            + " facilities) -> "
            + data.getBestCandidateFitness());

    log.info(logMessage);
  }

  private void publishLogs(
      PopulationData<? extends T> data, int numberOfFacilities, double currentMutationRate) {

    String logMessage =
        "Generation "
            + data.getGenerationNumber()
            + " [Mutation Rate: "
            + String.format("%.3f", currentMutationRate)
            + "]: "
            + "("
            + numberOfFacilities
            + " facilities) -> "
            + data.getBestCandidateFitness();
    notificationService.publishLog(sessionId, MessageSubject.SESSION_LOG, logMessage);
  }

  private void publishProgress(int generation, double fitness) {
    notificationService.publishData(
        this.sessionId,
        MessageSubject.SESSION_ALLOCATION_FITNESS_DATA,
        Map.of("generation", generation, "value", fitness));
  }

  private void publishOverallStandardDeviation(List<BasicGenome> bestCandidate) {

    List<RegionDto> facilitiesCodes =
        bestCandidate.stream().map(BasicGenome::getRegionDto).toList();

    List<AllocationDto> allocations =
        FacilityCandidateUtil.findNearestFacilitiesForDemands(
            allocationDtos, facilitiesCodes, costMatrix);

    List<Map<String,Object>> result = this.travelCostDistribution(allocations);

    notificationService.publishData(
        this.sessionId, MessageSubject.SESSION_ALLOCATION_TRAVEL_COST_DISTRIBUTION, result);
  }

  private List<Map<String, Object>> travelCostDistribution(List<AllocationDto> allocations) {
    List<Map<String, Object>> distribution = new ArrayList<>();

    // Define the intervals
    int[] intervals = {0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60};

    for (int i = 0; i < intervals.length; i++) {
      int lowerBound = (i == 0) ? 0 : intervals[i - 1];
      int upperBound = intervals[i];

      long count = allocations.stream()
          .filter(genome -> genome.getTravelCost() > lowerBound && genome.getTravelCost() <= upperBound)
          .count();

      Map<String, Object> map = new HashMap<>();
      map.put("label", upperBound);
      map.put("value", count);
      distribution.add(map);
    }

    return distribution;
  }

  public Map<Integer, Double> getProgress() {
    return new TreeMap<>(progress);
  }
}
