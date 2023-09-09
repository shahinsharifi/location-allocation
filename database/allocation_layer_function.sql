CREATE OR REPLACE FUNCTION "public"."fn_allocation_zxy_query"("z" int4, "x" int4, "y" int4, "query_params" json)
    RETURNS "pg_catalog"."bytea" AS
$BODY$
DECLARE
    mvt bytea;
BEGIN
    SELECT INTO mvt ST_AsMVT(tile, 'allocation', 4096, 'geom')
    FROM (SELECT re."id"                 as demand_id,
                 al.facility_region_id   as facility_id,
                 al."travel_cost"        as travel_cost,
                 ST_AsMVTGeom(
                         ST_CurveToLine(re.geom),
                         ST_TileEnvelope(z, x, y),
                         4096, 64, true) AS geom
          FROM allocation al
                   JOIN region re ON al.region_id = re."id"
          WHERE al."session_id" = (query_params ->> 'session_id')::uuid
            AND re.geom && ST_Transform(ST_TileEnvelope(z, x, y), 3857)) as tile
    WHERE geom IS NOT NULL;

    RETURN mvt;
END
$BODY$
    LANGUAGE plpgsql IMMUTABLE
                     STRICT
                     PARALLEL SAFE
                     COST 100;