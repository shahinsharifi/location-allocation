package de.wigeogis.pmedian.optimizer.operation.location;

import com.google.common.collect.ImmutableTable;
import de.wigeogis.pmedian.database.dto.RegionDto;
import de.wigeogis.pmedian.optimizer.logger.MutationRateEvent;
import de.wigeogis.pmedian.optimizer.model.BasicGenome;
import de.wigeogis.pmedian.optimizer.util.FacilityCandidateUtil;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.uncommons.maths.number.AdjustableNumberGenerator;
import org.uncommons.maths.number.NumberGenerator;
import org.uncommons.maths.random.Probability;
import org.uncommons.watchmaker.framework.EvolutionaryOperator;

@Log4j2
@RequiredArgsConstructor
public class CoverageMutation implements EvolutionaryOperator<BasicGenome> {

  private Double mutationRate = 0.001;
  private NumberGenerator<Probability> mutationProbability =
      new AdjustableNumberGenerator<>(new Probability(mutationRate));
  private final UUID sessionId;
  private final List<RegionDto> demands;
  private final ImmutableTable<String, String, Double> distanceMatrix;

  @Override
  public List<BasicGenome> apply(List<BasicGenome> chromosome, Random random) {
    for (BasicGenome genome : chromosome) {
      if (mutationProbability.nextValue().nextEvent(random)) {
        double uncoveredBefore = checkCoverage(chromosome);
        String beforeFid = genome.getRegionId();
        String afterFid = mutateGenome(chromosome, genome, random).getRegionId();
        genome.setRegionId(afterFid);
        double uncoveredAfter = checkCoverage(chromosome);
        if (uncoveredAfter > uncoveredBefore) genome.setRegionId(beforeFid);
      }
    }
    return chromosome;
  }

  private BasicGenome mutateGenome(List<BasicGenome> chromosome, BasicGenome genome, Random rng) {

    List<String> reachableRegionCodes =
        distanceMatrix.row(genome.getRegionId()).keySet().stream().toList();
    // String nearestCandidate =
    // reachableRegion.entrySet().stream().min(Map.Entry.comparingByValue()).get().getKey();
    List<String> top10NearestCandidate =
        distanceMatrix.row(genome.getRegionId()).entrySet().stream()
            .sorted(Map.Entry.comparingByValue())
            .limit(10)
            .map(Map.Entry::getKey)
            .toList();
    int randomNumber = rng.nextInt(top10NearestCandidate.size());
    String candidate = top10NearestCandidate.get(randomNumber);

    List<String> newReachableRegionCodes =
        distanceMatrix.row(genome.getRegionId()).keySet().stream().toList();

    BasicGenome testGenome = new BasicGenome(candidate);
    if (!chromosome.contains(testGenome)
        && newReachableRegionCodes.size() >= reachableRegionCodes.size())
      genome.setRegionId(candidate);

    return genome;
  }

  private int checkCoverage(List<BasicGenome> chromosome) {
    List<RegionDto> facilities = chromosome.stream().map(BasicGenome::getRegionDto).toList();
    Map<RegionDto, RegionDto> allocated =
        FacilityCandidateUtil.findNearestFacilities(demands, facilities, distanceMatrix);
    return demands.size() - allocated.size();
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
