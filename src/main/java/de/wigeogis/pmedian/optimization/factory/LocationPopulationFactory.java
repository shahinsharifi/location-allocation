package de.wigeogis.pmedian.optimization.factory;

import de.wigeogis.pmedian.optimization.model.BasicGenome;
import de.wigeogis.pmedian.optimization.utils.LocationUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.uncommons.watchmaker.framework.factories.AbstractCandidateFactory;

@Log4j2
@AllArgsConstructor
public class LocationPopulationFactory<T> extends AbstractCandidateFactory<List<T>> {

  private final List<String> regions;
  private final ConcurrentHashMap<String, ConcurrentHashMap<String, Double>> costMatrix;
  private final Integer numberOfFacilities;
  private final Double maxTravelTime;

  public List<T> generateRandomCandidate(Random rng) {
    List<T> candidate = new ArrayList<>();

    List<String> candidateSeed =
        LocationUtils.findFacilityCandidates(
            numberOfFacilities, maxTravelTime, regions, rng, costMatrix);

    for (String facility : candidateSeed) {
      candidate.add((T) new BasicGenome(facility));
    }
    return candidate;
  }
}
