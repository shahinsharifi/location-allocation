package de.wigeogis.pmedian.optimizer.logger;

import de.wigeogis.pmedian.optimizer.model.BasicGenome;
import de.wigeogis.pmedian.optimizer.util.CostEvaluatorUtils;
import de.wigeogis.pmedian.websocket.MessageSubject;
import de.wigeogis.pmedian.websocket.NotificationService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationEventPublisher;
import org.uncommons.watchmaker.framework.EvolutionObserver;
import org.uncommons.watchmaker.framework.PopulationData;

@Log4j2
public class EvolutionLogger implements EvolutionObserver<List<BasicGenome>> {

  private int stagnationCounter = 0;
  private final double INIT_MUTATION_RATE = 0.005;
  private double currentMutationRate = INIT_MUTATION_RATE;
  private double lastBestFitness = Double.POSITIVE_INFINITY;

  private final UUID sessionId;
  private final CostEvaluatorUtils costEvaluatorUtils;
  private final ApplicationEventPublisher eventPublisher;
  private final NotificationService notificationService;
  private final ConcurrentHashMap<Integer, Double> progress;

  public EvolutionLogger(
      UUID sessionId,
      CostEvaluatorUtils costEvaluatorUtils,
      ApplicationEventPublisher publisher,
      NotificationService notificationService,
      ConcurrentHashMap<Integer, Double> progress) {
    this.sessionId = sessionId;
    this.costEvaluatorUtils = costEvaluatorUtils;
    this.eventPublisher = publisher;
    this.notificationService = notificationService;
    this.progress = progress;
  }

  public void populationUpdate(PopulationData<? extends List<BasicGenome>> data) {
    List<BasicGenome> bestCandidate = data.getBestCandidate();
    List<String> facilities = bestCandidate.stream().map(BasicGenome::getRegionId).toList();
    int generation = data.getGenerationNumber();
    double bestFitnessScore = data.getBestCandidateFitness();

    double MIN_IMPROVEMENT = 1.0;
    if (lastBestFitness - bestFitnessScore >= MIN_IMPROVEMENT) {
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

    double MIN_MUTATION_RATE = 0.005;
    double MAX_MUTATION_RATE = 0.1;
    currentMutationRate =
        Math.max(MIN_MUTATION_RATE, Math.min(MAX_MUTATION_RATE, currentMutationRate));

    lastBestFitness = bestFitnessScore;

    List<Double> costList = this.costEvaluatorUtils.calculateCostMap(facilities);
    progress.put(generation, costList.stream().mapToDouble(Double::doubleValue).sum());

    if (data.getGenerationNumber() % costEvaluatorUtils.getTravelCostDistributionUpdateInterval()
        == 0) {
      publishTravelCostDistribution(costList);
    }

    if (data.getGenerationNumber() % costEvaluatorUtils.getFitnessProgressUpdateInterval() == 0) {
      Map<Integer, Double> sampleProgress = getSampleMap(progress);
      publishFitnessProgress(sampleProgress);
    }

    if (data.getGenerationNumber() % 50 == 0) {
      publishMutationRate(currentMutationRate);
      writeLogs(data);
    }
  }

  private void publishMutationRate(double mutationRate) {
    MutationRateEvent event = new MutationRateEvent(this, mutationRate, sessionId);
    eventPublisher.publishEvent(event);
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

  private void publishFitnessProgress(Map<Integer, Double> sampleProgress) {

    Map<String, Object> metadata =
        getChartMetadata(
            0,
            10000,
            "Iteration",
            0,
            progress.get(0) + (progress.get(0) * 0.01),
            "Total Travel Cost Per Iteration",
            "Travel Time (minutes)");

    notificationService.publishData(
        this.sessionId,
        MessageSubject.SESSION_ALLOCATION_TRAVEL_COST_TOTAL_PROGRESS,
        metadata,
        getChartData(sampleProgress));
  }

  private void publishTravelCostDistribution(List<Double> costList) {

    List<Map<String, Object>> data = getTravelCostDistributionData(costList);

    // getting the maximum value of the y-axis from data
    double maxValue =
        data.stream()
            .map(map -> map.get("y"))
            .mapToDouble(value -> Double.parseDouble(value.toString()) + 200)
            .max()
            .orElse(1000);

    Map<String, Object> metadata =
        getChartMetadata(
            0,
            60,
            "Travel Time to Nearest Facility",
            0,
            maxValue,
            "Number of Regions Per Travel Time",
            "Cumulative Number of Regions");

    notificationService.publishData(
        this.sessionId,
        MessageSubject.SESSION_ALLOCATION_TRAVEL_COST_DISTRIBUTION_PROGRESS,
        metadata,
        data);
  }

  private Map<Integer, Double> getSampleMap(Map<Integer, Double> progress) {
    if (progress == null || progress.isEmpty()) {
      return Collections.emptyMap();
    }

    int size = progress.size();
    int interval = size > 100 ? 100 : 10;

    List<Map.Entry<Integer, Double>> entryList = new ArrayList<>(progress.entrySet());
    return IntStream.range(0, size)
        .filter(i -> i % interval == 0)
        .boxed()
        .limit(20)
        .collect(
            Collectors.toMap(
                i -> entryList.get(i).getKey(),
                i -> entryList.get(i).getValue(),
                (a, b) -> a,
                LinkedHashMap::new));
  }

  private List<Map<String, Object>> getTravelCostDistributionData(List<Double> travelCosts) {

    int[] intervals = {0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60};

    List<Map<String, Object>> distribution = new ArrayList<>();

    for (int i = 0; i < intervals.length; i++) {
      int lowerBound = (i == 0) ? 0 : intervals[i - 1];
      int upperBound = intervals[i];

      long count =
          travelCosts.stream().filter(cost -> cost > lowerBound && cost <= upperBound).count();

      distribution.add(getChartData(upperBound, count));
    }

    return distribution;
  }

  private List<Map<String, Object>> getChartData(Map<Integer, Double> progress) {
    return progress.keySet().stream()
        .map(generation -> getChartData(generation, progress.get(generation)))
        .toList();
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
}
