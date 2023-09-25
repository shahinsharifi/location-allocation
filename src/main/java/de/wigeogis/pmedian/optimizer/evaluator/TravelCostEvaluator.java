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
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.LoggerFactory;
import org.uncommons.watchmaker.framework.FitnessEvaluator;

public class TravelCostEvaluator implements FitnessEvaluator<List<BasicGenome>> {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(TravelCostEvaluator.class);
  private final List<RegionDto> demands;
  private final ImmutableTable<String, String, Double> dMatrix;
  private  final ConcurrentHashMap<String, ConcurrentHashMap<String, Double>> sparseCostMatrix;

  private final UUID sessionId;
  private final NotificationService notificationService;
  private final Map<String, Object> generationFitnessProgress;
  private final Map<String, Object> overallStandardDeviationOfTravelTime;


  public TravelCostEvaluator(
      UUID sessionId, List<RegionDto> demands, ImmutableTable<String, String, Double> dMatrix, NotificationService notificationService) {
    this.demands = demands;
    this.dMatrix = dMatrix;
    this.sparseCostMatrix = CostEvaluatorUtils.convertToSparseMatrix(dMatrix);
    this.notificationService = notificationService;

    this.sessionId = sessionId;
    this.generationFitnessProgress = new HashMap<>();
    this.overallStandardDeviationOfTravelTime = new HashMap<>();
  }

  @Override
  public double getFitness(List<BasicGenome> chromosome, List<? extends List<BasicGenome>> list) {

    double fitnessValue = 0;
    try {
      List<RegionDto> facilities = chromosome.stream().map(BasicGenome::getRegionDto).toList();
      Map<RegionDto, RegionDto> allocated = FacilityCandidateUtil.findNearestFacilities(demands, facilities, dMatrix);

      //evaluating fitness based on the absolute total travel cost
      fitnessValue = calculateTotalCost(allocated);

      //evaluating fitness based on the standard deviation of the travel cost
    //  fitnessValue = calculateStandardDeviation(allocated);

    } catch (Exception e) {
      LOGGER.error("Error in TravelCostEvaluator: {}", e.getMessage());
    }
    return fitnessValue;
  }

  private Double calculateTotalCost(Map<RegionDto, RegionDto> allocated) {
    return allocated.entrySet().stream()
        .mapToDouble(
            entry -> dMatrix.get(entry.getKey().getId(), entry.getValue().getId()))
        .sum();
  }

  private Double calculateStandardDeviation(Map<RegionDto, RegionDto> allocated) {
    DescriptiveStatistics ds = new DescriptiveStatistics();
    allocated.forEach((key, value1) -> {
      ConcurrentHashMap<String, Double> innerMap = sparseCostMatrix.get(key.getId());
      if (innerMap != null) {
        Double value = innerMap.get(value1.getId());
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
