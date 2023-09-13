package de.wigeogis.pmedian.database.repository;

import de.wigeogis.pmedian.database.entity.Region;
import jakarta.persistence.Tuple;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface RegionRepository extends JpaRepository<Region, String> {

  <T> List<T> getRegionsByIdStartsWith(String idStartsWith);

  @Query(value = "select * from region where id ~ ?1", nativeQuery = true)
  List<Region> getRegionsByRegionCodePattern(String regExp);

  @Query(
      value =
          "SELECT "
              + "  ST_XMin(extent) as minX,"
              + "  ST_YMin(extent) as minY,"
              + "  ST_XMax(extent) as maxX,"
              + "  ST_YMax(extent) as maxY"
              + " FROM ("
              + "  SELECT ST_Extent(ST_Transform(region.geom, 4326)) as extent"
              + "  FROM region"
              + ") as subquery;",
      nativeQuery = true)
  Tuple findAllRegionBounds();

  @Query(
      value =
          "SELECT * FROM region WHERE ST_Intersects(geom, ST_Transform(ST_GeomFromText(?1, 4326), 3857))",
      nativeQuery = true)
  List<Region> findRegionsByWKTPolygon(String wktPolygon);
}
