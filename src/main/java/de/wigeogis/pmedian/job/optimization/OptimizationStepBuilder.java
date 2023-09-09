package de.wigeogis.pmedian.job.optimization;

import de.wigeogis.pmedian.database.dto.AllocationDto;
import de.wigeogis.pmedian.database.dto.SessionDto;
import de.wigeogis.pmedian.database.entity.Region;
import de.wigeogis.pmedian.database.entity.Session;
import de.wigeogis.pmedian.database.service.AllocationService;
import de.wigeogis.pmedian.database.service.TravelCostService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

@Component
@AllArgsConstructor
public class OptimizationStepBuilder {

	private final JobRepository jobRepository;
	private final AllocationService allocationService;
	private final TravelCostService travelCostService;
	private final PlatformTransactionManager transactionManager;
	private final OptimizationListener optimizationListener;

	public Step createStep(Session session) {
		return new StepBuilder("optimization", jobRepository)
				.<List<String>, List<AllocationDto>>chunk(50000, transactionManager)
				.reader(createReader(session))
				.processor(createProcessor(session))
				.writer(createWriter())
				.listener(optimizationListener)
				.build();
	}


	private ItemReader<List<String>> createReader(Session session) {
		return new DemandReader(session, allocationService);
	}

	private ItemProcessor<List<String>, List<AllocationDto>> createProcessor(Session session) {
		return new OptimizationProcessor(session, travelCostService);
	}

	private ItemWriter<List<AllocationDto>> createWriter() {
    return new AllocationWriter();
	}


}
