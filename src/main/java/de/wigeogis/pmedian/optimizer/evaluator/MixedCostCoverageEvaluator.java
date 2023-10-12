package de.wigeogis.pmedian.optimizer.evaluator;

import de.wigeogis.pmedian.optimizer.model.BasicGenome;
import de.wigeogis.pmedian.optimizer.util.CostEvaluatorUtils;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.uncommons.watchmaker.framework.FitnessEvaluator;

@Log4j2
public class MixedCostCoverageEvaluator implements FitnessEvaluator<List<BasicGenome>> {

  private final CostEvaluatorUtils costEvaluatorUtils;

  public MixedCostCoverageEvaluator(CostEvaluatorUtils costEvaluatorUtils) {
    this.costEvaluatorUtils = costEvaluatorUtils;
  }

  @Override
  public double getFitness(List<BasicGenome> chromosome, List<? extends List<BasicGenome>> list) {
    double fitnessValue = 0;

    fitnessValue = this.costEvaluatorUtils.calculateUncoveredAndAboveLimitRegions(
        chromosome.stream().map(BasicGenome::getRegionId).toList()
    );

    return fitnessValue;
  }

  @Override
  public boolean isNatural() {
    return false;
  }
}
