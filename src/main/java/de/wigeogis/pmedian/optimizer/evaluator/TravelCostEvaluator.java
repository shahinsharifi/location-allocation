package de.wigeogis.pmedian.optimizer.evaluator;

import de.wigeogis.pmedian.optimizer.model.BasicGenome;
import de.wigeogis.pmedian.optimizer.util.CostEvaluatorUtils;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.uncommons.watchmaker.framework.FitnessEvaluator;

@Log4j2
public class TravelCostEvaluator implements FitnessEvaluator<List<BasicGenome>> {

  private final CostEvaluatorUtils costEvaluatorUtils;

  public TravelCostEvaluator(CostEvaluatorUtils costEvaluatorUtils) {
    this.costEvaluatorUtils = costEvaluatorUtils;
  }

  @Override
  public double getFitness(List<BasicGenome> chromosome, List<? extends List<BasicGenome>> list) {

    double fitnessValue = 0;
    try {
      List<String> facilities = chromosome.stream().map(BasicGenome::getRegionId).toList();
      // Map<RegionDto, RegionDto> allocated = FacilityCandidateUtil.findNearestFacilities(demands,
      // facilities, dMatrix);

      // evaluating fitness based on the absolute total travel cost
      // fitnessValue = calculateTotalCost(allocated);

      // evaluating fitness based on the standard deviation of the travel cost
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
