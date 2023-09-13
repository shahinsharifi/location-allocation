package de.wigeogis.pmedian.job.optimization;

import de.wigeogis.pmedian.database.dto.AllocationDto;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.item.Chunk;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class OptimizationListener implements ItemWriteListener<AllocationDto> {

  @Override
  public void beforeWrite(Chunk<? extends AllocationDto> items) {}

  @Override
  public void afterWrite(Chunk<? extends AllocationDto> items) {
    log.info("afterWrite: " + items.size());
  }

  @Override
  public void onWriteError(Exception exception, Chunk<? extends AllocationDto> items) {

    log.error("onWriteError: " + exception.getMessage());
  }
}
