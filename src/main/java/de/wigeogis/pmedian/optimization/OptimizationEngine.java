package de.wigeogis.pmedian.optimization;


import de.wigeogis.pmedian.database.dto.AllocationDto;
import de.wigeogis.pmedian.database.dto.SessionDto;
import de.wigeogis.pmedian.database.entity.Session;
import de.wigeogis.pmedian.optimization.evaluator.CoverageEvaluator;
import de.wigeogis.pmedian.optimization.evaluator.TravelCostEvaluator;
import de.wigeogis.pmedian.optimization.factory.AllocationOperationFactory;
import de.wigeogis.pmedian.optimization.factory.LocationOperationFactory;
import de.wigeogis.pmedian.optimization.factory.AllocationPopulationFactory;
import de.wigeogis.pmedian.optimization.factory.LocationPopulationFactory;
import de.wigeogis.pmedian.optimization.logger.EvolutionObserver;
import de.wigeogis.pmedian.optimization.model.BasicGenome;


import de.wigeogis.pmedian.optimization.utils.LocationUtils;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.nd4j.common.primitives.Pair;
import org.springframework.stereotype.Component;

import org.uncommons.maths.number.AdjustableNumberGenerator;
import org.uncommons.maths.random.MersenneTwisterRNG;
import org.uncommons.maths.random.Probability;

import org.uncommons.watchmaker.framework.CandidateFactory;
import org.uncommons.watchmaker.framework.EvolutionaryOperator;
import org.uncommons.watchmaker.framework.GenerationalEvolutionEngine;
import org.uncommons.watchmaker.framework.SelectionStrategy;
import org.uncommons.watchmaker.framework.selection.TournamentSelection;
import org.uncommons.watchmaker.framework.termination.Stagnation;

import java.util.*;
import org.uncommons.watchmaker.framework.termination.TargetFitness;
import org.uncommons.watchmaker.framework.termination.UserAbort;

@Log4j2
public class OptimizationEngine {


  private final Session session;

  private final ConcurrentHashMap<UUID, UserAbort> locationAbortSignalMap = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<UUID, UserAbort> allocationAbortSignalMap = new ConcurrentHashMap<>();

  private final SelectionStrategy<Object> selection = new TournamentSelection(
      new AdjustableNumberGenerator<>(new Probability(0.9d)));


  public OptimizationEngine(Session session) {
    this.session = session;
  }

  public List<AllocationDto> evolve(List<String> regions, ConcurrentHashMap<String, ConcurrentHashMap<String, Double>> costMatrix) {

    List<AllocationDto> allocationResult = new ArrayList<>();

    //create distance matrix

    Random rng = createRNG();
    long start = System.currentTimeMillis();

    Pair<Integer, List<String>> initData = initializeData(session, regions, costMatrix, rng);
    int numberOfFacilities = initData.getFirst();
    List<String> demands = initData.getSecond();

    List<BasicGenome> locationResult = runLocationEngine(session, regions, costMatrix, numberOfFacilities, rng,
        demands);

    if (locationResultIsValid(locationResult)) {
      allocationResult = processLocationResults(session, demands, costMatrix, rng, locationResult);
    }

    recordProcedureDuration(start);

    return allocationResult;
  }

  private Pair<Integer, List<String>> initializeData(Session session, List<String> demands,
      ConcurrentHashMap<String, ConcurrentHashMap<String, Double>> costMatrix, Random rng) {
    int numberOfFacilities = 0;
    if (session.getNumberOfFacilities() != null) {
      numberOfFacilities = session.getNumberOfFacilities();
    }

    log.info("Initializing demand regions ...");

    List<String> initialSeed = LocationUtils.findFacilityCandidates(
        numberOfFacilities, session.getMaxTravelTimeInMinutes(), demands, rng, costMatrix);

    numberOfFacilities = Math.min(numberOfFacilities, initialSeed.size());

    log.info("Initial seed with size '" + numberOfFacilities + "' has been created...");

    int uncoveredRegions = LocationUtils.calculateUncoveredRegions(demands, initialSeed, costMatrix);
    log.info("Uncovered regions: " + uncoveredRegions);

    return new Pair<>(numberOfFacilities, demands);
  }

  private List<AllocationDto> processLocationResults(Session session, List<String> demands,
      ConcurrentHashMap<String, ConcurrentHashMap<String, Double>> costMatrix, Random rng, List<BasicGenome> locationResult) {
    List<BasicGenome> allocationResult = runAllocationEngine(session, demands, costMatrix, rng, locationResult);
    return processAllocationResults(session.getId(), demands, costMatrix, allocationResult);
  }

