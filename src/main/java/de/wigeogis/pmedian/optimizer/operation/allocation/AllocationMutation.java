package de.wigeogis.pmedian.optimizer.operation.allocation;

import com.google.common.collect.ImmutableTable;
import de.wigeogis.pmedian.database.dto.RegionDto;
import de.wigeogis.pmedian.optimizer.logger.MutationRateEvent;
import de.wigeogis.pmedian.optimizer.model.BasicGenome;
import de.wigeogis.pmedian.optimizer.util.CostEvaluatorUtils;
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
  private final CostEvaluatorUtils costEvaluatorUtils;
  private Double mutationRate = 0.001;
  private NumberGenerator<Probability> mutationProbability =
      new AdjustableNumberGenerator<>(new Probability(mutationRate));

  @Override
  public List<BasicGenome> apply(List<BasicGenome> chromosome, Random random) {
    for (BasicGenome genome : chromosome) {
      if (mutationProbability.nextValue().nextEvent(random)) {
        double uncoveredBefore = calculateCoverage(chromosome);
        double beforeFitness = calculateStandardDeviation(chromosome);
        String beforeFid = genome.getRegionId();
        String afterFid = mutateGenome(chromosome, genome, random).getRegionId();
        genome.setRegionId(afterFid);
        double uncoveredAfter = calculateCoverage(chromosome);
        double afterFitness = calculateStandardDeviation(chromosome);
        if (uncoveredAfter > uncoveredBefore || afterFitness > beforeFitness) genome.setRegionId(beforeFid);
      }
    }
    return chromosome;
  }

  private BasicGenome mutateGenome(List<BasicGenome> chromosome, BasicGenome genome, Random rng) {
    int randomNumber = rng.nextInt(demands.size());
    String candidate = demands.get(randomNumber).getId();
    BasicGenome testGenome = new BasicGenome(candidate);
    if (!chromosome.contains(testGenome)) genome.setRegionId(candidate);
    return genome;
  }

  private Double calculateTotalCost(List<BasicGenome> chromosome) {
    List<String> facilities = chromosome.stream().map(BasicGenome::getRegionId).toList();
    return this.costEvaluatorUtils.calculateTotalCost(facilities);
  }

  private Double calculateStandardDeviation(List<BasicGenome> chromosome) {
    List<String> facilities = chromosome.stream().map(BasicGenome::getRegionId).toList();
    return this.costEvaluatorUtils.calculateStandardDeviation(facilities);
  }

  private int calculateCoverage(List<BasicGenome> chromosome) {
    List<String> facilities = chromosome.stream().map(BasicGenome::getRegionId).toList();
    return this.costEvaluatorUtils.calculateUncoveredAndAboveLimitRegions(facilities);
  }

  //  private Double calculateTotalCost(List<BasicGenome> chromosome) {
  //    List<RegionDto> facilities = chromosome.stream().map(BasicGenome::getRegionDto).toList();
  //    Map<RegionDto, RegionDto> allocated =
  //        FacilityCandidateUtil.findNearestFacilities(demands, facilities, this.dMatrix);
  //    return allocated.entrySet().stream()
  //        .mapToDouble(
  //            entry -> dMatrix.get(entry.getKey().getId(), entry.getValue().getId()))
  //        .sum();
  //  }

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
