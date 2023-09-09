package de.wigeogis.pmedian.job.preprocessing;

import de.wigeogis.pmedian.database.entity.Allocation;
import de.wigeogis.pmedian.database.entity.Region;
import de.wigeogis.pmedian.database.entity.Session;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.batch.item.ItemProcessor;

@NoArgsConstructor
public class DemandItemProcessor implements ItemProcessor<Region, String> {

	@Override
	public String process(Region region) throws Exception {
		return region.getId();
	}

}
