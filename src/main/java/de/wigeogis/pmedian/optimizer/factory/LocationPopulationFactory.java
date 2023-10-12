package de.wigeogis.pmedian.optimizer.factory;

import de.wigeogis.pmedian.database.dto.RegionDto;
import de.wigeogis.pmedian.optimizer.model.BasicGenome;
import de.wigeogis.pmedian.optimizer.util.CostEvaluatorUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.uncommons.watchmaker.framework.factories.AbstractCandidateFactory;

@Log4j2
@AllArgsConstructor
public class LocationPopulationFactory<T> extends AbstractCandidateFactory<List<T>> {

  private final Integer numberOfFacilities;
  private final CostEvaluatorUtils costEvaluatorUtils;

  public List<T> generateRandomCandidate(Random rng) {
    List<T> candidate = new ArrayList<>();

    List<RegionDto> candidateSeed = costEvaluatorUtils.findFacilityCandidates(numberOfFacilities);

    for (RegionDto facility : candidateSeed) {
      candidate.add((T) new BasicGenome(facility.getId()));
    }
    return candidate;
  }
}
