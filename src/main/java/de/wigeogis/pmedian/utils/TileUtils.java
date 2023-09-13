package de.wigeogis.pmedian.utils;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TileUtils {

  private static final Pattern TILE_PATTERN = Pattern.compile("^/(\\d+)/(\\d+)/(\\d+)\\.(\\w+)");

  public static void main(String[] args) {
    TileUtils utils = new TileUtils();

    HashMap<String, Object> tile = utils.pathToTile("/5/15/12.pbf");
    HashMap<String, Double> env = utils.tileToEnvelope(tile);
    HashMap<String, Object> tbl = utils.getTable();
    String sql = utils.envelopeToSQL(tbl, env);
  }

  public HashMap<String, Object> pathToTile(String path) {
    Matcher m = TILE_PATTERN.matcher(path);
    if (m.matches()) {
      HashMap<String, Object> result = new HashMap<>();
      result.put("zoom", Integer.parseInt(m.group(1)));
      result.put("x", Integer.parseInt(m.group(2)));
      result.put("y", Integer.parseInt(m.group(3)));
      result.put("format", m.group(4));
      return result;
    }
    return null;
  }

  public HashMap<String, Double> tileToEnvelope(HashMap<String, Object> tile) {
    final double worldMercMax = 20037508.3427892;
    final double worldMercMin = -1 * worldMercMax;
    final double worldMercSize = worldMercMax - worldMercMin;
    double worldTileSize = Math.pow(2, (int) tile.get("zoom"));
    double tileMercSize = worldMercSize / worldTileSize;

    HashMap<String, Double> env = new HashMap<>();
    env.put("xmin", worldMercMin + tileMercSize * (int) tile.get("x"));
    env.put("xmax", worldMercMin + tileMercSize * ((int) tile.get("x") + 1));
    env.put("ymin", worldMercMax - tileMercSize * ((int) tile.get("y") + 1));
    env.put("ymax", worldMercMax - tileMercSize * (int) tile.get("y"));

    return env;
  }

  public String envelopeToBoundsSQL(HashMap<String, Double> env) {
    final int DENSIFY_FACTOR = 4;
    double segSize = (env.get("xmax") - env.get("xmin")) / DENSIFY_FACTOR;
    return String.format(
        "ST_Segmentize(ST_MakeEnvelope(%f, %f, %f, %f, 3857), %f)",
        env.get("xmin"), env.get("ymin"), env.get("xmax"), env.get("ymax"), segSize);
  }

  public String envelopeToSQL(HashMap<String, Object> tbl, HashMap<String, Double> env) {
    tbl.put("env", envelopeToBoundsSQL(env));
    String sql_tmpl =
        """
            WITH
            bounds AS (
                SELECT %s AS geom,
                       %s::box2d AS b2d
            ),
            mvtgeom AS (
                SELECT ST_AsMVTGeom(ST_Transform(t.%s, 3857), bounds.b2d) AS geom,
                       %s
                FROM %s t, bounds
                WHERE ST_Intersects(t.%s, ST_Transform(bounds.geom, %d))
            )
            SELECT ST_AsMVT(mvtgeom.*) FROM mvtgeom
        """;
    return String.format(
        sql_tmpl,
        tbl.get("env"),
        tbl.get("env"),
        tbl.get("geomColumn"),
        tbl.get("attrColumns"),
        tbl.get("table"),
        tbl.get("geomColumn"),
        tbl.get("srid"));
  }

  public HashMap<String, Object> getTable() {
    HashMap<String, Object> table = new HashMap<>();
    // Fill in your table details
    table.put("geomColumn", "geom");
    table.put("attrColumns", "id, name, region_code, country_code");
    table.put("table", "region");
    table.put("srid", 3857); // or whatever SRID value you have
    return table;
  }
}
