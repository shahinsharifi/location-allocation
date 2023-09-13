package de.wigeogis.pmedian.job.optimization;


import de.wigeogis.pmedian.database.dto.AllocationDto;
import java.util.List;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

public class AllocationWriter implements ItemWriter<List<AllocationDto>> {



	@Override
	public void write(Chunk<? extends List<AllocationDto>> chunk) throws Exception {
//		allocationService.insertInChunks(chunk.getItems().stream().flatMap(List::stream).toList());
	}
}