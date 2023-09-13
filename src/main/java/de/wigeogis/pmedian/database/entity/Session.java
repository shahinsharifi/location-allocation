package de.wigeogis.pmedian.database.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Session {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "UUID")
  @Column(nullable = false, updatable = false)
  private UUID id;

  @Column(columnDefinition = "TEXT")
  private String spatialQuery;

  private Integer numberOfFacilities;
  private Double maxTravelTimeInMinutes;
  private Double maxTravelDistanceInMeters;
  private Double maxTravelDistanceInKilometers;

  // represents the status of the session
  @Enumerated(EnumType.STRING)
  private SessionStatus status;

  private final String createdAt = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss z").format(new Date());
}
