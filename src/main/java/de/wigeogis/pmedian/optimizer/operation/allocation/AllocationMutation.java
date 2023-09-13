package de.wigeogis.pmedian.optimizer.operation.allocation;

import com.google.common.collect.ImmutableTable;
import de.wigeogis.pmedian.database.dto.RegionDto;
import de.wigeogis.pmedian.optimizer.logger.MutationRateEvent;
import de.wigeogis.pmedian.optimizer.model.BasicGenome;
import de.wigeogis.pmedian.optimizer.util.FacilityCandidateUtil;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.event.EventListener;
import org.uncommons.maths.number.AdjustableNumberGenerator;
import org.uncommons.maths.number.NumberGenerator;
import org.uncommons.maths.random.Probability;
import org.uncommons.watchmaker.framework.EvolutionaryOperator;

@Log4j2
@RequiredArgsConstructor
public class AllocationMutation implements EvolutionaryOperator<BasicGenome> {

  private final UUID sessionId;
  private final List<RegionDto> demands;
  private final ImmutableTable<String, String, Double> dMatrix;
  private Double mutationRate = 0.001;
  private NumberGenerator<Probability> mutationProbability =
      new AdjustableNumberGenerator<>(new Probability(mutationRate));

  @Override
  public List<BasicGenome> apply(List<BasicGenome> chromosome, Random random) {
    for (BasicGenome genome : chromosome) {
      if (mutationProbability.nextValue().nextEvent(random)) {
        double beforeCost = calculateTotalCost(chromosome);
        String beforeFid = genome.getRegionId();

        String afterFid = mutateGenome(chromosome, genome, random).getRegionId();
        genome.setRegionId(afterFid);
        double afterCost = calculateTotalCost(chromosome);

        if (afterCost > beforeCost) genome.setRegionId(beforeFid);
      }
    }

    return chromosome;
  }

  private BasicGenome mutateGenome(List<BasicGenome> chromosome, BasicGenome genome, Random rng) {

    List<String> reachableRegionCodes =
        dMatrix.row(genome.getRegionId()).keySet().stream().toList();
    //    int randomNumber = rng.nextInt(reachableRegion.size());
    //    String candidate = reachableRegion.get(randomNumber);
    //    BasicGenome testGenome = new BasicGenome(candidate);
    //    if (!chromosome.contains(testGenome)) gene.setRegionId(candidate);

    List<String> top10NearestCandidate =
        dMatrix.row(genome.getRegionId()).entrySet().stream()
            .sorted(Map.Entry.comparingByValue())
            .limit(10)
            .map(Map.Entry::getKey)
            .toList();
    int randomNumber = rng.nextInt(top10NearestCandidate.size());
    String candidate = top10NearestCandidate.get(randomNumber);

    List<String> newReachableRegionCodes =
        dMatrix.row(genome.getRegionId()).keySet().stream().toList();

    BasicGenome testGenome = new BasicGenome(candidate);
    if (!chromosome.contains(testGenome)
        && newReachableRegionCodes.size() >= reachableRegionCodes.size())
      genome.setRegionId(candidate);

    return genome;
  }

  private Double calculateTotalCost(List<BasicGenome> chromosome) {
    List<RegionDto> facilities = chromosome.stream().map(BasicGenome::getRegionDto).toList();
    Map<RegionDto, RegionDto> allocated =
        FacilityCandidateUtil.findNearestFacilities(demands, facilities, this.dMatrix);
    return allocated.entrySet().stream()
        .mapToDouble(
            entry -> dMatrix.get(entry.getKey().getId(), entry.getValue().getId()))
        .sum();
  }

  private void setMutationProbability(double mutationRate) {
    this.mutationProbability = new AdjustableNumberGenerator<>(new Probability(mutationRate));
  }

  @EventListener
  public void updateMutationRateEvent(MutationRateEvent event) {
    if (event.getSessionId() == this.sessionId) {
      double newMutationRate = event.getMutationRate();
      if (newMutationRate != this.mutationRate) {
        this.mutationRate = newMutationRate;
        this.setMutationProbability(newMutationRate);
      }
    }
  }
}
