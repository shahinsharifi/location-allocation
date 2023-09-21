CREATE OR REPLACE FUNCTION "public"."fn_location_zxy_query"("z" int4, "x" int4, "y" int4, "query_params" json)
    RETURNS "pg_catalog"."bytea" AS
$BODY$
DECLARE
    mvt bytea;
BEGIN
    SELECT INTO mvt ST_AsMVT(tile, 'location', 4096, 'geom')
    FROM (SELECT loc.region_id   as "id",
                 loc."demand_count"        as demand_count,
                 loc."travel_cost_mean"        as travel_cost_mean,
                 ST_AsMVTGeom(
                         ST_Centroid(re.geom), -- Centroid of the polygon
                         ST_TileEnvelope(z, x, y),
                         4096, 64, true) AS geom
          FROM "location" loc
                   JOIN region re ON loc.region_id = re."id"
          WHERE loc."session_id" = (query_params ->> 'session_id')::uuid
            AND re.geom && ST_Transform(ST_TileEnvelope(z, x, y), 3857)) as tile
    WHERE geom IS NOT NULL;

    RETURN mvt;
END
$BODY$
    LANGUAGE plpgsql IMMUTABLE
                     STRICT
                     PARALLEL SAFE
                     COST 100;