package de.wigeogis.pmedian.optimization.operation.allocation;

import de.wigeogis.pmedian.optimization.model.BasicGenome;
import de.wigeogis.pmedian.optimization.utils.AllocationUtils;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.uncommons.maths.number.AdjustableNumberGenerator;
import org.uncommons.maths.number.NumberGenerator;
import org.uncommons.maths.random.Probability;
import org.uncommons.watchmaker.framework.EvolutionaryOperator;

import java.util.List;
import java.util.Random;

@Log4j2
@AllArgsConstructor
public class AllocationMutation implements EvolutionaryOperator<BasicGenome> {

  private Double mutationRate;
  private final List<String> demands;
  private final ConcurrentHashMap<String, ConcurrentHashMap<String, Double>> costMatrix;

  @Override
  public List<BasicGenome> apply(List<BasicGenome> chromosome, Random random) {
    NumberGenerator<Probability> mutationProbability =
        new AdjustableNumberGenerator<>(new Probability(mutationRate));
    for (BasicGenome genome : chromosome) {
      if (mutationProbability.nextValue().nextEvent(random)) {
        double beforeCost =
            AllocationUtils.calculateTotalCost(chromosome, this.demands, this.costMatrix);
        String beforeFid = genome.getRegionId();

        String afterFid = mutateGenome(chromosome, genome, random).getRegionId();
        genome.setRegionId(afterFid);
        double afterCost =
            AllocationUtils.calculateTotalCost(chromosome, this.demands, this.costMatrix);

        if (afterCost > beforeCost) genome.setRegionId(beforeFid);
      }
    }

    return chromosome;
  }

  private BasicGenome mutateGenome(List<BasicGenome> chromosome, BasicGenome genome, Random rng) {

    List<String> reachableRegionCodes =
        costMatrix.get(genome.getRegionId()).keySet().stream().toList();
    List<String> top10NearestCandidate =
        costMatrix.get(genome.getRegionId()).entrySet().stream()
            .sorted(Map.Entry.comparingByValue())
            .limit(10)
            .map(Map.Entry::getKey)
            .toList();
    int randomNumber = rng.nextInt(top10NearestCandidate.size());
    String candidate = top10NearestCandidate.get(randomNumber);
    List<String> newReachableRegionCodes =
        costMatrix.get(genome.getRegionId()).keySet().stream().toList();
    BasicGenome testGenome = new BasicGenome(candidate);
    if (!chromosome.contains(testGenome)
        && newReachableRegionCodes.size() >= reachableRegionCodes.size())
      genome.setRegionId(candidate);
    return genome;
  }
}
