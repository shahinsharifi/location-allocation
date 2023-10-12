package de.wigeogis.pmedian.optimizer.evaluator;

import de.wigeogis.pmedian.optimizer.model.BasicGenome;
import de.wigeogis.pmedian.optimizer.util.CostEvaluatorUtils;
import java.util.List;
import org.uncommons.watchmaker.framework.FitnessEvaluator;

public class CoverageEvaluator implements FitnessEvaluator<List<BasicGenome>> {

  private final CostEvaluatorUtils costEvaluatorUtils;

  public CoverageEvaluator(CostEvaluatorUtils costEvaluatorUtils) {
    this.costEvaluatorUtils = costEvaluatorUtils;
  }

  @Override
  public double getFitness(List<BasicGenome> chromosome, List<? extends List<BasicGenome>> list) {
    List<String> facilities = chromosome.stream().map(BasicGenome::getRegionId).toList();
    return this.costEvaluatorUtils.calculateUncoveredAndAboveLimitRegions(facilities);
  }

  @Override
  public boolean isNatural() {
    return false;
  }
}
