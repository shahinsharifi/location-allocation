package de.wigeogis.pmedian.optimization.utils;

import de.wigeogis.pmedian.optimization.model.BasicGenome;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AllocationUtils {

  public static Double calculateTotalCost(
      List<BasicGenome> chromosome,
      List<String> demands,
      ConcurrentHashMap<String, ConcurrentHashMap<String, Double>> costMatrix) {
    List<String> facilities = chromosome.stream().map(BasicGenome::getRegionId).toList();
    Map<String, String> allocated =
        LocationUtils.findNearestFacilities(demands, facilities, costMatrix);
    return allocated.entrySet().stream()
        .mapToDouble(entry -> costMatrix.get(entry.getKey()).get(entry.getValue()))
        .sum();
  }
}
