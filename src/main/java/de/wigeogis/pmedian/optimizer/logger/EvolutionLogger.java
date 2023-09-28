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
public class EvolutionLogger implements IslandEvolutionObserver<List<BasicGenome>> {

  private int stagnationCounter = 0;
  private final double INIT_MUTATION_RATE = 0.005;
  private double currentMutationRate = INIT_MUTATION_RATE;
  private double lastBestFitness = Double.POSITIVE_INFINITY;

  private final UUID sessionId;
  private final Integer optimizationPhase;
  private final List<AllocationDto> allocations;
  private final ApplicationEventPublisher publisher;
  private final NotificationService notificationService;
  private final ImmutableTable<String, String, Double> costMatrix;
  private final Map<Integer, Double> progress = new HashMap<>();
  private final Map<String, Object> generationFitnessProgress = new HashMap<>();

  public EvolutionLogger(
      UUID sessionId,
      List<AllocationDto> allocations,
      ImmutableTable<String, String, Double> costMatrix,
      ApplicationEventPublisher publisher,
      NotificationService notificationService,
      Integer optimizationPhase) {
    this.sessionId = sessionId;
    this.allocations = allocations;
    this.costMatrix = costMatrix;
    this.publisher = publisher;
    this.notificationService = notificationService;
    this.optimizationPhase = optimizationPhase;
  }

  public void populationUpdate(PopulationData<? extends List<BasicGenome>> data) {

    double currentBestFitness = data.getMeanFitness();

    double MIN_IMPROVEMENT = 1.0;
    if (lastBestFitness - currentBestFitness >= MIN_IMPROVEMENT) {
      stagnationCounter = 0;
    } else {
      stagnationCounter++; // Reset if there's sufficient decrease
    }

    int STAGNATION_THRESHOLD = 100;
    if (currentMutationRate >= 0.09) {
      currentMutationRate -= 0.005;
      stagnationCounter = 0;
    } else if (stagnationCounter >= STAGNATION_THRESHOLD) {
      currentMutationRate += 0.001; // Increase mutation rate to escape potential local optima
    } else {
      currentMutationRate -= 0.001;
    }

    // Ensure mutation rate is within bounds
    double MIN_MUTATION_RATE = 0.005;
    double MAX_MUTATION_RATE = 0.1;
    currentMutationRate =
        Math.max(MIN_MUTATION_RATE, Math.min(MAX_MUTATION_RATE, currentMutationRate));

    lastBestFitness = currentBestFitness;

    progress.put(data.getGenerationNumber(), data.getBestCandidateFitness());

    if (data.getGenerationNumber() % 5 == 0) {
      publishTravelCostDistribution(data.getBestCandidate());
      publishFitnessProgress(
          data.getGenerationNumber(), data.getBestCandidateFitness(), optimizationPhase);
    }
    if (data.getGenerationNumber() % 50 == 0) {
      publishMutationRate(currentMutationRate);
      writeLogs(data);
    }
  }

  private void publishMutationRate(double mutationRate) {
    MutationRateEvent event = new MutationRateEvent(this, mutationRate, sessionId);
    publisher.publishEvent(event);
  }

  private void writeLogs(
      PopulationData<? extends List<BasicGenome>> data,
      int numberOfFacilities,
      double currentMutationRate) {

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
      PopulationData<? extends List<BasicGenome>> data, Integer optimizationPhase) {
    String logMessage = prepareLogMessage(data);
    notificationService.publishLog(sessionId, MessageSubject.SESSION_LOG, logMessage);
  }

  private void writeLogs(PopulationData<? extends List<BasicGenome>> data) {
    String logMessage = prepareLogMessage(data);
    log.info(logMessage);
  }

  private String prepareLogMessage(PopulationData<? extends List<BasicGenome>> data) {
    int numberOfFacilities = calculateNumberOfFacilities(data.getBestCandidate());
    return String.format(
        "Generation %d [Mutation Rate: %.3f]: (%s facilities) -> %.2f",
        data.getGenerationNumber(),
        currentMutationRate,
        numberOfFacilities,
        data.getBestCandidateFitness());
  }

  private int calculateNumberOfFacilities(List<BasicGenome> bestCandidate) {
    return bestCandidate.stream().collect(Collectors.groupingBy(BasicGenome::getRegionId)).size();
  }

  private void publishFitnessProgress(int generation, double fitness, Integer optimizationPhase) {

    MessageSubject subject =
        switch (optimizationPhase) {
          case 1 -> MessageSubject.SESSION_LOCATION_FITNESS_DATA;
          case 2 -> MessageSubject.SESSION_ALLOCATION_FITNESS_DATA;
          default -> throw new IllegalStateException("Unexpected value: " + optimizationPhase);
        };

    Map<String, Object> metadata =
        getChartMetadata(
            0,
            10000,
            "Number of Iteration",
            0,
            progress.get(0) * 1.5,
            "Fitness Score",
            "Fitness");
    Map<String, Object> data = getFitnessData(generation, fitness);
    notificationService.publishData(this.sessionId, subject, metadata, data);
  }

  private void publishTravelCostDistribution(List<BasicGenome> bestCandidate) {

    List<RegionDto> facilitiesCodes =
        bestCandidate.stream().map(BasicGenome::getRegionDto).toList();

    List<AllocationDto> allocationsUpdated =
        FacilityCandidateUtil.findNearestFacilitiesForDemands(
            allocations, facilitiesCodes, costMatrix);

    Map<String, Object> metadata =
        getChartMetadata(
            0,
            60,
            "Travel-time to Nearest Facility",
            0,
            allocations.size(),
            "Cumulative Number of Regions",
            "Region Count");
    List<Map<String, Object>> data = getTravelCostDistributionData(allocationsUpdated);

    notificationService.publishData(
        this.sessionId, MessageSubject.SESSION_ALLOCATION_TRAVEL_COST_DISTRIBUTION, metadata, data);
  }

  private Map<String, Object> getFitnessData(int generation, double fitness) {
    return getChartData(generation, fitness);
  }

  private List<Map<String, Object>> getTravelCostDistributionData(List<AllocationDto> allocations) {

    int[] intervals = {0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60};

    List<Map<String, Object>> distribution = new ArrayList<>();

    for (int i = 0; i < intervals.length; i++) {
      int lowerBound = (i == 0) ? 0 : intervals[i - 1];
      int upperBound = intervals[i];

      long count =
          allocations.stream()
              .filter(
                  genome ->
                      genome.getTravelCost() > lowerBound && genome.getTravelCost() <= upperBound)
              .count();
      distribution.add(getChartData(upperBound, count));
    }

    return distribution;
  }

  private Map<String, Object> getChartData(Object x, Object y) {
    return Map.of(
        "x", x,
        "y", y);
  }

  private Map<String, Object> getChartMetadata(
      Object xMin,
      Object xMax,
      String xAxisTitle,
      Object yMin,
      Object yMax,
      String yAxisTitle,
      String yLabel) {
    return Map.of(
        "xMin", xMin,
        "xMax", xMax,
        "xAxisTitle", xAxisTitle,
        "yMin", yMin,
        "yMax", yMax,
        "yAxisTitle", yAxisTitle,
        "yLabel", yLabel);
  }

  public Map<Integer, Double> getProgress() {
    return new TreeMap<>(progress);
  }

  @Override
  public void islandPopulationUpdate(
      int i, PopulationData<? extends List<BasicGenome>> populationData) {}
}
