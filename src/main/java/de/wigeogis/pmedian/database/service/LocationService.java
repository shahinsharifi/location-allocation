package de.wigeogis.pmedian.database.service;

import de.wigeogis.pmedian.database.dto.AllocationDto;
import de.wigeogis.pmedian.database.dto.LocationDto;
import de.wigeogis.pmedian.database.dto.VectorTileLayerDto.BoundingBoxDto;
import de.wigeogis.pmedian.database.entity.Location;
import de.wigeogis.pmedian.database.repository.LocationRepository;
import jakarta.persistence.Tuple;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LocationService {

  private final LocationRepository repository;

  @Autowired
  public LocationService(LocationRepository locationRepository) {
    this.repository = locationRepository;
  }

  @Transactional
  public void insertAll(List<LocationDto> locations) {
    repository.saveAll(locations.stream().map(this::dtoToEntity).collect(Collectors.toList()));
  }

  @Transactional
  public void insertAllFromAllocation(List<AllocationDto> allocations) {
    List<Location> entities =
        allocations.stream()
            .collect(Collectors.groupingBy(AllocationDto::getFacilityRegionId))
            .entrySet()
            .stream()
            .map(
                entry -> {
                  String facilityRegionId = entry.getKey();
                  List<AllocationDto> groupAllocations = entry.getValue();

                  UUID sessionId = groupAllocations.get(0).getSessionId();
                  int demandCount = groupAllocations.size();

                  double travelCostMean =
                      groupAllocations.stream()
                          .mapToDouble(AllocationDto::getTravelCost)
                          .average()
                          .orElse(0);

                  return new LocationDto()
                      .setSessionId(sessionId)
                      .setRegionId(facilityRegionId)
                      .setDemandCount(demandCount)
                      .setTravelCostMean(travelCostMean);
                })
            .map(this::dtoToEntity)
            .collect(Collectors.toList());

    repository.saveAll(entities);
  }

  @Async
  public CompletableFuture<Integer> insertAllAsync(List<LocationDto> locations) {
    List<Location> savedLocations =
        repository.saveAll(locations.stream().map(this::dtoToEntity).collect(Collectors.toList()));
    return CompletableFuture.completedFuture(savedLocations.size());
  }

  public boolean existsLocationsBySessionId(UUID sessionId) {
    return repository.existsLocationBySessionId(sessionId);
  }

  public List<LocationDto> getLocationsBySessionId(UUID sessionId) {
    return repository.findLocationBySessionId(sessionId).stream()
        .map(this::entityToDto)
        .collect(Collectors.toList());
  }

  public BoundingBoxDto getLocationBoundsBySessionId(UUID sessionId) {
    Tuple result = repository.findLocationExtentBySessionId(sessionId);
    return new BoundingBoxDto(
        result.get("minX", Double.class),
        result.get("minY", Double.class),
        result.get("maxX", Double.class),
        result.get("maxY", Double.class));
  }

  public LocationDto entityToDto(Location location) {
    return new ModelMapper().map(location, LocationDto.class);
  }

  public Location dtoToEntity(LocationDto LocationDto) {
    return new ModelMapper().map(LocationDto, Location.class);
  }
}
