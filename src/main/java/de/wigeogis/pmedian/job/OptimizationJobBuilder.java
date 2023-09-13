package de.wigeogis.pmedian.job;

import de.wigeogis.pmedian.database.entity.Region;
import de.wigeogis.pmedian.database.repository.RegionPageableRepository;
import de.wigeogis.pmedian.database.service.AllocationService;
import de.wigeogis.pmedian.job.preprocessing.DemandItemProcessor;
import de.wigeogis.pmedian.job.preprocessing.DemandItemReader;
import de.wigeogis.pmedian.job.preprocessing.DemandItemWriter;
import de.wigeogis.pmedian.job.preprocessing.PreprocessingListener;
import jakarta.persistence.EntityManagerFactory;
import java.util.UUID;
import lombok.extern.log4j.Log4j2;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

@Log4j2
@Component
public class OptimizationJobBuilder extends QuartzJobBean {

  @Autowired private JobLauncher jobLauncher;

  @Autowired private JobRepository jobRepository;

  @Autowired private AllocationService allocationService;

  @Autowired private RegionPageableRepository regionRepository;

  @Autowired private PlatformTransactionManager transactionManager;

  @Autowired private EntityManagerFactory entityManagerFactory;

  @Override
  protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
    JobDataMap dataMap = context.getJobDetail().getJobDataMap();
   String sessionId = dataMap.getString("sessionId");
    // Session session = (Session) dataMap.get("session");
  //  PlatformTransactionManager transactionManager =
    //    (PlatformTransactionManager) dataMap.get("transactionManager");

//    if (session == null) {
//      log.error("Session is null. Exiting job execution.");
//      return;
//    }

    try {
      // Convert session object to string
  //    ObjectMapper mapper = new ObjectMapper();
    //  String sessionAsString = mapper.writeValueAsString(session);

      JobParameters jobParameters =
          new JobParametersBuilder().addString("sessionId", sessionId).toJobParameters();

      Job job = createJob(sessionId);
      jobLauncher.run(job, jobParameters);
    } catch (Exception e) {
      log.error("Error during job execution", e);
      throw new JobExecutionException(e);
    }
  }

  private Job createJob(String sessionId) {

    ItemReader<Region> reader = new DemandItemReader(regionRepository).reader();
    ItemProcessor<Region, String> processor = new DemandItemProcessor();
    ItemWriter<String> writer = new DemandItemWriter(UUID.fromString(sessionId), allocationService);
    PreprocessingListener progressWriterListener = new PreprocessingListener();

    Step preprocessing =
        new StepBuilder("preprocessing", jobRepository)
            .<Region, String>chunk(10, transactionManager)
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .listener(progressWriterListener)
            .build();

    return new JobBuilder(sessionId, jobRepository)
        .incrementer(new RunIdIncrementer())
        .start(preprocessing)
        .build();
  }

//  @Bean
//  public AsyncItemWriter<String> writer() {
//    AsyncItemWriter<String> asyncWriter = new AsyncItemWriter<>();
//    asyncWriter.setDelegate(delegateWriter());
//    return asyncWriter;
//  }
//
//  @Bean
//  public JpaItemWriter<String> delegateWriter() {
//    JpaItemWriter<String> writer = new JpaItemWriter<>();
//    writer.setEntityManagerFactory(entityManagerFactory().getObject());
//    return writer;
//  }

//  @Bean
//  public TaskExecutor taskExecutor() {
//    SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
//    taskExecutor.setConcurrencyLimit(5);  // adjust as needed
//    return taskExecutor;
//  }

}
