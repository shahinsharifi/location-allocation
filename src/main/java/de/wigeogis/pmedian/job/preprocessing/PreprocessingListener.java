package de.wigeogis.pmedian.job.preprocessing;

import de.wigeogis.pmedian.database.dto.AllocationDto;
import de.wigeogis.pmedian.database.entity.Allocation;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.item.Chunk;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class PreprocessingListener implements ItemWriteListener<Allocation> {


	@Override
	public void beforeWrite(Chunk<? extends Allocation> items) {

	}

	@Override
	public void afterWrite(Chunk<? extends Allocation> items) {
		log.info("afterWrite: " + items.size());
	}

	@Override
	public void onWriteError(Exception exception, Chunk<? extends Allocation> items) {
		log.error("onWriteError: " + exception.getMessage());
	}
}
