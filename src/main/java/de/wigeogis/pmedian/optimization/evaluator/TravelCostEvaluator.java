package de.wigeogis.pmedian.optimization.evaluator;


import de.wigeogis.pmedian.optimization.model.BasicGenome;
import de.wigeogis.pmedian.optimization.utils.LocationUtils;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.uncommons.watchmaker.framework.FitnessEvaluator;

import java.util.List;

@Log4j2
public class TravelCostEvaluator implements FitnessEvaluator<List<BasicGenome>> {

  private final List<String> demands;
  private final ConcurrentHashMap<String, ConcurrentHashMap<String, Double>> costMatrix;

  public TravelCostEvaluator(
      List<String> demands, ConcurrentHashMap<String, ConcurrentHashMap<String, Double>> costMatrix) {
    this.demands = demands;
    this.costMatrix = costMatrix;
  }

  @Override
  public double getFitness(List<BasicGenome> chromosome, List<? extends List<BasicGenome>> list) {

    double fitnessValue = 0;
    try {
      List<String> facilities = chromosome.stream().map(BasicGenome::getRegionId).toList();
      Map<String, String> allocated = LocationUtils.findNearestFacilities(demands, facilities, costMatrix);

      //evaluating fitness based on the absolute total travel cost
      //fitnessValue = calculateTotalCost(allocated);

      //evaluating fitness based on the standard deviation of the travel cost
      fitnessValue = calculateStandardDeviation(allocated);

    } catch (Exception e) {
      log.error("Error in TravelCostEvaluator: {}", e.getMessage());
    }
    return fitnessValue;
  }

  private Double calculateTotalCost(Map<String, String> allocated) {
    return allocated.entrySet().stream()
        .mapToDouble(
            entry -> costMatrix.get(entry.getKey()).get(entry.getValue()))
        .sum();
  }

  private Double calculateStandardDeviation(Map<String, String> allocated) {
    DescriptiveStatistics ds = new DescriptiveStatistics();
    allocated.forEach((key, value1) -> {
      ConcurrentHashMap<String, Double> innerMap = costMatrix.get(key);
      if (innerMap != null) {
        Double value = innerMap.get(value1);
        if (value != null) {
          ds.addValue(value);
        }
      }
    });
    return ds.getStandardDeviation();
  }

  @Override
  public boolean isNatural() {
    return false;
  }
}
