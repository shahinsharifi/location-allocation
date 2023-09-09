package de.wigeogis.pmedian.job.optimization;

import de.wigeogis.pmedian.database.dto.AllocationDto;
import de.wigeogis.pmedian.database.dto.SessionDto;
import de.wigeogis.pmedian.database.entity.Session;
import de.wigeogis.pmedian.database.service.AllocationService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;


@AllArgsConstructor
public class DemandReader implements ItemReader<List<String>> {

  private Session session;
  private final AllocationService allocationService;

  @Override
  public List<String> read() throws Exception {
    List<AllocationDto> demands = allocationService.getAllocationsBySessionId(session.getId());
    return demands.stream().map(AllocationDto::getRegionId).toList();
  }
}
