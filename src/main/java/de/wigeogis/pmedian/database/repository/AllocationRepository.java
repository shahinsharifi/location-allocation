package de.wigeogis.pmedian.database.repository;


import de.wigeogis.pmedian.database.entity.Allocation;
import jakarta.persistence.Tuple;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface AllocationRepository extends JpaRepository<Allocation, UUID> {


  boolean existsAllocationsBySessionId(UUID sessionId);

  List<Allocation> findAllocationBySessionId(UUID sessionId);

  @Query(
      value =
          "SELECT "
              + "  ST_XMin(extent) as minX,"
              + "  ST_YMin(extent) as minY,"
              + "  ST_XMax(extent) as maxX,"
              + "  ST_YMax(extent) as maxY"
              + " FROM ("
              + "  SELECT ST_Extent(ST_Transform(re.geom, 4326)) as extent"
              + "  FROM allocation al JOIN region re ON al.region_id = re.id"
              + "  WHERE al.session_id = ?1"
              + ") as subquery;",
      nativeQuery = true)
  Tuple findAllocationExtentBySessionId(UUID sessionId);


  @Query(
      value =
          "WITH inserted AS (" +
              "    INSERT INTO allocation (session_id, region_id, facility_region_id, travel_cost) " +
              "    SELECT ?1 as session_id, \"id\" as region_id, '-1' as facility_region_id, 0.0 as travel_cost " +
              "    FROM region " +
              "    WHERE ST_Intersects(geom, ST_Transform(ST_GeomFromText(?2, 4326), 3857)) " +
              "    RETURNING * " +
              "), " +
              "reachable_regions AS (" +
              "    SELECT end_region_id as region_id, count(start_region_id) as total_reachable_regions " +
              "    FROM travel_cost " +
              "    WHERE travel_time_in_minutes < ?3 " +
              "    GROUP BY end_region_id " +
              "    HAVING count(start_region_id) = 1 " +
              ") " +
              "SELECT i.* " +
              "FROM inserted i " +
              "LEFT JOIN reachable_regions rr ON i.region_id = rr.region_id " +
              "WHERE rr.region_id IS NULL;",
      nativeQuery = true)
  List<Allocation> insertAndFetchRegionsByWKTPolygon(UUID sessionId, String wktPolygon, Integer maxTravelTime);


}
