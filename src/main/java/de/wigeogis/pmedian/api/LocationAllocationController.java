package de.wigeogis.pmedian.api;

import com.google.common.collect.ImmutableTable;
import de.wigeogis.pmedian.database.dto.AllocationDto;
import de.wigeogis.pmedian.database.dto.SessionDto;
import de.wigeogis.pmedian.database.entity.SessionStatus;
import de.wigeogis.pmedian.database.service.AllocationService;
import de.wigeogis.pmedian.database.service.LocationService;
import de.wigeogis.pmedian.database.service.RegionService;
import de.wigeogis.pmedian.database.service.SessionService;
import de.wigeogis.pmedian.database.service.TravelCostService;
import de.wigeogis.pmedian.optimizer.OptimizationEngine;
import de.wigeogis.pmedian.websocket.MessageSubject;
import de.wigeogis.pmedian.websocket.NotificationService;
import io.github.resilience4j.bulkhead.ThreadPoolBulkhead;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.decorators.Decorators;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
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

@Log4j2
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1")
public class LocationAllocationController {

  private final SessionService sessionService;
  private final TravelCostService costService;
  private final RegionService regionService;
  private final LocationService locationService;
  private final AllocationService allocationService;
  private final ApplicationEventPublisher publisher;
  private final OptimizationEngine optimizationEngine;
  private final CircuitBreaker optimizationEngineCircuitBreaker;
  private final ThreadPoolBulkhead optimizationEngineBulkhead;
  private final NotificationService notificationService;

  @RequestMapping(value = "/start", method = RequestMethod.POST)
  @ResponseBody
  public ResponseEntity<SessionDto> startOptimization(@RequestBody SessionDto sessionDto)
      throws Exception {

    SessionDto session = sessionService.createNewSession(sessionDto);

    Supplier<SessionDto> supplier =
        () -> {
          // List<RegionDto> regions =
          // regionService.findRegionsByWKTPolygon(session.getSpatialQuery());
          // regionService.getRegionsByRegionCodePattern("^DE-(8[0-9]{4}|9[0-8][0-9]{3})$");

          List<AllocationDto> allocations =
              allocationService.insertAndFetchRegionsByWKTPolygon(
                  session.getId(), session.getWkt());

          session.setStatus(SessionStatus.RUNNING);
          notificationService.publishData(
              session.getId(),
              MessageSubject.SESSION_STATUS,
              "Session status changed to " + session.getStatus(),
              Map.of("id", session.getId(), "status", session.getStatus()));

          //          List<AllocationDto> allocations =
          //              regions.stream()
          //                  .map(
          //                      regionDto ->
          //                          new AllocationDto()
          //                              .setSessionId(session.getId())
          //                              .setRegionId(regionDto.getId())
          //                              .setFacilityRegionId(null)
          //                              .setTravelCost(-1d))
          //                  .toList();
          //          CompletableFuture<Integer> completableFuture =
          // allocationService.insertAllAsync(allocations);
          //
          //          completableFuture.whenComplete(
          //              (result, throwable) -> {
          //                if (throwable != null) {
          //                  log.error("Optimization failed: ", throwable);
          //                } else {
          //                  session.setStatus(SessionStatus.RUNNING);
          //                  notificationService.publishData(
          //                      session.getId(),
          //                      MessageSubject.SESSION_STATUS,
          //                      "Session status changed to " + session.getStatus(),
          //                      Map.of("status", session.getStatus()));
          //                }
          //              });

          ImmutableTable<String, String, Double> distanceMatrix =
              costService.getByRegionIdListAndTravelTime(session);

          log.info("New session with id '" + session.getId() + "' has been created...");

          allocations = optimizationEngine.evolve(session, allocations, distanceMatrix);

          allocationService.insertAll(allocations);
          locationService.insertAllFromAllocation(allocations);

          session.setStatus(SessionStatus.COMPLETED);
          notificationService.publishData(
              session.getId(),
              MessageSubject.SESSION_STATUS,
              "Session status changed to " + session.getStatus(),
              Map.of("id", session.getId(), "status", session.getStatus()));

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

    session.setStatus(SessionStatus.STARTING);

    return ResponseEntity.ok().body(session);
  }

  @RequestMapping(value = "/abort", method = RequestMethod.POST)
  @ResponseBody
  public ResponseEntity<?> abortLocationAlgorithm(@RequestBody SessionDto sessionDto) {
    //    sessionDto = optimizationJobManager.stop(sessionDto);
    return ResponseEntity.ok().body(sessionDto);
  }

  @RequestMapping(value = "/resume", method = RequestMethod.POST)
  @ResponseBody
  public ResponseEntity<?> resumeLocationAlgorithm(@RequestBody SessionDto sessionDto) {
    //   optimizationJobManager.resume(sessionDto);
    return ResponseEntity.ok().body(sessionDto);
  }
}
