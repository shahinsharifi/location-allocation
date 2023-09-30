package de.wigeogis.pmedian.optimizer.evaluator;

import com.google.common.collect.ImmutableTable;
import de.wigeogis.pmedian.database.dto.RegionDto;
import de.wigeogis.pmedian.optimizer.model.BasicGenome;
import de.wigeogis.pmedian.optimizer.util.CostEvaluatorUtils;
import de.wigeogis.pmedian.optimizer.util.FacilityCandidateUtil;
import de.wigeogis.pmedian.websocket.NotificationService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.LoggerFactory;
import org.uncommons.watchmaker.framework.FitnessEvaluator;

@Log4j2
public class TravelCostEvaluator implements FitnessEvaluator<List<BasicGenome>> {

  private final List<RegionDto> demands;
  private final ImmutableTable<String, String, Double> dMatrix;

  private final CostEvaluatorUtils costEvaluatorUtils;


  public TravelCostEvaluator(List<RegionDto> demands, ImmutableTable<String, String, Double> dMatrix, Double maxTravelTime) {
    this.demands = demands;
    this.dMatrix = dMatrix;
    this.costEvaluatorUtils = new CostEvaluatorUtils(demands.stream().map(RegionDto::getId).toList(), dMatrix, maxTravelTime);
  }

  @Override
  public double getFitness(List<BasicGenome> chromosome, List<? extends List<BasicGenome>> list) {

    double fitnessValue = 0;
    try {
      List<String> facilities = chromosome.stream().map(BasicGenome::getRegionId).toList();
      //Map<RegionDto, RegionDto> allocated = FacilityCandidateUtil.findNearestFacilities(demands, facilities, dMatrix);

      //evaluating fitness based on the absolute total travel cost
      //fitnessValue = calculateTotalCost(allocated);

      //evaluating fitness based on the standard deviation of the travel cost
      fitnessValue = this.costEvaluatorUtils.calculateStandardDeviation(facilities);

    } catch (Exception e) {
      log.error("Error in TravelCostEvaluator: {}", e.getMessage());
    }
    return fitnessValue;
  }





  @Override
  public boolean isNatural() {
    return false;
  }
}
