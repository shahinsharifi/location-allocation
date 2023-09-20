package de.wigeogis.pmedian.optimizer.evaluator;

import com.google.common.collect.ImmutableTable;
import de.wigeogis.pmedian.database.dto.RegionDto;
import de.wigeogis.pmedian.optimizer.model.BasicGenome;
import de.wigeogis.pmedian.optimizer.util.FacilityCandidateUtil;
import de.wigeogis.pmedian.websocket.NotificationService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.uncommons.watchmaker.framework.FitnessEvaluator;

public class CoverageEvaluator implements FitnessEvaluator<List<BasicGenome>> {


  private final List<RegionDto> demands;
  private final ImmutableTable<String, String, Double> distanceMatrix;

  private final UUID sessionId;
  private final NotificationService notificationService;

  public CoverageEvaluator(
      UUID sessionId, List<RegionDto> demands, ImmutableTable<String, String, Double> distanceMatrix, NotificationService notificationService) {
    this.distanceMatrix = distanceMatrix;
    this.demands = demands;
    this.sessionId = sessionId;
    this.notificationService = notificationService;
  }

  @Override
  public double getFitness(List<BasicGenome> chromosome, List<? extends List<BasicGenome>> list) {
    List<RegionDto> facilities = chromosome.stream().map(BasicGenome::getRegionDto).toList();
    Map<RegionDto, RegionDto> allocated = FacilityCandidateUtil.findNearestFacilities(demands, facilities, distanceMatrix);
    return demands.size() - allocated.size();
  }


    @Override
  public boolean isNatural() {
    return false;
  }
}
