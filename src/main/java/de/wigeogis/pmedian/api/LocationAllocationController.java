package de.wigeogis.pmedian.api;

import com.google.common.collect.ImmutableTable;
import de.wigeogis.pmedian.database.dto.AllocationDto;
import de.wigeogis.pmedian.database.dto.RegionDto;
import de.wigeogis.pmedian.database.dto.SessionDto;
import de.wigeogis.pmedian.database.entity.Session;
import de.wigeogis.pmedian.database.service.AllocationService;
import de.wigeogis.pmedian.database.service.RegionService;
import de.wigeogis.pmedian.database.service.SessionService;
import de.wigeogis.pmedian.database.service.TravelCostService;
import de.wigeogis.pmedian.job.OptimizationJobService;
import de.wigeogis.pmedian.optimizer.OptimizationEngine;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import io.github.resilience4j.bulkhead.ThreadPoolBulkhead;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.core.functions.CheckedRunnable;
import io.github.resilience4j.decorators.Decorators;

@Log4j2
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1")
public class LocationAllocationController {

  private final SessionService sessionService;
  private final TravelCostService costService;
  private final RegionService regionService;
  private final AllocationService allocationService;
  private final ApplicationEventPublisher publisher;
  private final OptimizationEngine optimizationEngine;

  // Inject the resilience4j components
  private final CircuitBreaker optimizationEngineCircuitBreaker;
  private final ThreadPoolBulkhead optimizationEngineBulkhead;
  private final OptimizationJobService optimizationJobService;

  @RequestMapping(value = "/start", method = RequestMethod.POST)
  @ResponseBody
  public ResponseEntity<SessionDto> startOptimization(@RequestBody SessionDto sessionDto)
      throws Exception {

    SessionDto session = sessionService.createNewSession(sessionDto);

    Supplier<SessionDto> supplier =
        () -> {
          List<RegionDto> regions =
              regionService.getRegionsByRegionCodePattern("^DE-(8[0-9]{4}|9[0-8][0-9]{3})$");

          ImmutableTable<String, String, Double> distanceMatrix = costService.getCostMatrix();

          log.info("New session with id '" + session.getId() + "' has been created...");

          List<AllocationDto> allocations =
              optimizationEngine.evolve(session, regions, distanceMatrix);
          allocationService.insertAll(allocations);

          return sessionDto;
        };

    Supplier<CompletionStage<SessionDto>> decoratedSupplier =
        Decorators.ofSupplier(supplier)
            .withThreadPoolBulkhead(optimizationEngineBulkhead)
            .withCircuitBreaker(optimizationEngineCircuitBreaker)
            .decorate();

    CompletableFuture.supplyAsync(decoratedSupplier)
        .exceptionally(
            throwable -> {
              log.error("Optimization failed: ", throwable);
              return null;
            });

    return ResponseEntity.ok().body(session);
  }


  @RequestMapping(value = "/abort", method = RequestMethod.POST)
  @ResponseBody
  public ResponseEntity<?> abortLocationAlgorithm(@RequestBody SessionDto sessionDto)
      throws Exception {
//    sessionDto = optimizationJobManager.stop(sessionDto);
    return ResponseEntity.ok().body(sessionDto);
  }

  @RequestMapping(value = "/resume", method = RequestMethod.POST)
  @ResponseBody
  public ResponseEntity<?> resumeLocationAlgorithm(@RequestBody SessionDto sessionDto) throws Exception {
 //   optimizationJobManager.resume(sessionDto);
    return ResponseEntity.ok().body(sessionDto);
  }
}
