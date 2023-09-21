package de.wigeogis.pmedian.api;

import de.wigeogis.pmedian.database.dto.AllocationDto;
import de.wigeogis.pmedian.database.dto.VectorTileLayerDto;
import de.wigeogis.pmedian.database.dto.VectorTileLayerDto.BoundingBoxDto;
import de.wigeogis.pmedian.database.service.AllocationService;
import de.wigeogis.pmedian.database.service.RegionService;
import de.wigeogis.pmedian.utils.VectorTileUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1")
public class VectorTileController {

  private final RegionService regionService;
  private final AllocationService allocationService;

  @GetMapping("/tiles/base")
  public ResponseEntity<VectorTileLayerDto> getBaseLayerTileJson() {
    BoundingBoxDto layerBounds = regionService.findAllRegionBounds();
    VectorTileLayerDto polygonLayer = createBaseLayer(layerBounds);
    return ResponseEntity.ok(polygonLayer);
  }

  @GetMapping("/tiles/allocation/{sessionId}")
  public ResponseEntity<?> getResultLayerTileJson(@PathVariable UUID sessionId) {
    if (!allocationService.existsAllocationsBySessionId(sessionId)) {
      return ResponseEntity.badRequest().build();
    }
    List<String> facilities = fetchFacilities(sessionId);
    BoundingBoxDto layerBounds = allocationService.getAllocationBoundsBySessionId(sessionId);
    VectorTileLayerDto allocationLayer = createAllocationLayer(sessionId, facilities, layerBounds);
    VectorTileLayerDto locationLayer = createLocationLayer(sessionId, facilities, layerBounds);
    return ResponseEntity.ok(Arrays.asList(allocationLayer, locationLayer));
  }

  private VectorTileLayerDto createBaseLayer(BoundingBoxDto layerBounds) {
    VectorTileLayerDto layer = new VectorTileLayerDto();

    Map<String, String> source = Map.of("type", "vector", "url", "http://localhost:3000/region", "promoteId", "id");

    Map<String, String> fields =
        Map.of(
            "id", "varchar",
            "region_code", "varchar",
            "country_code", "varchar");

    layer.setId("region");
    layer.setSource(source);
    layer.setSourceLayer("region");
    layer.setType("fill");
    layer.setFields(fields);
    layer.setBounds(layerBounds);
    layer.setDescription("region.geom");
    layer.setLayout(new HashMap<>());

    // Define fill-color using the conditional feature state logic
    List<Object> fillColorValue =
        new ArrayList<>(
            Arrays.asList(
                "case",
                Arrays.asList("boolean", Arrays.asList("feature-state", "highlight"), false),
                "#ff9900", // Highlight color
                String.format("#%02x%02x%02x", 163, 163, 163) // Original color
                ));

    layer.setPaint(
        Map.of(
            "fill-color",
            fillColorValue,
            "fill-opacity",
            0.5,
            "fill-antialias",
            true,
            "fill-outline-color",
            "hsl(0, 0%, 47%)"));

    return layer;
  }

  private VectorTileLayerDto createAllocationLayer(
      UUID sessionId, List<String> facilities, BoundingBoxDto layerBounds) {
    VectorTileLayerDto layer = new VectorTileLayerDto();

    Map<String, String> source =
        Map.of(
            "type",
            "vector",
            "url",
            "http://localhost:3000/fn_allocation_zxy_query?session_id=" + sessionId);

    Map<String, String> fields =
        Map.of(
            "id", "varchar",
            "session_id", "varchar",
            "demand_id", "varchar",
            "facility_id", "varchar",
            "travel_cost", "double precision"
        );

    layer.setId("allocation");
    layer.setSource(source);
    layer.setSourceLayer("allocation");
    layer.setType("fill");
    layer.setFields(fields);
    layer.setBounds(layerBounds);
    layer.setDescription("allocation.demand.geom");

    if (!facilities.isEmpty()) {
      Map<String, Object> paint = VectorTileUtils.getPaintForValues("facility_id", facilities);
      layer.setPaint(paint);
      layer.setLayout(new HashMap<>());
    }

    return layer;
  }

  private VectorTileLayerDto createLocationLayer(
      UUID sessionId, List<String> facilities, BoundingBoxDto layerBounds) {
    VectorTileLayerDto layer = new VectorTileLayerDto();

    Map<String, String> source =
        Map.of(
            "type",
            "vector",
            "url",
            "http://localhost:3000/fn_location_zxy_query?session_id=" + sessionId.toString());

    Map<String, String> fields =
        Map.of(
            "id", "varchar",
            "session_id", "varchar",
            "demand_id", "varchar",
            "facility_id", "varchar",
            "travel_cost", "double precision"
        );

    layer.setId("location");
    layer.setSource(source);
    layer.setSourceLayer("location");
    layer.setType("symbol");
    layer.setBounds(layerBounds);
    layer.setDescription("location.facility.geom");

    if (!facilities.isEmpty()) {
      Map<String, Object> layout = VectorTileUtils.getLayoutForCenters("demand_id", facilities);
      layer.setLayout(layout);

      // Here, we might also want to define how the icons will appear, e.g. size, color, etc.
      layer.setPaint(new HashMap<>());
    }

    return layer;
  }

  private List<String> fetchFacilities(UUID sessionId) {
    return allocationService.getAllocationsBySessionId(sessionId).stream()
        .map(AllocationDto::getFacilityRegionId)
        .distinct()
        .toList();
  }
}
