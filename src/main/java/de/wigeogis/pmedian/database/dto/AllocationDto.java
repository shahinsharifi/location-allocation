package de.wigeogis.pmedian.database.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.wigeogis.pmedian.database.entity.Region;
import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.Value;
import lombok.experimental.Accessors;
import org.locationtech.jts.geom.MultiPolygon;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AllocationDto implements Serializable {

  private Integer id;
  private UUID sessionId;
  private String regionId;
  private String facilityRegionId;
  private Double travelCost;


  public static AllocationDto fromRegion(Region region) {
    AllocationDto allocationDto = new AllocationDto();
    allocationDto.setRegionId(region.getId());
    return allocationDto;
  }
}
