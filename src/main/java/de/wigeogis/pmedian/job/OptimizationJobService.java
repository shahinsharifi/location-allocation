package de.wigeogis.pmedian.job;

import de.wigeogis.pmedian.database.dto.SessionDto;
import de.wigeogis.pmedian.database.service.AllocationService;
import de.wigeogis.pmedian.database.service.RegionService;
import de.wigeogis.pmedian.database.service.TravelCostService;
import de.wigeogis.pmedian.optimizer.OptimizationEngine;
import io.github.resilience4j.bulkhead.ThreadPoolBulkhead;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class OptimizationJobService {

  private final OptimizationEngine optimizationEngine;
  private final TravelCostService costService;
  private final RegionService regionService;
  private final AllocationService allocationService;
  private final ApplicationEventPublisher publisher;

  // Inject the resilience4j components
  private final CircuitBreaker optimizationEngineCircuitBreaker;
  private final ThreadPoolBulkhead optimizationEngineBulkhead;


  public SessionDto run(SessionDto sessionDto) throws Exception {
//    List<RegionDto> regions = regionService.getRegionsByRegionCodePattern("^DE-(8[0-9]{4}|9[0-8][0-9]{3})$");
//    ImmutableTable<String, String, Double> distanceMatrix = costService.getCostMatrix();
//    Thread execution =
//        new Thread(
//            () -> {
//              try {
//
//                List<AllocationDto> allocations = optimizationEngine.evolve(sessionDto, regions, distanceMatrix);
//                allocationService.insertAll(allocations);
//                allocationService.insertAllAsync(allocations);
//              } catch (Exception e) {
//                e.printStackTrace();
//              }
//            });
//    execution.start();
//    sessionDto.setStatus(SessionStatus.RUNNING);
    return sessionDto;
  }
}
