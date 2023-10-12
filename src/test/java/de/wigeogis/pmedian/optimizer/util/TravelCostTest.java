package de.wigeogis.pmedian.optimizer.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.ImmutableTable;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TravelCostTest {

  @Test
  public void testCalculateUncoveredAndAboveLimitRegions_allRegionsCovered() {
    List<String> regions = Arrays.asList("R1", "R2", "R3");
    List<String> facilities = Arrays.asList("F1", "F2");

    ImmutableTable.Builder<String, String, Double> builder = new ImmutableTable.Builder<>();
    builder.put("R1", "F1", 5.0);
    builder.put("R1", "F2", 8.0);
    builder.put("R2", "F1", 3.0);
    builder.put("R3", "F2", 6.0);
    ImmutableTable<String, String, Double> costSparseMatrix = builder.build();

    CostEvaluatorGpuUtils evaluator = new CostEvaluatorGpuUtils(regions, costSparseMatrix, 10.0);
    assertEquals(0, evaluator.calculateUncoveredAndAboveLimitRegions(facilities));
  }

  @Test
  public void testCalculateUncoveredAndAboveLimitRegions_someRegionsUncovered() {
    List<String> regions = Arrays.asList("R1", "R2", "R3");
    List<String> facilities = Arrays.asList("F1", "F2");

    ImmutableTable.Builder<String, String, Double> builder = new ImmutableTable.Builder<>();
    builder.put("R1", "F1", 9.0);
    builder.put("R1", "F2", 6.0);
    builder.put("R2", "F1", 3.0);
    ImmutableTable<String, String, Double> costSparseMatrix = builder.build();

    CostEvaluatorGpuUtils evaluator = new CostEvaluatorGpuUtils(regions, costSparseMatrix, 10.0);
    assertEquals(1, evaluator.calculateUncoveredAndAboveLimitRegions(facilities));
    assertEquals(9.0, evaluator.calculateTotalCost(facilities));
  }

  @Test
  public void testCalculateUncoveredAndAboveLimitRegions_someRegionsAboveLimit() {
    List<String> regions = Arrays.asList("R1", "R2", "R3");
    List<String> facilities = Arrays.asList("F1", "F2");

    ImmutableTable.Builder<String, String, Double> builder = new ImmutableTable.Builder<>();
    builder.put("R1", "F1", 11.0);
    builder.put("R2", "F1", 3.0);
    builder.put("R3", "F2", 12.0);
    ImmutableTable<String, String, Double> costSparseMatrix = builder.build();

    CostEvaluatorGpuUtils evaluator = new CostEvaluatorGpuUtils(regions, costSparseMatrix, 10.0);
    assertEquals(2, evaluator.calculateUncoveredAndAboveLimitRegions(facilities));
    assertEquals(3.0, evaluator.calculateCostMap(facilities).get(1));
  }

  @Test
  public void testCalculateUncoveredAndAboveLimitRegions_allRegionsUncoveredOrAboveLimit() {
    List<String> regions = Arrays.asList("R1", "R2", "R3");
    List<String> facilities = Arrays.asList("F1", "F2");

    ImmutableTable.Builder<String, String, Double> builder = new ImmutableTable.Builder<>();
    builder.put("R1", "F1", 11.0);
    builder.put("R3", "F2", 12.0);
    ImmutableTable<String, String, Double> costSparseMatrix = builder.build();

    CostEvaluatorGpuUtils evaluator = new CostEvaluatorGpuUtils(regions, costSparseMatrix, 10.0);
    assertEquals(3, evaluator.calculateUncoveredAndAboveLimitRegions(facilities));
  }
}
