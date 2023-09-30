package de.wigeogis.pmedian.optimizer.evaluator;

import com.google.common.collect.ImmutableTable;
import de.wigeogis.pmedian.database.dto.RegionDto;
import de.wigeogis.pmedian.optimizer.model.BasicGenome;
import de.wigeogis.pmedian.optimizer.util.CostEvaluatorUtils;
import de.wigeogis.pmedian.optimizer.util.FacilityCandidateUtil;
import de.wigeogis.pmedian.websocket.NotificationService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.LoggerFactory;
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
