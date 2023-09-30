package de.wigeogis.pmedian.job;

import com.google.common.collect.ImmutableTable;
import de.wigeogis.pmedian.database.dto.AllocationDto;
import de.wigeogis.pmedian.database.dto.SessionDto;
import de.wigeogis.pmedian.database.entity.SessionStatus;
import de.wigeogis.pmedian.database.service.AllocationService;
import de.wigeogis.pmedian.database.service.LocationService;
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
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.uncommons.watchmaker.framework.termination.UserAbort;

@Log4j2
@Service
@RequiredArgsConstructor
public class JobManager {

  private final SessionService sessionService;
  private final TravelCostService costService;
  private final LocationService locationService;
  private final AllocationService allocationService;
  private final OptimizationEngine optimizationEngine;
  private final CircuitBreaker optimizationEngineCircuitBreaker;
  private final ThreadPoolBulkhead optimizationEngineBulkhead;
  private final NotificationService notificationService;

  private final ConcurrentHashMap<UUID, UserAbort> abortSignalStorage = new ConcurrentHashMap<>();

  public SessionDto start(SessionDto session) throws Exception {
    Supplier<SessionDto> supplier =
        () -> {
          List<AllocationDto> allocations =
              allocationService.insertAndFetchRegionsByWKTPolygon(
                  session.getId(), session.getWkt(), session.getMaxTravelTimeInMinutes());

          this.updateSessionStatus(session.getId(), SessionStatus.RUNNING, true);

          ImmutableTable<String, String, Double> distanceMatrix =
              costService.getByRegionIdListAndTravelTime(session);

          log.info("New session with id '" + session.getId() + "' has been created...");

          UserAbort abortSignal = new UserAbort();
          this.abortSignalStorage.put(session.getId(), abortSignal);

          allocations =
              optimizationEngine.evolve(session, allocations, distanceMatrix, abortSignal);

          allocationService.insertAll(allocations);
          locationService.insertAllFromAllocation(allocations);

          SessionDto sessionDto = sessionService.getById(session.getId());
          if (sessionDto.getStatus() == SessionStatus.ABORTING) {
            updateSessionStatus(sessionDto.getId(), SessionStatus.ABORTED, true);
          } else if (sessionDto.getStatus() == SessionStatus.RUNNING) {
            updateSessionStatus(sessionDto.getId(), SessionStatus.COMPLETED, true);
          }

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
              updateSessionStatus(session.getId(), SessionStatus.FAILED, true);
              log.error("Optimization failed: ", throwable);
              return null;
            });

    return updateSessionStatus(session.getId(), SessionStatus.STARTING);
  }

  public SessionDto stop(SessionDto session) {
    updateSessionStatus(session.getId(), SessionStatus.ABORTING);
    if (this.abortSignalStorage.containsKey(session.getId())) {
      this.abortSignalStorage.get(session.getId()).abort();
    }
    return session;
  }

  private SessionDto updateSessionStatus(UUID sessionId, SessionStatus status) {
    return updateSessionStatus(sessionId, status, null);
  }

  private SessionDto updateSessionStatus(UUID sessionId, SessionStatus status, Boolean publish) {
    SessionDto sessionDto = sessionService.updateSessionStatus(sessionId, status);
    if (publish != null && publish) {
      log.info("Publishing session status update: " + status);
      notificationService.publishData(
          sessionId,
          MessageSubject.SESSION_STATUS,
          null,
          Map.of("id", sessionId, "status", status));
    }
    return sessionDto;
  }
}
