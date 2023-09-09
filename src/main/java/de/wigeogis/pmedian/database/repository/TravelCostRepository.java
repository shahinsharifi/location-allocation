package de.wigeogis.pmedian.database.repository;

import de.wigeogis.pmedian.database.entity.TravelCost;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface TravelCostRepository extends ListCrudRepository<TravelCost, Integer> {
  boolean existsTravelCostByStartRegionIdAndEndRegionId(String startRegionId, String endRegionId);

  List<TravelCost> findAllByStartRegionIdStartingWithAndEndRegionIdStartingWith(
      String startRegionId, String endRegionId);

  @Query(
      value =
          "select * from travel_cost as r where r.start_region_id ~ ?1 and r.end_region_id ~ ?2",
      nativeQuery = true)
  List<TravelCost> findByStartAndEndRegExp(String startRegExp, String endRegExp);
}
