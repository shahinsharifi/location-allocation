package de.wigeogis.pmedian.database.repository;

import de.wigeogis.pmedian.database.entity.TravelCost;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.repository.CrudRepository;
import java.util.UUID;
import java.util.List;

@Transactional
public interface TravelCostRepository extends CrudRepository<TravelCost, Integer> {

  boolean existsTravelCostByStartRegionIdAndEndRegionId(String startRegionId, String endRegionId);

  List<TravelCost> findAllByStartRegionIdStartingWithAndEndRegionIdStartingWith(
      String startRegionId, String endRegionId);

  @Query(
      value =
          "select * from travel_cost as r where r.start_region_id ~ ?1 and r.end_region_id ~ ?2",
      nativeQuery = true)
  List<TravelCost> findByStartAndEndRegExp(String startRegExp, String endRegExp);

  // Adding the new method
  @Query(
      value =
          "SELECT tc.* " +
              "FROM travel_cost tc " +
              "JOIN allocation a1 ON tc.start_region_id = a1.region_id AND a1.session_id = ?1 " +
              "JOIN allocation a2 ON tc.end_region_id = a2.region_id AND a2.session_id = ?1 ",
      nativeQuery = true)
  List<TravelCost> findTravelCostsBySessionAndTravelTimeLessThan(UUID sessionId, Double travelTime);

}

