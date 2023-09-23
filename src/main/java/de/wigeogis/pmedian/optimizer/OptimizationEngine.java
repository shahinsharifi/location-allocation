package de.wigeogis.pmedian.optimizer;

import com.google.common.collect.ImmutableTable;
import de.wigeogis.pmedian.database.dto.AllocationDto;
import de.wigeogis.pmedian.database.dto.RegionDto;
import de.wigeogis.pmedian.database.dto.SessionDto;
import de.wigeogis.pmedian.optimizer.evaluator.CoverageEvaluator;
import de.wigeogis.pmedian.optimizer.evaluator.TravelCostEvaluator;
import de.wigeogis.pmedian.optimizer.factory.AllocationOperationFactory;
import de.wigeogis.pmedian.optimizer.factory.AllocationPopulationFactory;
import de.wigeogis.pmedian.optimizer.factory.LocationOperationFactory;
import de.wigeogis.pmedian.optimizer.factory.LocationPopulationFactory;
import de.wigeogis.pmedian.optimizer.logger.EvolutionLogger;
import de.wigeogis.pmedian.optimizer.model.BasicGenome;
import de.wigeogis.pmedian.optimizer.util.FacilityCandidateUtil;
import de.wigeogis.pmedian.websocket.NotificationService;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.uncommons.maths.number.AdjustableNumberGenerator;
import org.uncommons.maths.random.Probability;
import org.uncommons.maths.random.XORShiftRNG;
import org.uncommons.watchmaker.framework.CandidateFactory;
import org.uncommons.watchmaker.framework.EvolutionaryOperator;
import org.uncommons.watchmaker.framework.GenerationalEvolutionEngine;
import org.uncommons.watchmaker.framework.SelectionStrategy;
import org.uncommons.watchmaker.framework.selection.TournamentSelection;
import org.uncommons.watchmaker.framework.termination.ElapsedTime;
import org.uncommons.watchmaker.framework.termination.Stagnation;
import org.uncommons.watchmaker.framework.termination.TargetFitness;
import org.uncommons.watchmaker.framework.termination.UserAbort;

@Log4j2
@Service
@RequiredArgsConstructor
public class OptimizationEngine {

  private final ApplicationEventPublisher eventPublisher;
  private final NotificationService notificationService;

  private final ConcurrentHashMap<UUID, UserAbort> locationAbortSignalMap =
      new ConcurrentHashMap<>();

  private final ConcurrentHashMap<UUID, UserAbort> allocationAbortSignalMap =
      new ConcurrentHashMap<>();

