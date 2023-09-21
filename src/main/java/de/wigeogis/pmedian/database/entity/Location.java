package de.wigeogis.pmedian.database.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "location", indexes = {
    @Index(name = "idx_location_id_unq", columnList = "id", unique = true),
    @Index(name = "idx_location_session_id", columnList = "session_id"),
    @Index(name = "idx_location_region_id", columnList = "region_id"),
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Location {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(nullable = false, updatable = false)
  private Integer id;

  @Column(name = "session_id")
  private UUID sessionId;

  @Column(name = "region_id")
  private String regionId;

  @Column(name = "demand_count")
  private Integer demandCount;

  @Column(name = "travel_cost_mean")
  private Double travelCostMean;

}
