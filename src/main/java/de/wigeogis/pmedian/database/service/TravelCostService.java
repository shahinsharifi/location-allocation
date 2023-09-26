package de.wigeogis.pmedian.database.service;

import com.google.common.collect.ImmutableTable;
import de.wigeogis.pmedian.database.dto.SessionDto;
import de.wigeogis.pmedian.database.entity.TravelCost;
import de.wigeogis.pmedian.database.repository.TravelCostRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class TravelCostService {

  private final TravelCostRepository repository;

  @lombok.Getter private ImmutableTable<String, String, Double> costMatrix;

  @lombok.Getter
  private ConcurrentHashMap<String, ConcurrentHashMap<String, Double>> sparseCostMatrix;

  public TravelCostService(TravelCostRepository repository) {
    this.repository = repository;
  }


  private void preloadTravelCostMatrices() {
    log.info("Please wait, the travel cost matrix is being loaded ...");

    List<TravelCost> travelCosts = this.getAll();
    //        this.getByStartAndEndRegExp(
    //            "^DE-(8[0-9]{4}|9[0-8][0-9]{3})$", "^DE-(8[0-9]{4}|9[0-8][0-9]{3})$");

    costMatrix =
        travelCosts.stream()
            .collect(
                ImmutableTable.toImmutableTable(
                    TravelCost::getStartRegionId,
                    TravelCost::getEndRegionId,
                    TravelCost::getTravelTimeInMinutes));

    sparseCostMatrix = new ConcurrentHashMap<>();
    travelCosts.forEach(
        travelCost -> {
          sparseCostMatrix
              .computeIfAbsent(travelCost.getStartRegionId(), k -> new ConcurrentHashMap<>())
              .put(travelCost.getEndRegionId(), travelCost.getTravelTimeInMinutes());
        });

    log.info("Travel cost matrix is loaded ...");
  }

  public void insert(TravelCost cost) {
    repository.save(cost);
  }

  public List<TravelCost> getAll() {
    List<TravelCost> target = new ArrayList<>();
    repository.findAll().iterator().forEachRemaining(target::add);
    return target;
  }

  public ImmutableTable<String, String, Double> getByRegionIdListAndTravelTime(SessionDto session) {

    List<TravelCost> cost =
        repository.findTravelCostsBySessionAndTravelTimeLessThan(
            session.getId(), session.getMaxTravelTimeInMinutes());

    return cost.stream()
        .collect(
            ImmutableTable.toImmutableTable(
                TravelCost::getStartRegionId,
                TravelCost::getEndRegionId,
                TravelCost::getTravelTimeInMinutes));
  }

  public boolean existByStartIdAndEndId(String startRegionId, String endRegionId) {
    return repository.existsTravelCostByStartRegionIdAndEndRegionId(startRegionId, endRegionId);
  }

  public List<TravelCost> getByStartIdAndEndIdBeginsWith(String startRegionId, String endRegionId) {
    return repository.findAllByStartRegionIdStartingWithAndEndRegionIdStartingWith(
        startRegionId, endRegionId);
  }

  public List<TravelCost> getByStartAndEndRegExp(String startRegionId, String endRegionId) {
    return repository.findByStartAndEndRegExp(startRegionId, endRegionId);
  }
}
