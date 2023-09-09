package de.wigeogis.pmedian.database.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SessionDto implements Serializable {

  private UUID id;
  private Integer numberOfFacilities;
  private Double maxTravelTimeInMinutes;
  private Double maxTravelDistanceInMeters;
  private Double maxTravelDistanceInKilometers;
  private Boolean running;
  private String createdAt = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss z").format(new Date());

}