  public List<AllocationDto> evolve(
      SessionDto session,
      List<AllocationDto> allocationDtos,
      ImmutableTable<String, String, Double> distanceMatrix) {

    List<RegionDto> regions =
        allocationDtos.stream().map(AllocationDto::toRegionDto).collect(Collectors.toList());

    ElapsedTime elapsedTime = new ElapsedTime(session.getMaxRunningTimeInMinutes() * 60000);

    Random rng = new XORShiftRNG();

    int numberOfFacilities = 0;
    if (session.getNumberOfFacilities() != null) {
      numberOfFacilities = session.getNumberOfFacilities();
    }

    log.info("Initializing demand regions ...");

    List<RegionDto> initialSeed =
        FacilityCandidateUtil.findFacilityCandidates(
            regions, distanceMatrix, numberOfFacilities, session.getMaxTravelTimeInMinutes(), rng);

//    List<RegionDto> initialSeed =
//        FacilityCandidateUtil.findMinimumFacilityCandidates(
//            regions, distanceMatrix, session.getMaxTravelTimeInMinutes());

    if(initialSeed.size() > numberOfFacilities)
      numberOfFacilities = initialSeed.size();

    log.info("Initial seed with size '" + numberOfFacilities + "' has been created...");

    // Configuration of selection strategy
    SelectionStrategy<Object> selection =
        new TournamentSelection(new AdjustableNumberGenerator<>(new Probability(0.9d)));

    // first check if the initial seed has 100% coverage, if not, run the location engine
    int uncoveredRegions =
        FacilityCandidateUtil.calculateUncoveredDemands(regions, initialSeed, distanceMatrix);
    log.info("Uncovered regions: " + uncoveredRegions);

    List<BasicGenome> locationResult = null;

    CandidateFactory<List<BasicGenome>> locationCandidateFactory =
        new LocationPopulationFactory<>(
            regions, distanceMatrix, numberOfFacilities, session.getMaxTravelTimeInMinutes());

    EvolutionaryOperator<List<BasicGenome>> locationPipeline =
        new LocationOperationFactory(session.getId())
            .createEvolutionPipeline(regions, distanceMatrix);
    CoverageEvaluator coverageEvaluator =
        new CoverageEvaluator(session.getId(), regions, distanceMatrix, notificationService);
    GenerationalEvolutionEngine<List<BasicGenome>> locationEngine =
        new GenerationalEvolutionEngine<>(
            locationCandidateFactory, locationPipeline, coverageEvaluator, selection, rng);
    locationEngine.addEvolutionObserver(
        new EvolutionLogger(
            session.getId(), allocationDtos, distanceMatrix, eventPublisher, notificationService));
    locationEngine.setSingleThreaded(true);

    log.info("Running location engine...");
    long start = System.currentTimeMillis();

    UserAbort locationAbortSignal;
    if (locationAbortSignalMap.containsKey(session.getId())) {
      locationAbortSignal = locationAbortSignalMap.get(session.getId());
    } else {
      locationAbortSignal = new UserAbort();
      locationAbortSignalMap.put(session.getId(), locationAbortSignal);
    }

    locationResult =
        locationEngine.evolve(
            12,
            7,
            locationAbortSignal,
            new TargetFitness(0, false),
            new Stagnation(2000, false),
            elapsedTime);

    long end = System.currentTimeMillis();
    log.info("Location engine finished in " + (end - start) / 1000 + " seconds");

    if (locationResult == null || locationResult.isEmpty()) {
      log.error("Location engine did not find a solution, exiting...");
      return null;
    }

    CandidateFactory<List<BasicGenome>> allocationCandidateFactory =
        new AllocationPopulationFactory<>(
            locationResult.stream().map(BasicGenome::getRegionDto).toList());

    EvolutionaryOperator<List<BasicGenome>> allocationPipeline =
        new AllocationOperationFactory(session.getId())
            .createEvolutionPipeline(regions, distanceMatrix);
    TravelCostEvaluator travelCostEvaluator =
        new TravelCostEvaluator(session.getId(), regions, distanceMatrix, notificationService);
    GenerationalEvolutionEngine<List<BasicGenome>> allocationEngine =
        new GenerationalEvolutionEngine<>(
            allocationCandidateFactory, allocationPipeline, travelCostEvaluator, selection, rng);
    allocationEngine.addEvolutionObserver(
        new EvolutionLogger(
            session.getId(), allocationDtos, distanceMatrix, eventPublisher, notificationService));
    allocationEngine.setSingleThreaded(true);

    // Running allocation engine
    start = System.currentTimeMillis();

    UserAbort allocationAbortSignal;
    if (allocationAbortSignalMap.containsKey(session.getId())) {
      allocationAbortSignal = allocationAbortSignalMap.get(session.getId());
    } else {
      allocationAbortSignal = new UserAbort();
      allocationAbortSignalMap.put(session.getId(), locationAbortSignal);
    }

    List<BasicGenome> resultAllocation =
        allocationEngine.evolve(
            12, 7, allocationAbortSignal, new Stagnation(2000, false), elapsedTime);
    end = System.currentTimeMillis();


    List<RegionDto> facilitiesCodes =
        resultAllocation.stream().map(BasicGenome::getRegionDto).toList();

    //        Map<RegionDto, RegionDto> coveredDemands =
    //            FacilityCandidateUtil.findNearestFacilities(regions, facilitiesCodes,
    // distanceMatrix);
    //        log.info(
    //            resultAllocation.size()
    //                + " demands points have been allocated by the "
    //                + facilitiesCodes.size()
    //                + " facilities ...");
    //
    //        List<AllocationDto> allocations =
    //            coveredDemands.keySet().stream()
    //                .map(
    //                    demandDto ->
    //                        new AllocationDto()
    //                            .setSessionId(session.getId())
    //                            .setRegionId(demandDto.getId())
    //                            .setFacilityRegionId(coveredDemands.get(demandDto).getId())
    //                            .setTravelCost(distanceMatrix.get(demandDto.getId(),
    //     coveredDemands.get(demandDto).getId())))
    //                .toList();

    List<AllocationDto> optimizedAllocations =
        FacilityCandidateUtil.findNearestFacilitiesForDemands(
            allocationDtos, facilitiesCodes, distanceMatrix);

    log.info("Allocated demands are saved into the database ...");

    log.info("End of processing ...");
    log.info("Exe. time: " + (end - start) / 1000);

    return optimizedAllocations;
  }

  public void abortLocationEngine(UUID sessionId) {
    if (locationAbortSignalMap.containsKey(sessionId)) {
      locationAbortSignalMap.get(sessionId).abort();
    }
  }

  public void abortAllocationEngine(UUID sessionId) {
    if (allocationAbortSignalMap.containsKey(sessionId)) {
      allocationAbortSignalMap.get(sessionId).abort();
    }
  }
}
