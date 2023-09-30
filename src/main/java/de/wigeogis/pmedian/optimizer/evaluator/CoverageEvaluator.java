package de.wigeogis.pmedian.optimizer.evaluator;

import com.google.common.collect.ImmutableTable;
import de.wigeogis.pmedian.database.dto.RegionDto;
import de.wigeogis.pmedian.optimizer.model.BasicGenome;
import de.wigeogis.pmedian.optimizer.util.CostEvaluatorUtils;
import de.wigeogis.pmedian.optimizer.util.FacilityCandidateUtil;
import de.wigeogis.pmedian.websocket.NotificationService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.uncommons.watchmaker.framework.FitnessEvaluator;

public class CoverageEvaluator implements FitnessEvaluator<List<BasicGenome>> {

  private final CostEvaluatorUtils costEvaluatorUtils;

  public CoverageEvaluator(List<RegionDto> demands, ImmutableTable<String, String, Double> distanceMatrix, Double maxTravelTime) {
    this.costEvaluatorUtils = new CostEvaluatorUtils(demands.stream().map(RegionDto::getId).toList(), distanceMatrix, maxTravelTime);
  }

  @Override
  public double getFitness(List<BasicGenome> chromosome, List<? extends List<BasicGenome>> list) {
    List<String> facilities = chromosome.stream().map(BasicGenome::getRegionId).toList();
    return this.costEvaluatorUtils.calculateUncoveredAndAboveLimitRegions(facilities);
  }



//  @Override
//  public double getFitness(List<BasicGenome> chromosome, List<? extends List<BasicGenome>> list) {
//    List<RegionDto> facilities = chromosome.stream().map(BasicGenome::getRegionId).toList();
//    Map<RegionDto, RegionDto> allocated = FacilityCandidateUtil.findNearestFacilities(demands, facilities, distanceMatrix);
//    return demands.size() - allocated.size();
//  }


    @Override
  public boolean isNatural() {
    return false;
  }
}
