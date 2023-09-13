package de.wigeogis.pmedian.job.optimization;

import de.wigeogis.pmedian.database.dto.AllocationDto;
import de.wigeogis.pmedian.database.entity.Session;
import de.wigeogis.pmedian.database.service.AllocationService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.batch.item.ItemReader;

@AllArgsConstructor
public class DemandReader implements ItemReader<List<String>> {

  private final AllocationService allocationService;
  private Session session;

  @Override
  public List<String> read() throws Exception {
    List<AllocationDto> demands = allocationService.getAllocationsBySessionId(session.getId());
    return demands.stream().map(AllocationDto::getRegionId).toList();
  }
}
