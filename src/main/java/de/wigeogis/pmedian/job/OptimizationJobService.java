package de.wigeogis.pmedian.job;

import com.google.common.collect.ImmutableTable;
import de.wigeogis.pmedian.database.dto.AllocationDto;
import de.wigeogis.pmedian.database.dto.RegionDto;
import de.wigeogis.pmedian.database.dto.SessionDto;
import de.wigeogis.pmedian.database.entity.Session;
import de.wigeogis.pmedian.database.service.AllocationService;
import de.wigeogis.pmedian.database.service.RegionService;
import de.wigeogis.pmedian.database.service.SessionService;
import de.wigeogis.pmedian.database.service.TravelCostService;
import de.wigeogis.pmedian.optimizer.OptimizationEngine;
import io.github.resilience4j.bulkhead.ThreadPoolBulkhead;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.support.ReferenceJobFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
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
    List<RegionDto> regions = regionService.getRegionsByRegionCodePattern("^DE-(8[0-9]{4}|9[0-8][0-9]{3})$");
    ImmutableTable<String, String, Double> distanceMatrix = costService.getCostMatrix();
    Thread execution =
        new Thread(
            () -> {
              try {

                List<AllocationDto> allocations = optimizationEngine.evolve(sessionDto, regions, distanceMatrix);
                allocationService.insertAll(allocations);
                allocationService.insertAllAsync(allocations);
              } catch (Exception e) {
                e.printStackTrace();
              }
            });
    execution.start();
    sessionDto.setRunning(true);
    return sessionDto;
  }
}
