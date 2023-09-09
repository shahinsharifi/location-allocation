package de.wigeogis.pmedian.optimizer.model;

import de.wigeogis.pmedian.database.dto.RegionDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BasicGenome implements Comparable<BasicGenome> {

  private String regionId;
  private Double weight;

  public BasicGenome(String regionId) {
    this.regionId = regionId;
  }

  public RegionDto getRegionDto() {
    return new RegionDto().setId(regionId);
  }

  @Override
  public int compareTo(BasicGenome t) {
    return this.regionId.compareTo(t.getRegionId());
  }

  @Override
  public boolean equals(Object object) {
    boolean same = false;
    if (object instanceof BasicGenome)
      same = this.regionId.equals(((BasicGenome) object).getRegionId());
    return same;
  }
}
