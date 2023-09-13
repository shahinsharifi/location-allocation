package de.wigeogis.pmedian.config;


import de.wigeogis.pmedian.database.dto.AllocationDto;
import de.wigeogis.pmedian.database.dto.RegionDto;
import io.github.resilience4j.bulkhead.ThreadPoolBulkhead;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;

//@Configuration
//@EnableBatchProcessing
//@RequiredArgsConstructor
public class BatchConfig {

//	private final ThreadPoolBulkhead optimizationEngineBulkhead;
//
//	@Bean(name = "batchTaskExecutor")
//	public TaskExecutor batchTaskExecutor() {
//		return task -> {
//			Runnable decoratedTask = (Runnable) ThreadPoolBulkhead.decorateRunnable(optimizationEngineBulkhead, task);
//			new Thread(decoratedTask).start();
//		};
//	}

}