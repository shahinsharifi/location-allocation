package de.wigeogis.pmedian.optimization.factory;


import de.wigeogis.pmedian.optimization.model.BasicGenome;
import de.wigeogis.pmedian.optimization.operation.allocation.AllocationCrossOver;
import de.wigeogis.pmedian.optimization.operation.location.CoverageMutation;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.uncommons.maths.number.AdjustableNumberGenerator;
import org.uncommons.maths.number.ConstantGenerator;
import org.uncommons.maths.random.Probability;
import org.uncommons.watchmaker.framework.EvolutionaryOperator;
import org.uncommons.watchmaker.framework.operators.EvolutionPipeline;
import org.uncommons.watchmaker.framework.operators.ListOperator;

@Log4j2
@AllArgsConstructor
public class LocationOperationFactory {

  private Double mutationRate;

  public EvolutionaryOperator<List<BasicGenome>> createEvolutionPipeline(List<String> demands, ConcurrentHashMap<String, ConcurrentHashMap<String, Double>> costMatrix) {


    List<EvolutionaryOperator<List<BasicGenome>>> operators = new LinkedList<>();

    // Installing crossover operator
    operators.add(
        new AllocationCrossOver<>(
            new ConstantGenerator<>(1), new AdjustableNumberGenerator<>(new Probability(0.9))));


    // Installing mutation operator
    CoverageMutation coverageMutation = new CoverageMutation(
        demands,
        costMatrix,
        mutationRate);

    operators.add(
        new ListOperator<>(
            coverageMutation));

    return new EvolutionPipeline<>(operators);
  }
}
