package de.wigeogis.pmedian.job.preprocessing;

import de.wigeogis.pmedian.database.dto.AllocationDto;
import de.wigeogis.pmedian.database.dto.RegionDto;
import de.wigeogis.pmedian.database.entity.Allocation;
import de.wigeogis.pmedian.database.entity.Region;
import de.wigeogis.pmedian.database.entity.Session;
import de.wigeogis.pmedian.database.service.AllocationService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

public class DemandItemWriter implements ItemWriter<String> {

  private final UUID session;
  private final AllocationService allocationService;

  public DemandItemWriter(UUID session, AllocationService allocationService) {
    this.session = session;
    this.allocationService = allocationService;
  }

  @Override
  public void write(Chunk<? extends String> chunk) throws Exception {
    List<Allocation> allocations =
        chunk.getItems().stream()
            .map(
                region ->
                    allocationService.dtoToEntity(
                        AllocationDto.builder()
                            .regionId(region)
                            .sessionId(session)
                            .facilityRegionId("DE-00000")
                            .build()))
            .collect(Collectors.toList());
    allocationService.insertAllEntities(allocations);
  }
}
