package de.wigeogis.pmedian.optimization.operation.location;

import java.util.*;
import org.uncommons.maths.number.NumberGenerator;
import org.uncommons.maths.random.Probability;
import org.uncommons.watchmaker.framework.operators.AbstractCrossover;

public class CoverageCrossOver<T> extends AbstractCrossover<List<T>> {

  public CoverageCrossOver(
      NumberGenerator<Integer> crossoverPointsVariable,
      NumberGenerator<Probability> crossoverProbabilityVariable) {
    super(crossoverPointsVariable, crossoverProbabilityVariable);
  }

  @Override
  protected List<List<T>> mate(
      List<T> parent1, List<T> parent2, int numberOfCrossoverPoints, Random rng) {

    for (int i = 0; i < numberOfCrossoverPoints; i++) {

      int max = Math.min(parent1.size(), parent2.size());
      if (max > 1) {
        int crossoverIndex = (1 + rng.nextInt(max - 1));
        for (int j = 0; j < crossoverIndex; j++) {
          T temp = parent1.get(j);
          parent1.set(j, parent2.get(j));
          parent2.set(j, temp);
        }
      }
    }

    List<List<T>> result = new ArrayList<List<T>>(2);
    result.add(parent1);
    result.add(parent2);

    return result;
  }

  private static class CustomComparator<T extends Comparable<? super T>> implements Comparator<T> {
    @Override
    public int compare(T a, T b) {
      return a.compareTo(b);
    }
  }
}
