package de.wigeogis.pmedian.database.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LocationDto implements Serializable {
  private Integer id;
  private UUID sessionId;
  private String regionId;
  private Integer demandCount;
  private Double travelCostMean;

  public RegionDto toRegionDto() {
    return new RegionDto().setId(regionId);
  }
}
