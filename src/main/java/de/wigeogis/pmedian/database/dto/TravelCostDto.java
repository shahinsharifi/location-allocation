package de.wigeogis.pmedian.database.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TravelCostDto implements Serializable {

  private String startRegionId;
  private String endRegionId;
  private Double travelTimeInMinutes;
  private Double travelDistanceInMeters;
  private Double travelDistanceInKilometers;
}
