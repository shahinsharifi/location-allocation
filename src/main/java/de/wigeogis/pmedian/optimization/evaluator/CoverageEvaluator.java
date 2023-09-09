package de.wigeogis.pmedian.optimization.evaluator;


import de.wigeogis.pmedian.optimization.model.BasicGenome;
import de.wigeogis.pmedian.optimization.utils.LocationUtils;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.uncommons.watchmaker.framework.FitnessEvaluator;

public class CoverageEvaluator implements FitnessEvaluator<List<BasicGenome>> {


  private final List<String> demands;
  private final ConcurrentHashMap<String, ConcurrentHashMap<String, Double>> costMatrix;

  public CoverageEvaluator(
      List<String> demands, ConcurrentHashMap<String, ConcurrentHashMap<String, Double>> costMatrix) {
    this.costMatrix = costMatrix;
    this.demands = demands;
  }

  @Override
  public double getFitness(List<BasicGenome> chromosome, List<? extends List<BasicGenome>> list) {
    List<String> facilities = chromosome.stream().map(BasicGenome::getRegionId).toList();
	  return LocationUtils.calculateUncoveredRegions(facilities, demands, costMatrix);
  }


    @Override
  public boolean isNatural() {
    return false;
  }
}
