package de.wigeogis.pmedian.config;

import io.github.resilience4j.bulkhead.ThreadPoolBulkhead;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResilienceConfig {

  @Value("${resilience.thread-pool.core-size:8}") // default to half of 16 cores
  private int coreSize;

  @Value("${resilience.thread-pool.max-size:10}") // leave 2 cores for other tasks
  private int maxSize;

  @Value("${resilience.thread-pool.queue-capacity:20}") // arbitrary default
  private int queueCapacity;

  @Bean
  public ThreadPoolBulkheadConfig bulkheadConfig() {
    return ThreadPoolBulkheadConfig.custom()
        .coreThreadPoolSize(coreSize)
        .maxThreadPoolSize(maxSize)
        .queueCapacity(queueCapacity)
        .build();
  }

  @Bean
  public ThreadPoolBulkhead optimizationEngineBulkhead(ThreadPoolBulkheadConfig bulkheadConfig) {
    return ThreadPoolBulkhead.of("optimizationEngine", bulkheadConfig);
  }

  @Bean
  public CircuitBreaker optimizationEngineCircuitBreaker() {
    return CircuitBreaker.ofDefaults("optimizationEngine");
  }
}
