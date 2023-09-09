package de.wigeogis.pmedian.job.optimization;

import de.wigeogis.pmedian.database.dto.AllocationDto;
import de.wigeogis.pmedian.database.dto.SessionDto;
import de.wigeogis.pmedian.database.entity.Session;
import de.wigeogis.pmedian.database.service.TravelCostService;
import de.wigeogis.pmedian.optimization.OptimizationEngine;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;
import org.springframework.batch.item.ItemProcessor;

public class OptimizationProcessor implements ItemProcessor<List<String>, List<AllocationDto>> {

  private final TravelCostService travelCostService;
  private final OptimizationEngine optimizationEngine;

  public OptimizationProcessor(Session sessionDto, TravelCostService travelCostService) {
    Session session = new ModelMapper().map(sessionDto, Session.class);
    this.travelCostService = travelCostService;
    optimizationEngine = new OptimizationEngine(session);
  }

  @Override
  public List<AllocationDto> process(List<String> demands) throws Exception {
    ConcurrentHashMap<String, ConcurrentHashMap<String, Double>> matrixByRegions =
        this.travelCostService.getSparseCostMatrix();
    return this.optimizationEngine.evolve(
        demands, this.getCostMatrixByRegions(matrixByRegions, demands));
  }

  private ConcurrentHashMap<String, ConcurrentHashMap<String, Double>> getCostMatrixByRegions(
      ConcurrentHashMap<String, ConcurrentHashMap<String, Double>> costMatrix,
      List<String> regions) {

    ConcurrentHashMap<String, ConcurrentHashMap<String, Double>> result = new ConcurrentHashMap<>();

    for (String startId : regions) {
      if (costMatrix.containsKey(startId)) {
        ConcurrentHashMap<String, Double> endIdMap = costMatrix.get(startId);
        ConcurrentHashMap<String, Double> filteredEndIdMap =
            (ConcurrentHashMap<String, Double>)
                endIdMap.entrySet().stream()
                    .filter(entry -> regions.contains(entry.getKey()))
                    .collect(Collectors.toConcurrentMap(Entry::getKey, Entry::getValue));

        if (!filteredEndIdMap.isEmpty()) {
          result.put(startId, filteredEndIdMap);
        }
      }
    }

    return result;
  }
}
