package de.wigeogis.pmedian.database.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AllocationTileService {


  private EntityManager entityManager;

  private static final double TILE_SIZE = 256.0;
  private static final double HALF_CIRCUMFERENCE = 20037508.34;

  private void setTileBoundsToQuery(Query query, int z, int x, int y) {
    double resolution = (2 * HALF_CIRCUMFERENCE) / (TILE_SIZE * Math.pow(2, z));

    double minX = x * TILE_SIZE * resolution - HALF_CIRCUMFERENCE;
    double maxX = (x + 1) * TILE_SIZE * resolution - HALF_CIRCUMFERENCE;
    double minY = HALF_CIRCUMFERENCE - (y + 1) * TILE_SIZE * resolution;
    double maxY = HALF_CIRCUMFERENCE - y * TILE_SIZE * resolution;

    query.setParameter("minX", minX);
    query.setParameter("minY", minY);
    query.setParameter("maxX", maxX);
    query.setParameter("maxY", maxY);
  }

  public byte[] getAllocationVectorTile(int z, int x, int y, UUID sessionId) {
    String sql = "SELECT ST_AsMVT(tile) "
        + "FROM (SELECT a.id, ST_AsMVTGeom(r.geom, ST_MakeEnvelope(:minX, :minY, :maxX, :maxY, 3857)) AS geom "
        + "FROM allocation a JOIN region r on a.region_id = r.id WHERE a.session_id = :sessionId) AS tile WHERE tile.geom IS NOT NULL";

    Query query = entityManager.createNativeQuery(sql);
    setTileBoundsToQuery(query, z, x, y);
    query.setParameter("sessionId", sessionId);

    List<Object> result = query.getResultList();
    return (result.isEmpty()) ? null : (byte[]) result.get(0);
  }
}

