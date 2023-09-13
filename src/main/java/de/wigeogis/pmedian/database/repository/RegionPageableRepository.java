package de.wigeogis.pmedian.database.repository;

import de.wigeogis.pmedian.database.entity.Region;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface RegionPageableRepository extends PagingAndSortingRepository<Region, String> {

  <T> List<T> getRegionsByIdStartsWith(String idStartsWith);

  @Query(
      value = "select * from region where id ~ ?1",
      countQuery = "select count(*) from region where id ~ ?1",
      nativeQuery = true)
  Page<Region> getRegionsByRegionCodePattern(String regExp, Pageable pageable);
}
