package de.wigeogis.pmedian.database.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.locationtech.jts.geom.MultiPolygon;

/** DTO for {@link de.wigeogis.pmedian.database.entity.Region} */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegionGeometryDto implements Serializable {

  private String id;
  private String regionCode;
  private MultiPolygon geom;
}
