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
import org.apache.commons.math3.stat.Frequency;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
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
      //     publishOverallStandardDeviation(bestCandidate);
      //publishLogs(data, numberOfFacilities, currentMutationRate);
      publishProgress(data.getGenerationNumber(), data.getBestCandidateFitness());
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
        MessageSubject.SESSION_PROGRESS_DATA,
        Map.of("generation", generation, "value", fitness));
  }

  private void publishOverallStandardDeviation(List<BasicGenome> bestCandidate) {

    List<RegionDto> facilitiesCodes =
        bestCandidate.stream().map(BasicGenome::getRegionDto).toList();

    List<AllocationDto> allocations =
        FacilityCandidateUtil.findNearestFacilitiesForDemands(
            allocationDtos, facilitiesCodes, costMatrix);

    Map<String, Object> overallStandardDeviationOfTravelTime = new HashMap<>();
    DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics();
    Frequency frequency = new Frequency(); // Create a Frequency instance

    for (AllocationDto allocation : allocations) {

      descriptiveStatistics.addValue(allocation.getTravelCost()); // the weight return travel time
      frequency.addValue(
          allocation.getTravelCost()); // Add the weight into the frequency for histogram
    }

    // Generating the histogram. We know that our range is 0 to 60, and interval is 5.
    for (int i = 0; i <= 60; i += 5) {
      // creates a key in the format "0-5", "5-10", "10-15" etc.
      String key = i + "-" + (i + 5);
      // counts the weight that is in the interval i and i+5
      long count = frequency.getCount(i / (i + 5));
      // put this into your map
      overallStandardDeviationOfTravelTime.put(key, count);
    }

    List<Map<String, Object>> result = new ArrayList<>();


    for(String key : overallStandardDeviationOfTravelTime.keySet()) {
      result.add(Map.of("generation", key, "value", overallStandardDeviationOfTravelTime.get(key)));
    }

    notificationService.publishData(
        this.sessionId, MessageSubject.SESSION_PROGRESS_DATA, result);
  }

  public Map<Integer, Double> getProgress() {
    return new TreeMap<>(progress);
  }
}
