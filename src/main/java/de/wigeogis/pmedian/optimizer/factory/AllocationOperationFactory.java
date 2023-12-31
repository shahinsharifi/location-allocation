package de.wigeogis.pmedian.optimizer.factory;

import de.wigeogis.pmedian.database.dto.RegionDto;
import de.wigeogis.pmedian.optimizer.model.BasicGenome;
import de.wigeogis.pmedian.optimizer.operation.allocation.AllocationCrossOver;
import de.wigeogis.pmedian.optimizer.operation.allocation.AllocationMutation;
import de.wigeogis.pmedian.optimizer.util.CostEvaluatorUtils;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.uncommons.maths.number.AdjustableNumberGenerator;
import org.uncommons.maths.number.ConstantGenerator;
import org.uncommons.maths.random.Probability;
import org.uncommons.watchmaker.framework.EvolutionaryOperator;
import org.uncommons.watchmaker.framework.operators.EvolutionPipeline;
import org.uncommons.watchmaker.framework.operators.ListOperator;

@Log4j2
@RequiredArgsConstructor
public class AllocationOperationFactory {

  private final UUID sessionId;

  public EvolutionaryOperator<List<BasicGenome>> createEvolutionPipeline(
      List<RegionDto> demands, CostEvaluatorUtils costEvaluatorUtils) {

    List<EvolutionaryOperator<List<BasicGenome>>> operators = new LinkedList<>();

    // Installing crossover operator
    operators.add(
        new AllocationCrossOver<>(
            new ConstantGenerator<>(1), new AdjustableNumberGenerator<>(new Probability(0.9))));

    // Installing mutation operator
    operators.add(
        new ListOperator<>(new AllocationMutation(sessionId, demands, costEvaluatorUtils)));

    return new EvolutionPipeline<>(operators);
  }
}
