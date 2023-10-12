package de.wigeogis.pmedian.optimizer.util;

import com.google.common.collect.ImmutableTable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.impl.reduce.longer.CountNonZero;
import org.nd4j.linalg.factory.Nd4j;

public class CostEvaluatorGpuUtils {

  private final double maxTravelTime;
  private final INDArray sparseMatrix;
  private final Map<String, Integer> regionIndex = new HashMap<>();
  private final Map<String, Integer> facilityIndex = new HashMap<>();

  public CostEvaluatorGpuUtils(
      List<String> regions,
      ImmutableTable<String, String, Double> costSparseMatrix,
      double maxTravelTime) {

    this.maxTravelTime = maxTravelTime;
    // Creating an index map for regions and facilities
    for(String region : regions) {
      regionIndex.put(region, regionIndex.size());
    }
    costSparseMatrix.columnKeySet().forEach(facility -> facilityIndex.putIfAbsent(facility, facilityIndex.size()));

    // Initialize sparseMatrix with NaNs (assuming NaN indicates no connection)
    sparseMatrix = Nd4j.create(regionIndex.size(), regionIndex.size()).assign(Double.NaN);

    costSparseMatrix.cellSet().forEach(cell -> {
      String region = cell.getRowKey();
      String facility = cell.getColumnKey();

      // Ensure the region is in our predefined list before adding it to the matrix
      if(regionIndex.containsKey(region) && facilityIndex.containsKey(facility)) {
        int regionIdx = regionIndex.get(region);
        int facilityIdx = facilityIndex.get(facility);
        sparseMatrix.putScalar(regionIdx, facilityIdx, cell.getValue());
      }
    });
  }

  public int calculateUncoveredAndAboveLimitRegions(List<String> facilities) {
    int[] facilityIndices = facilities.stream()
        .mapToInt(facility -> facilityIndex.getOrDefault(facility, -1))
        .filter(index -> index != -1)
        .toArray();

    INDArray selectedFacilityMatrix = sparseMatrix.getColumns(facilityIndices);

    INDArray minDistances = selectedFacilityMatrix.min(1);

    long aboveMaxTravelTime = Nd4j.getExecutioner()
        .exec(new CountNonZero(minDistances.gt(maxTravelTime)))
        .getInt(0);

    long uncoveredRegions = Nd4j.getExecutioner()
        .exec(new CountNonZero(minDistances.isNaN()))
        .getInt(0);

    return (int) (uncoveredRegions + aboveMaxTravelTime);
  }
}