  private List<BasicGenome> runLocationEngine(Session session, List<String> regions,
      ConcurrentHashMap<String, ConcurrentHashMap<String, Double>> costMatrix, int numberOfFacilities, Random rng,
      List<String> demands) {

    log.info("Running location engine...");
    long start = System.currentTimeMillis();

    UserAbort locationAbortSignal = locationAbortSignalMap.computeIfAbsent(session.getId(), k -> new UserAbort());

    CandidateFactory<List<BasicGenome>> locationCandidateFactory = new LocationPopulationFactory<>(regions,
        costMatrix, numberOfFacilities, session.getMaxTravelTimeInMinutes());

    Double initialMutationRate = 0.001;

    LocationOperationFactory locationOperationFactory = new LocationOperationFactory(initialMutationRate);

    EvolutionaryOperator<List<BasicGenome>> locationPipeline = locationOperationFactory
        .createEvolutionPipeline(demands, costMatrix);

    CoverageEvaluator coverageEvaluator = new CoverageEvaluator(demands, costMatrix);

    GenerationalEvolutionEngine<List<BasicGenome>> locationEngine = new GenerationalEvolutionEngine<>(
        locationCandidateFactory, locationPipeline, coverageEvaluator, selection, rng);

    locationEngine.setSingleThreaded(false);
    locationEngine.addEvolutionObserver(new EvolutionObserver(initialMutationRate));

    List<BasicGenome> locationResult = locationEngine.evolve(20, 12, locationAbortSignal,
        new TargetFitness(0, false), new Stagnation(5000, false));

    long end = System.currentTimeMillis();
    log.info("Location engine finished in {} seconds", (end - start) / 1000);

    if (locationResult == null || locationResult.isEmpty()) {
      log.info("Location engine did not find a solution, exiting...");
    }

    return locationResult != null ? Collections.unmodifiableList(locationResult) : Collections.emptyList();
  }

  private List<BasicGenome> runAllocationEngine(Session session, List<String> demands,
      ConcurrentHashMap<String, ConcurrentHashMap<String, Double>> costMatrix, Random rng, List<BasicGenome> locationResult) {

    log.info("The allocation process started ...");
    double start = System.currentTimeMillis();

    UserAbort allocationAbortSignal = allocationAbortSignalMap.computeIfAbsent(session.getId(),
        k -> new UserAbort());

    Double initialMutationRate = 0.001;
    AllocationOperationFactory allocationOperationFactory = new AllocationOperationFactory(initialMutationRate);

    CandidateFactory<List<BasicGenome>> allocationCandidateFactory = new AllocationPopulationFactory<>(locationResult.stream().map(BasicGenome::getRegionId).toList());

    EvolutionaryOperator<List<BasicGenome>> allocationPipeline = allocationOperationFactory
        .createEvolutionPipeline(demands, costMatrix);
    TravelCostEvaluator travelCostEvaluator = new TravelCostEvaluator(demands, costMatrix);
    GenerationalEvolutionEngine<List<BasicGenome>> allocationEngine = new GenerationalEvolutionEngine<>(
        allocationCandidateFactory, allocationPipeline, travelCostEvaluator, selection, rng);

    allocationEngine.setSingleThreaded(false);
    allocationEngine.addEvolutionObserver(new EvolutionObserver(initialMutationRate));


    List<BasicGenome> resultAllocation = allocationEngine.evolve(20, 12, allocationAbortSignal,
        new Stagnation(2000, false));

    double end = System.currentTimeMillis();
    log.info("Location engine finished in {} seconds", (end - start) / 1000);

    if (resultAllocation == null || resultAllocation.isEmpty()) {
      log.info("Location engine did not find a solution, exiting...");
    }

    return resultAllocation != null ? Collections.unmodifiableList(resultAllocation) : Collections.emptyList();
  }


  private List<AllocationDto> processAllocationResults(UUID sessionId, List<String> demands,
      ConcurrentHashMap<String, ConcurrentHashMap<String, Double>> costMatrix, List<BasicGenome> allocationResult) {

    List<String> facilityCodes = allocationResult.stream().map(BasicGenome::getRegionId).toList();

    Map<String, String> coveredDemands = LocationUtils.findNearestFacilities(demands, facilityCodes,
        costMatrix);
    log.info(allocationResult.size() + " demands points have been allocated by the " + facilityCodes.size()
        + " facilities ...");

	  return coveredDemands.keySet().stream()
        .map(demand -> new AllocationDto().setSessionId(sessionId).setRegionId(demand)
            .setFacilityRegionId(coveredDemands.get(demand)))
        .collect(Collectors.toList());

  }

  private boolean locationResultIsValid(List<BasicGenome> locationResult) {
    return locationResult != null && !locationResult.isEmpty();
  }

  private void recordProcedureDuration(long start) {
    long end = System.currentTimeMillis();
    log.info("Procedure has been finished in {} seconds", (end - start) / 1000);
  }

  private Random createRNG() {
    return new MersenneTwisterRNG();
  }

}
