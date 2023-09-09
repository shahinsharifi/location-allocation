package de.wigeogis.pmedian.database.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VectorTileLayerDto implements Serializable {
  private String id;
  private String type;
  private Map<String, String> source;
  @JsonProperty("source-layer") private String sourceLayer;
  private Map<String, String> fields;
  private Map<String, Object> paint;
  private Map<String, Object> layout;
  private BoundingBoxDto bounds;
  private String description;

  // add inner class of 'BoundingBox' here
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class BoundingBoxDto {
    private double minX;
    private double minY;
    private double maxX;
    private double maxY;
  }



  public List<Double> getBounds() {
    return new ArrayList<>(
        List.of(bounds.getMinX(), bounds.getMinY(), bounds.getMaxX(), bounds.getMaxY()));
  }
}
