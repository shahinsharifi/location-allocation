package de.wigeogis.pmedian.optimizer.evaluator;

import com.google.common.collect.ImmutableTable;
import de.wigeogis.pmedian.database.dto.RegionDto;
import de.wigeogis.pmedian.database.dto.RegionDto;
import de.wigeogis.pmedian.database.dto.RegionDto;
import de.wigeogis.pmedian.optimizer.model.BasicGenome;
import de.wigeogis.pmedian.optimizer.util.FacilityCandidateUtil;
import java.util.List;
import java.util.Map;
import org.uncommons.watchmaker.framework.FitnessEvaluator;

public class CoverageEvaluator implements FitnessEvaluator<List<BasicGenome>> {


  private final List<RegionDto> demands;
  private final ImmutableTable<String, String, Double> distanceMatrix;

  public CoverageEvaluator(
      List<RegionDto> demands, ImmutableTable<String, String, Double> distanceMatrix) {
    this.distanceMatrix = distanceMatrix;
    this.demands = demands;
  }

  @Override
  public double getFitness(List<BasicGenome> chromosome, List<? extends List<BasicGenome>> list) {
    List<RegionDto> facilities = chromosome.stream().map(BasicGenome::getRegionDto).toList();
    Map<RegionDto, RegionDto> allocated = FacilityCandidateUtil.findNearestFacilities(demands, facilities, distanceMatrix);
    return demands.size() - allocated.size();
  }


    @Override
  public boolean isNatural() {
    return false;
  }
}
