package de.wigeogis.pmedian.optimizer.util;

import com.google.common.collect.ImmutableTable;
import de.wigeogis.pmedian.database.dto.RegionDto;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.nd4j.common.primitives.Pair;
import org.nd4j.linalg.api.buffer.DataType;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.util.*;
import org.nd4j.linalg.factory.Nd4jBackend;

@Log4j2
public class CostEvaluatorUtils {

  private final Double maxTravelTime;
  private final List<String> regions;
  private final ConcurrentHashMap<String, ConcurrentHashMap<String, Double>> sparseMatrix;

  private static final Random random = new Random();

  public CostEvaluatorUtils(
      List<String> regions,
      ImmutableTable<String, String, Double> costSparseMatrix,
      Double maxTravelTime) {
    this.maxTravelTime = maxTravelTime;
    this.regions = new ArrayList<>(regions);
    this.sparseMatrix = convertToSparseMatrix(costSparseMatrix);
  }

  public Double calculateStandardDeviation(List<String> facilities) {
    DescriptiveStatistics ds = new DescriptiveStatistics();
    ConcurrentHashMap<String, Double> facilityCost = new ConcurrentHashMap<>();
    for (String region : regions) {
      final ConcurrentHashMap<String, Double> facilityToCost = sparseMatrix.get(region);
      facilities.stream()
          .filter(facilityToCost::containsKey)
          .map(facility -> new AbstractMap.SimpleEntry<>(facility, facilityToCost.get(facility)))
          .min(Map.Entry.comparingByValue())
          .ifPresent(
              nearest -> {
                if (facilityCost.containsKey(nearest.getKey())) {
                  facilityCost.computeIfPresent(nearest.getKey(), (k, v) -> nearest.getValue() + v);
                } else {
                  facilityCost.put(nearest.getKey(), nearest.getValue());
                }
              });
    }
    for (Double cost : facilityCost.values()) {
      ds.addValue(cost);
    }
    return ds.getStandardDeviation();
  }

  public Double calculateTotalCost(List<String> facilities) {
    Map<String, Double> costMap = new HashMap<>();
    for (String region : regions) {
      final ConcurrentHashMap<String, Double> facilityToCost = sparseMatrix.get(region);
      facilities.stream()
          .filter(facilityToCost::containsKey)
          .map(facility -> new AbstractMap.SimpleEntry<>(facility, facilityToCost.get(facility)))
          .min(Map.Entry.comparingByValue())
          .ifPresent(nearest -> costMap.put(region + "|" + nearest.getKey(), nearest.getValue()));
    }
    return costMap.values().stream().mapToDouble(Double::doubleValue).sum();
  }

  public Integer calculateUncoveredAndAboveLimitRegions(List<String> facilities) {
    AtomicInteger uncoveredRegions = new AtomicInteger();
    AtomicInteger aboveMaxTravelTime = new AtomicInteger();
    for (String region : regions) {
      final ConcurrentHashMap<String, Double> facilityToCost = sparseMatrix.get(region);
      facilities.stream()
          .filter(facilityToCost::containsKey)
          .map(facility -> new AbstractMap.SimpleEntry<>(facility, facilityToCost.get(facility)))
          .min(Map.Entry.comparingByValue())
          .ifPresentOrElse(
              nearest -> {
                if (nearest.getValue() > maxTravelTime) aboveMaxTravelTime.incrementAndGet();
              },
              uncoveredRegions::incrementAndGet
          );
    }
    return uncoveredRegions.get() + aboveMaxTravelTime.get();
  }

  public List<RegionDto> findFacilityCandidates(int N) {

    Set<String> remainingFacilities = new HashSet<>(regions);
    List<String> facilities = new ArrayList<>();

    while (facilities.size() < N && !remainingFacilities.isEmpty()) {
      String center =
          new ArrayList<>(remainingFacilities).get(random.nextInt(remainingFacilities.size()));
      Set<String> coveredZipcodes = getReachableRegions(center);
      if (!coveredZipcodes.isEmpty()) {
        facilities.add(center);
      }

      // To ensure the center itself gets removed from remainingZipcodes.
      coveredZipcodes.add(center);
      remainingFacilities.removeAll(coveredZipcodes);
    }

    return facilities.stream().map(region -> new RegionDto().setId(region)).collect(Collectors.toList());
  }

  private Set<String> getReachableRegions(String facility) {
    ConcurrentHashMap<String, Double> facilityToCost = sparseMatrix.get(facility);
    return facilityToCost.keySet().stream()
        .filter(region -> facilityToCost.get(region) <= maxTravelTime)
        .collect(Collectors.toSet());
  }

  private ConcurrentHashMap<String, ConcurrentHashMap<String, Double>> convertToSparseMatrix(
      ImmutableTable<String, String, Double> table) {
    ConcurrentHashMap<String, ConcurrentHashMap<String, Double>> sparseMatrix =
        new ConcurrentHashMap<>();
    for (String region : table.rowKeySet()) {
      ConcurrentHashMap<String, Double> innerMap = new ConcurrentHashMap<>();
      for (String facility : table.columnKeySet()) {
        if (table.contains(region, facility)) {
          innerMap.put(facility, table.get(region, facility));
        }
      }
      sparseMatrix.put(region, innerMap);
    }
    return sparseMatrix;
  }
}
