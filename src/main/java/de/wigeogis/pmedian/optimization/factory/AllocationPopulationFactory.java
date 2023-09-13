package de.wigeogis.pmedian.optimization.factory;

import de.wigeogis.pmedian.optimization.model.BasicGenome;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.uncommons.watchmaker.framework.factories.AbstractCandidateFactory;

@Log4j2
@AllArgsConstructor
public class AllocationPopulationFactory<T> extends AbstractCandidateFactory<List<T>> {

  private final List<String> candidateSeed;

  public List<T> generateRandomCandidate(Random rng) {

    List<T> candidate = new ArrayList<>();
    for (String facility : candidateSeed) {
      candidate.add((T) new BasicGenome(facility));
    }

    return candidate;
  }
}
