package de.wigeogis.pmedian.job;


//@Log4j2
//@Configuration
//@EnableBatchProcessing
//@RequiredArgsConstructor
public class JobConfig {

//  private final JobRepository jobRepository;
//  private final PlatformTransactionManager transactionManager;
//  private final ThreadPoolBulkhead optimizationEngineBulkhead;
//
//  @Bean(name = "resilienceExecutorService")
//  public TaskExecutor resilienceExecutorService() {
//    return task -> {
//      Supplier<CompletionStage<Void>> decoratedSupplier =
//          Decorators.ofRunnable(task).withThreadPoolBulkhead(optimizationEngineBulkhead).decorate();
//
//      CompletableFuture.supplyAsync(decoratedSupplier).join();
//    };
//  }
//
//
//  @Bean
//  public Step optimizationStep(
//      ReadTasklet reader,
//      OptimizationTasklet processor,
//      WriteTasklet writer,
//      @Qualifier("resilienceExecutorService") TaskExecutor resilienceExecutorService) {
//
//    return new StepBuilder("optimizationStep", jobRepository)
//        .tasklet(reader, transactionManager)
//        .tasklet(processor, transactionManager)
//        .tasklet(writer, transactionManager)
//        .startLimit(1)
//        .taskExecutor(resilienceExecutorService)
//        .build();
//  }
//
//  @Bean
//  public Job optimizationJob(Step optimizationStep) {
//    return new JobBuilder("optimizationJob", jobRepository)
//        .preventRestart()
//        .start(optimizationStep)
//        .build();
//  }
}
