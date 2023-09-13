package de.wigeogis.pmedian.optimization.logger;

import de.wigeogis.pmedian.optimization.model.BasicGenome;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.uncommons.watchmaker.framework.PopulationData;
import org.uncommons.watchmaker.framework.islands.IslandEvolutionObserver;

@Log4j2
public class EvolutionObserver<T extends BasicGenome> implements IslandEvolutionObserver<T> {


  private final double INIT_MUTATION_RATE = 0.005;
  private final Map<Integer, Double> progress = new HashMap<>();
  private int stagnationCounter = 0;
  private Double globalMutationRate;
  private double currentMutationRate = INIT_MUTATION_RATE;
  private double lastBestFitness = Double.POSITIVE_INFINITY;


  public EvolutionObserver(Double globalMutationRate) {
    this.globalMutationRate = globalMutationRate;
  }


  public void populationUpdate(PopulationData<? extends T> data) {

    double currentBestFitness = data.getMeanFitness();

    // Minimal decrease to reset stagnation counter
    double MIN_IMPROVEMENT = 1.0;
    if (lastBestFitness - currentBestFitness >= MIN_IMPROVEMENT) {
      stagnationCounter = 0;
    } else {
      stagnationCounter++; // Reset if there's sufficient decrease
    }

    // Number of generations with minimal decrease before adapting mutation rate
    int STAGNATION_THRESHOLD = 100;
    if (currentMutationRate >= 0.09) {
      currentMutationRate -= 0.005;
      stagnationCounter = 0;
    } else if (stagnationCounter >= STAGNATION_THRESHOLD) {
      currentMutationRate += 0.001; // Increase mutation rate to escape potential local optima
    }else{
      currentMutationRate -= 0.001;
    }

    // Ensure mutation rate is within bounds
    // Adjust as needed
    double MAX_MUTATION_RATE = 0.1;
    double MIN_MUTATION_RATE = 0.005;
    currentMutationRate =
        Math.max(MIN_MUTATION_RATE, Math.min(MAX_MUTATION_RATE, currentMutationRate));

    lastBestFitness = currentBestFitness;

    if (data.getGenerationNumber() % 100 == 0) {
      writeProgress(data, currentMutationRate);
      publishMutationRate(currentMutationRate);
    }
  }

  private void publishMutationRate(double mutationRate) {
    globalMutationRate = mutationRate;
    //MutationRateEvent event = new MutationRateEvent(this, mutationRate);
    //publisher.publishEvent(event);
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

  public ConcurrentMap<Integer, Double> getProgress() {
    return new ConcurrentHashMap<>(progress);
  }
}
