# Use the official Postgres image with PostGIS extension
FROM postgis/postgis:15-3.4

# Set environment variables for Postgres
ENV POSTGRES_USER=postgres
ENV POSTGRES_PASSWORD=demo123
ENV POSTGRES_DB=location_allocation

# Install necessary tools
RUN apt-get update && apt-get install -y gzip && rm -rf /var/lib/apt/lists/*

# Copy the backup file and restore script into the container
COPY ./database/location_allocation.gz /docker-entrypoint-initdb.d/
COPY ./database/restore.sh /docker-entrypoint-initdb.d/
COPY ./database/location_layer_function.sql /docker-entrypoint-initdb.d/
COPY ./database/allocation_layer_function.sql /docker-entrypoint-initdb.d/

# Ensure the script is executable
RUN chmod +x /docker-entrypoint-initdb.d/restore.sh

EXPOSE 5432
