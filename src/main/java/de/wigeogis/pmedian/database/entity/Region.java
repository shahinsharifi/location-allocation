package de.wigeogis.pmedian.database.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "region", indexes = {
    @Index(name = "idx_region_id", columnList = "id", unique = true)
})
public class Region {

  @Id
  @Column(nullable = false, updatable = false)
  private String id;

  @Column(name = "name")
  private String name;

  @Column(name = "region_code")
  private String regionCode;

  @Column(name = "country_code")
  private String countryCode;

  @Transient
  private MultiPolygon geom;

}
