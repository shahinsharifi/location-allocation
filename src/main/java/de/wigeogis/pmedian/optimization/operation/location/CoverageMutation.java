package de.wigeogis.pmedian.optimization.operation.location;

import de.wigeogis.pmedian.optimization.model.BasicGenome;
import de.wigeogis.pmedian.optimization.utils.LocationUtils;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.uncommons.maths.number.AdjustableNumberGenerator;
import org.uncommons.maths.number.NumberGenerator;
import org.uncommons.maths.random.Probability;
import org.uncommons.watchmaker.framework.EvolutionaryOperator;

@Log4j2
@AllArgsConstructor
public class CoverageMutation implements EvolutionaryOperator<BasicGenome> {

  private Double mutationRate;
  private final List<String> demands;
  private final ConcurrentHashMap<String, ConcurrentHashMap<String, Double>> costMatrix;


  @Override
  public List<BasicGenome> apply(List<BasicGenome> chromosome, Random random) {
    NumberGenerator<Probability> mutationProbability = new AdjustableNumberGenerator<>(new Probability(mutationRate));
    List<String> facilities = chromosome.stream().map(BasicGenome::getRegionId).toList();
    for (BasicGenome genome : chromosome) {
      if (mutationProbability.nextValue().nextEvent(random)) {
        double uncoveredBefore = LocationUtils.calculateUncoveredRegions(facilities, demands, costMatrix);
        String beforeFid = genome.getRegionId();
        String afterFid = mutateGenome(chromosome, genome, random).getRegionId();
        genome.setRegionId(afterFid);
        double uncoveredAfter = LocationUtils.calculateUncoveredRegions(facilities, demands, costMatrix);
        if (uncoveredAfter > uncoveredBefore) genome.setRegionId(beforeFid);
      }
    }
    return chromosome;
  }

  private BasicGenome mutateGenome(List<BasicGenome> chromosome, BasicGenome genome, Random rng) {

    List<String> reachableRegionCodes = costMatrix.get(genome.getRegionId()).keySet().stream().toList();
    //String nearestCandidate = reachableRegion.entrySet().stream().min(Map.Entry.comparingByValue()).get().getKey();
    List<String> top10NearestCandidate = costMatrix.get(genome.getRegionId())
        .entrySet()
        .stream()
        .sorted(Map.Entry.comparingByValue())
        .limit(10)
        .map(Map.Entry::getKey)
        .toList();
    int randomNumber = rng.nextInt(top10NearestCandidate.size());
    String candidate = top10NearestCandidate.get(randomNumber);

    List<String> newReachableRegionCodes = costMatrix.get(genome.getRegionId()).keySet().stream().toList();

    BasicGenome testGenome = new BasicGenome(candidate);
    if (!chromosome.contains(testGenome) && newReachableRegionCodes.size() >= reachableRegionCodes.size()) genome.setRegionId(candidate);

    return genome;
  }



//  @EventListener
//  public void handleMutationRateEvent(MutationRateEvent event) {
//    // Handle the mutation rate update
//    double newMutationRate = event.getMutationRate();
//    if(newMutationRate != this.mutationRate) {
//      this.mutationRate = newMutationRate;
//      this.setMutationProbability(newMutationRate);
//    }
//  }
}
