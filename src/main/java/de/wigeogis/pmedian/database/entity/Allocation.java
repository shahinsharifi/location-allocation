package de.wigeogis.pmedian.database.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.ToString.Exclude;
import org.antlr.v4.runtime.misc.NotNull;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.proxy.HibernateProxy;



@Entity
@Table(name = "allocation", indexes = {
    @Index(name = "idx_allocation_id_unq", columnList = "id", unique = true),
    @Index(name = "idx_allocation_session_id", columnList = "session_id"),
    @Index(name = "idx_allocation_region_id", columnList = "region_id"),
    @Index(name = "idx_allocation_facility_region_id", columnList = "facility_region_id"),
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Allocation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(nullable = false, updatable = false)
  private Integer id;

  @Column(name = "session_id")
  private UUID sessionId;

  @Column(name = "region_id")
  private String regionId;

  @Column(name = "facility_region_id")
  private String facilityRegionId;

  private Double travelCost;

}
