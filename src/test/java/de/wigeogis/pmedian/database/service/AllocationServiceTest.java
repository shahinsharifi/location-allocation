package de.wigeogis.pmedian.database.service;

import de.wigeogis.pmedian.database.dto.AllocationDto;
import de.wigeogis.pmedian.database.entity.Allocation;
import de.wigeogis.pmedian.database.entity.Region;
import de.wigeogis.pmedian.database.entity.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AllocationServiceTest {

  private AllocationService allocationService;


  @BeforeEach
  public void setUp() {
    allocationService = new AllocationService(null);
  }

  @Test
  public void testEntityToDtoTransformation() {
    Allocation allocation = new Allocation();
    allocation.setId(1);
    Session session = new Session();
    session.setId(UUID.randomUUID());
    allocation.setSessionId(session.getId());
    allocation.setRegionId(new Region().getId());
    allocation.setFacilityRegionId("facilityCode");

    AllocationDto dto = allocationService.entityToDto(allocation);

    assertEquals(allocation.getId(), dto.getId());
    assertEquals(allocation.getSessionId(), dto.getSessionId());
    assertEquals(allocation.getRegionId(), dto.getRegionId());
    assertEquals(allocation.getFacilityRegionId(), dto.getFacilityRegionId());
  }

  @Test
  public void testDtoToEntityTransformation() {
    AllocationDto dto = new AllocationDto();
    dto.setId(1);
    dto.setSessionId(UUID.randomUUID());
    dto.setRegionId("demandCode");
    dto.setRegionId("facilityCode");

    Allocation allocation = allocationService.dtoToEntity(dto);

    assertEquals(dto.getId(), allocation.getId());
    assertEquals(dto.getSessionId(), allocation.getSessionId());
    assertEquals(dto.getRegionId(), allocation.getRegionId());
    assertEquals(dto.getFacilityRegionId(), allocation.getFacilityRegionId());
  }
}
