package de.wigeogis.pmedian.job.preprocessing;


import de.wigeogis.pmedian.database.entity.Region;
import de.wigeogis.pmedian.database.repository.RegionPageableRepository;
import java.util.Collections;
import java.util.HashMap;
import lombok.AllArgsConstructor;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;


@Component
@AllArgsConstructor
public class DemandItemReader {

  private final RegionPageableRepository regionRepository;

  public RepositoryItemReader<Region> reader() {

    return new RepositoryItemReaderBuilder<Region>()
        .repository(regionRepository)
        .methodName("getRegionsByRegionCodePattern")
        //.arguments(Collections.singletonList("^DE-(8[0-9]{4}|9[0-8][0-9]{3})$"))
        .arguments(Collections.singletonList("^DE-(8[0-9]{4}|9[0-8][0-9]{3})$"))
        .pageSize(100)
        .sorts(
            new HashMap<>() {
              {
                put("id", Sort.Direction.ASC);
              }
            })
        .name("regionItemReader")
        .build();
  }
}
