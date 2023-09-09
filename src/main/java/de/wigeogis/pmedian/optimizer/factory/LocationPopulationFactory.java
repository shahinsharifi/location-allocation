package de.wigeogis.pmedian.optimizer.factory;

import com.google.common.collect.ImmutableTable;
import de.wigeogis.pmedian.database.dto.RegionDto;
import de.wigeogis.pmedian.optimizer.model.BasicGenome;
import de.wigeogis.pmedian.optimizer.util.FacilityCandidateUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.uncommons.watchmaker.framework.factories.AbstractCandidateFactory;

@Log4j2
@AllArgsConstructor
public class LocationPopulationFactory<T> extends AbstractCandidateFactory<List<T>> {

  private final List<RegionDto> regions;
  private final ImmutableTable<String, String, Double> distanceMatrix;
  private final Integer numberOfFacilities;
  private final Double maxTravelTime;

  public List<T> generateRandomCandidate(Random rng) {
    List<T> candidate = new ArrayList<>();

    List<RegionDto> candidateSeed =
        FacilityCandidateUtil.findFacilityCandidates(
            regions, distanceMatrix, numberOfFacilities, maxTravelTime, rng);

    for (RegionDto facility : candidateSeed) {
      candidate.add((T) new BasicGenome(facility.getId()));
    }
    return candidate;
  }
}
