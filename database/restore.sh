#!/bin/bash
set -e

gunzip -c /docker-entrypoint-initdb.d/location_allocation.gz | psql -U "$POSTGRES_USER" -d "$POSTGRES_DB"

# call a sql script to create a database function in the location_allocation database.
psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -f /docker-entrypoint-initdb.d/location_layer_function.sql
psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -f /docker-entrypoint-initdb.d/allocation_layer_function.sql
