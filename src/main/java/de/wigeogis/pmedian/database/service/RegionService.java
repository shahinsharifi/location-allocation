package de.wigeogis.pmedian.database.service;


import de.wigeogis.pmedian.database.dto.RegionDto;
import de.wigeogis.pmedian.database.dto.VectorTileLayerDto.BoundingBoxDto;
import de.wigeogis.pmedian.database.entity.Region;
import de.wigeogis.pmedian.database.repository.RegionRepository;
import jakarta.persistence.Tuple;

import java.util.List;

import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RegionService {

  private final ModelMapper modelMapper;
  private final RegionRepository repository;

  public List<RegionDto> getAll() {
    List<Region> regions = repository.findAll();
    return regions.stream().map(this::entityToDto).collect(Collectors.toList());
  }

  public List<RegionDto> getRegionsByIdStartsWith(String id) {
    List<Region> regions = repository.getRegionsByIdStartsWith(id);
    return regions.stream().map(this::entityToDto).collect(Collectors.toList());
  }

  public List<RegionDto> getRegionsByRegionCodePattern(String regExp) {
    List<Region> regions = repository.getRegionsByRegionCodePattern(regExp);
    return regions.stream()
        .map(r -> new RegionDto(r.getId(), r.getName()))
        .collect(Collectors.toList());
  }

  public RegionDto entityToDto(Region allocation) {
    return modelMapper.map(allocation, RegionDto.class);
  }


  public BoundingBoxDto findAllRegionBounds() {
    Tuple result = repository.findAllRegionBounds();
    return new BoundingBoxDto(
        result.get("minX", Double.class),
        result.get("minY", Double.class),
        result.get("maxX", Double.class),
        result.get("maxY", Double.class)
    );
  }


  public List<RegionDto> findRegionsByWKTPolygon(String wktPolygon) {
    List<Region> regions = repository.findRegionsByWKTPolygon(wktPolygon);
    return regions.stream().map(this::entityToDto).collect(Collectors.toList());
  }

}

