package de.wigeogis.pmedian.database.service;

import de.wigeogis.pmedian.database.dto.AllocationDto;
import de.wigeogis.pmedian.database.dto.VectorTileLayerDto.BoundingBoxDto;
import de.wigeogis.pmedian.database.entity.Allocation;
import de.wigeogis.pmedian.database.repository.AllocationRepository;
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
public class AllocationService {

  private final AllocationRepository repository;

  @Autowired
  public AllocationService(AllocationRepository allocationRepository) {
    this.repository = allocationRepository;
  }

  @Transactional
  public void insertAll(List<AllocationDto> allocations) {
    repository.saveAll(allocations.stream().map(this::dtoToEntity).collect(Collectors.toList()));
  }

  @Transactional
  public void insertAllEntities(List<Allocation> allocations) {
    repository.saveAll(allocations);
  }

  @Async
  public CompletableFuture<Integer> insertAllAsync(List<AllocationDto> allocations) {
    List<Allocation> savedAllocations =
        repository.saveAll(
            allocations.stream().map(this::dtoToEntity).collect(Collectors.toList()));
    return CompletableFuture.completedFuture(savedAllocations.size());
  }

  public boolean existsAllocationsBySessionId(UUID sessionId) {
    return repository.existsAllocationsBySessionId(sessionId);
  }

  public List<AllocationDto> getAllocationsBySessionId(UUID sessionId) {
    return repository.findAllocationBySessionId(sessionId).stream()
        .map(this::entityToDto)
        .collect(Collectors.toList());
  }

  public BoundingBoxDto getAllocationBoundsBySessionId(UUID sessionId) {
    Tuple result = repository.findAllocationExtentBySessionId(sessionId);
    return new BoundingBoxDto(
        result.get("minX", Double.class),
        result.get("minY", Double.class),
        result.get("maxX", Double.class),
        result.get("maxY", Double.class));
  }

  public List<AllocationDto> insertAndFetchRegionsByWKTPolygon(UUID sessionId, String wktPolygon) {
    List<Allocation> allocations =
        repository.insertAndFetchRegionsByWKTPolygon(sessionId, wktPolygon);
    return allocations.stream().map(this::entityToDto).collect(Collectors.toList());
  }

  public AllocationDto entityToDto(Allocation allocation) {
    return new ModelMapper().map(allocation, AllocationDto.class);
  }

  public Allocation dtoToEntity(AllocationDto allocationDto) {
    return new ModelMapper().map(allocationDto, Allocation.class);
  }
}
