package de.wigeogis.pmedian.optimizer.util;

import com.google.common.collect.ImmutableTable;
import de.wigeogis.pmedian.database.dto.AllocationDto;
import de.wigeogis.pmedian.database.dto.RegionDto;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.Getter;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.impl.reduce.longer.CountNonZero;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.BooleanIndexing;
import org.nd4j.linalg.indexing.conditions.Conditions;

public class CostEvaluatorGpuUtils {

  private final double maxTravelTime;
  private final List<String> regions;
  private final INDArray sparseMatrix;
  private final Random random = new Random();
  private final Map<String, Integer> regionIndexMap = new HashMap<>();
  private final Map<String, Integer> facilityIndexMap = new HashMap<>();

  @Getter private int fitnessProgressUpdateInterval = 20;
  @Getter private int travelCostDistributionUpdateInterval = 10;

  public CostEvaluatorGpuUtils(
      List<String> regions,
      ImmutableTable<String, String, Double> costSparseMatrix,
      double maxTravelTime) {

    this.maxTravelTime = maxTravelTime;
    this.regions = regions;
    this.sparseMatrix = convertToSparseMatrix(costSparseMatrix);

    if (regions.size() > 2500) {
      fitnessProgressUpdateInterval = 20;
      travelCostDistributionUpdateInterval = 20;
    } else if (regions.size() > 1000) {
      fitnessProgressUpdateInterval = 50;
      travelCostDistributionUpdateInterval = 50;
    } else {
      fitnessProgressUpdateInterval = 100;
      travelCostDistributionUpdateInterval = 100;
    }
  }

  public List<AllocationDto> findNearestFacilitiesForDemands(
      List<String> facilities, List<AllocationDto> allocations) {

    for (AllocationDto demand : allocations) {
      int regionIndex = this.regionIndexMap.getOrDefault(demand.getRegionId(), -1);
      findNearestFacilityForRegion(facilities, regionIndex)
          .ifPresent(nearest -> demand.setFacilityRegionId(nearest.getKey()));
    }
    return allocations;
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

    return facilities.stream()
        .map(region -> new RegionDto().setId(region))
        .collect(Collectors.toList());
  }

  public Double calculateTotalCost(List<String> facilities) {
    ConcurrentMap<String, Double> costMap = calculateCostsForFacilities(facilities);
    return costMap.values().stream().mapToDouble(Double::doubleValue).sum();
  }

  public List<Double> calculateCostMap(List<String> facilities) {
    ConcurrentMap<String, Double> costMap = calculateCostsForFacilities(facilities);
    return new ArrayList<>(costMap.values());
  }

  public int calculateUncoveredAndAboveLimitRegions(List<String> facilities) {
    int[] facilityIndices =
        facilities.stream()
            .mapToInt(facility -> this.facilityIndexMap.getOrDefault(facility, -1))
            .filter(index -> index != -1)
            .toArray();

    INDArray selectedFacilityMatrix = sparseMatrix.getColumns(facilityIndices);

    INDArray minDistances = selectedFacilityMatrix.min(1);

    long aboveMaxTravelTime =
        Nd4j.getExecutioner().exec(new CountNonZero(minDistances.gt(maxTravelTime))).getInt(0);

    long uncoveredRegions =
        Nd4j.getExecutioner().exec(new CountNonZero(minDistances.isNaN())).getInt(0);

    return (int) (uncoveredRegions + aboveMaxTravelTime);
  }

  private Optional<SimpleEntry<String, Double>> findNearestFacilityForRegion(
      List<String> facilities, int regionIndex) {
    INDArray facilityCosts = sparseMatrix.getRow(regionIndex);

    INDArray minItem = facilityCosts.min();
    Double minValue = minItem.getDouble(0);

    // Here, we are getting the index of the minimum element in the `facilityCosts`
    // Replace NaNs with Double.MAX_VALUE before using argMin
    BooleanIndexing.replaceWhere(facilityCosts, Double.MAX_VALUE, Conditions.isNan());
    int minIdx = Nd4j.argMin(facilityCosts, 0).getInt(0);
    String nearestFacility = facilities.get(minIdx);

    return (minValue.isNaN() || minValue.isInfinite())
        ? Optional.empty()
        : Optional.of(new SimpleEntry<>(nearestFacility, minValue));
  }

  private ConcurrentMap<String, Double> calculateCostsForFacilities(List<String> facilities) {
    ConcurrentMap<String, Double> costMap = new ConcurrentHashMap<>();
    for (int regionIdx = 0; regionIdx < regions.size(); regionIdx++) {
      String region = regions.get(regionIdx);
      findNearestFacilityForRegion(facilities, regionIdx)
          .ifPresent(nearest -> costMap.put(region + "|" + nearest.getKey(), nearest.getValue()));
    }
    return costMap;
  }

  private Set<String> getReachableRegions(String facility) {
    if (!this.facilityIndexMap.containsKey(facility)) {
      return Collections.emptySet();
    }

    int facilityIdx = this.facilityIndexMap.get(facility);

    if (sparseMatrix == null) {
      // Sparse matrix must not be null
      return Collections.emptySet();
    }

    INDArray facilityCosts = sparseMatrix.getColumn(facilityIdx);

    if (facilityCosts == null) {
      // The facility costs array must not be null
      return Collections.emptySet();
    }

    // Find indices of reachable regions
    INDArray isReachable = facilityCosts.lte(maxTravelTime);

    INDArray[] indices = Nd4j.where(isReachable, null, null);
    if (indices.length == 0) {
      return Collections.emptySet();
    }

    List<Integer> reachableIndices =
        Arrays.stream(indices)
            .mapToInt(idx -> idx.getInt(0))
            .filter(Objects::nonNull)
            .boxed()
            .toList();

    if (regions == null || regions.isEmpty() || reachableIndices.isEmpty()) {
      return Collections.emptySet();
    }

    return reachableIndices.stream()
        .filter(
            index ->
                index >= 0 && index < regions.size()) // Check if index is within regions' bounds
        .map(regions::get)
        .collect(Collectors.toSet());
  }

  private INDArray convertToSparseMatrix(ImmutableTable<String, String, Double> travelCostMatrix) {

    for (String region : regions) {
      this.regionIndexMap.put(region, this.regionIndexMap.size());
    }
    travelCostMatrix
        .columnKeySet()
        .forEach(
            facility -> this.facilityIndexMap.putIfAbsent(facility, this.facilityIndexMap.size()));

    // Initialize sparseMatrix with NaNs (assuming NaN indicates no connection)
    INDArray sMatrix =
        Nd4j.create(this.regionIndexMap.size(), this.regionIndexMap.size()).assign(Double.NaN);

    travelCostMatrix
        .cellSet()
        .forEach(
            cell -> {
              String region = cell.getRowKey();
              String facility = cell.getColumnKey();

              // Ensure the region is in our predefined list before adding it to the matrix
              if (this.regionIndexMap.containsKey(region)
                  && this.facilityIndexMap.containsKey(facility)) {
                int regionIdx = this.regionIndexMap.get(region);
                int facilityIdx = this.facilityIndexMap.get(facility);
                sMatrix.putScalar(regionIdx, facilityIdx, cell.getValue());
              }
            });
    return sMatrix;
  }
}
