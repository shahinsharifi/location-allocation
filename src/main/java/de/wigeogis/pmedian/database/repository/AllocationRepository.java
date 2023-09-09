package de.wigeogis.pmedian.database.repository;


import de.wigeogis.pmedian.database.entity.Allocation;
import jakarta.persistence.Tuple;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
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
}
