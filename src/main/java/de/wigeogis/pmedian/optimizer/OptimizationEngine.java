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
import de.wigeogis.pmedian.optimizer.util.CostEvaluatorUtils;
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

  public List<AllocationDto> evolve(
      SessionDto session,
      List<AllocationDto> allocationDTOs,
      ImmutableTable<String, String, Double> distanceMatrix,
      UserAbort abortSignal) {

    Random rng = new XORShiftRNG();

    List<RegionDto> regions =
        allocationDTOs.stream().map(AllocationDto::toRegionDto).collect(Collectors.toList());

    CostEvaluatorUtils costEvaluatorUtils =
        new CostEvaluatorUtils(
            regions.stream().map(RegionDto::getId).toList(),
            distanceMatrix,
            session.getMaxTravelTimeInMinutes());

    ElapsedTime elapsedTime = new ElapsedTime(session.getMaxRunningTimeInMinutes() * 60000);

    ConcurrentHashMap<Integer, Double> progress = new ConcurrentHashMap<>();

    int numberOfFacilities = 0;
    if (session.getNumberOfFacilities() != null) {
      numberOfFacilities = session.getNumberOfFacilities();
    }

    log.info("Initializing demand regions ...");

    List<RegionDto> initialSeed = costEvaluatorUtils.findFacilityCandidates(numberOfFacilities);

    if (initialSeed.size() > numberOfFacilities) numberOfFacilities = initialSeed.size();

    log.info("Initial seed with size '" + numberOfFacilities + "' has been created...");

    // Configuration of selection strategy
    SelectionStrategy<Object> selection =
        new TournamentSelection(new AdjustableNumberGenerator<>(new Probability(0.9d)));

    int uncoveredRegions =
        costEvaluatorUtils.calculateUncoveredAndAboveLimitRegions(
            initialSeed.stream().map(RegionDto::getId).toList());
    //    int uncoveredRegions =
    //        FacilityCandidateUtil.calculateUncoveredDemands(regions, initialSeed, distanceMatrix);
    log.info("Uncovered regions: " + uncoveredRegions);

    List<BasicGenome> locationResult = null;

    CandidateFactory<List<BasicGenome>> locationCandidateFactory =
        new LocationPopulationFactory<>(
            regions, distanceMatrix, numberOfFacilities, session.getMaxTravelTimeInMinutes());

    EvolutionaryOperator<List<BasicGenome>> locationPipeline =
        new LocationOperationFactory(session.getId())
            .createEvolutionPipeline(regions, distanceMatrix, costEvaluatorUtils);
    CoverageEvaluator coverageEvaluator =
        new CoverageEvaluator(regions, distanceMatrix, session.getMaxTravelTimeInMinutes());
    GenerationalEvolutionEngine<List<BasicGenome>> locationEngine =
        new GenerationalEvolutionEngine<>(
            locationCandidateFactory, locationPipeline, coverageEvaluator, selection, rng);
    locationEngine.addEvolutionObserver(
        new EvolutionLogger(
            session.getId(), costEvaluatorUtils, eventPublisher, notificationService, progress));
    locationEngine.setSingleThreaded(true);

    log.info("Running location engine...");
    long start = System.currentTimeMillis();

    locationResult =
        locationEngine.evolve(
            12,
            7,
            abortSignal,
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
            .createEvolutionPipeline(regions, distanceMatrix, costEvaluatorUtils);
    TravelCostEvaluator travelCostEvaluator =
        new TravelCostEvaluator(regions, distanceMatrix, session.getMaxTravelTimeInMinutes());
    GenerationalEvolutionEngine<List<BasicGenome>> allocationEngine =
        new GenerationalEvolutionEngine<>(
            allocationCandidateFactory, allocationPipeline, travelCostEvaluator, selection, rng);
    allocationEngine.addEvolutionObserver(
        new EvolutionLogger(
            session.getId(), costEvaluatorUtils, eventPublisher, notificationService, progress));
    allocationEngine.setSingleThreaded(true);

    // Running allocation engine
    start = System.currentTimeMillis();

    List<BasicGenome> resultAllocation =
        allocationEngine.evolve(12, 7, abortSignal, new Stagnation(2000, false), elapsedTime);
    end = System.currentTimeMillis();

    List<RegionDto> facilitiesCodes =
        resultAllocation.stream().map(BasicGenome::getRegionDto).toList();

    List<AllocationDto> optimizedAllocations =
        FacilityCandidateUtil.findNearestFacilitiesForDemands(
            allocationDTOs, facilitiesCodes, distanceMatrix);

    log.info("Allocated demands are saved into the database ...");

    log.info("End of processing ...");
    log.info("Exe. time: " + (end - start) / 1000);

    return optimizedAllocations;
  }
}
