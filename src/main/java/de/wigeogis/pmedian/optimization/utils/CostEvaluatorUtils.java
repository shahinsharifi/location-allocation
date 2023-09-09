package de.wigeogis.pmedian.optimization.utils;

import com.google.common.collect.ImmutableTable;
import java.util.concurrent.ConcurrentHashMap;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class CostEvaluatorUtils {
  private List<String> regions;
  private ConcurrentHashMap<String, ConcurrentHashMap<String, Double>> costSparseMatrixMap;
  private Map<List<String>, Double> memoizedCosts;

  private INDArray matrixR;  // Matrix of travel costs

  public CostEvaluatorUtils(List<String> regions, ImmutableTable<String, String, Double> costSparseMatrix) {
    this.regions = regions;
    this.costSparseMatrixMap = convertToSparseMatrix(costSparseMatrix);
    this.memoizedCosts = new HashMap<>();

    int rowCount = regions.size();
    int colCount = regions.size();  // assuming a region can also be a facility

    this.matrixR = Nd4j.zeros(rowCount, colCount);

    for (int i = 0; i < rowCount; i++) {
      for (int j = 0; j < colCount; j++) {
        String region = regions.get(i);
        String facility = regions.get(j);

        if (costSparseMatrixMap.get(region).containsKey(facility)) {
          matrixR.putScalar(i, j, costSparseMatrixMap.get(region).get(facility));
        }
      }
    }
  }

  public static ConcurrentHashMap<String, ConcurrentHashMap<String, Double>> convertToSparseMatrix(ImmutableTable<String, String, Double> table) {
    ConcurrentHashMap<String, ConcurrentHashMap<String, Double>> sparseMatrix = new ConcurrentHashMap<>();
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

  public double evaluate(List<String> candidateFacilities) {
    // Check memoized results first.
    if (memoizedCosts.containsKey(candidateFacilities)) {
      return memoizedCosts.get(candidateFacilities);
    }

    INDArray vectorF = Nd4j.zeros(regions.size(), 1);
    for (String facility : candidateFacilities) {
      int index = regions.indexOf(facility);
      if (index != -1) {
        vectorF.putScalar(index, 1);
      }
    }

    INDArray result = matrixR.mmul(vectorF);

    double totalCost = 0.0;
    for (int i = 0; i < result.length(); i++) {
      totalCost += result.getDouble(i, 0);
    }

    // Store the computed result for reuse.
    memoizedCosts.put(candidateFacilities, totalCost);

    return totalCost;
  }
}
