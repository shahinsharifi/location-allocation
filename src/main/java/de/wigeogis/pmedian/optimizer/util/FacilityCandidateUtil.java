package de.wigeogis.pmedian.optimizer.util;

import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import de.wigeogis.pmedian.database.dto.AllocationDto;
import de.wigeogis.pmedian.database.dto.RegionDto;
import de.wigeogis.pmedian.database.entity.Region;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class FacilityCandidateUtil {

  public static List<RegionDto> findFacilityCandidates(
      List<RegionDto> regions,
      ImmutableTable<String, String, Double> travelTimeMatrix,
      int N,
      Double maxTravelTime,
      Random random) {

    Set<RegionDto> remainingFacilities = new HashSet<>(regions);
    List<RegionDto> facilities = new ArrayList<>();

    while (facilities.size() < N && !remainingFacilities.isEmpty()) {
      RegionDto center =
          new ArrayList<>(remainingFacilities).get(random.nextInt(remainingFacilities.size()));
      Set<RegionDto> coveredZipcodes = getReachableZipcodes(center, travelTimeMatrix, maxTravelTime);
      if (!coveredZipcodes.isEmpty()) {
        facilities.add(center);
      }

      // To ensure the center itself gets removed from remainingZipcodes.
      coveredZipcodes.add(center);
      remainingFacilities.removeAll(coveredZipcodes);
    }

    return facilities;
  }

  public static List<RegionDto> findMinimumFacilityCandidates(
      List<RegionDto> regions, ImmutableTable<String, String, Double> travelTimeMatrix,  Double maxTravelTime) {

    Set<RegionDto> remainingZipCodes = new HashSet<>(regions);
    List<RegionDto> facilities = new ArrayList<>();

    while (!remainingZipCodes.isEmpty()) {

      // Greedily find the center that covers the most zip codes
      Map.Entry<RegionDto, Set<RegionDto>> bestCenterEntry = null;

      for (RegionDto potentialCenter : remainingZipCodes) {
        Set<RegionDto> coveredByPotential = getReachableZipcodes(potentialCenter, travelTimeMatrix, maxTravelTime);
        if (bestCenterEntry == null
            || coveredByPotential.size() > bestCenterEntry.getValue().size()) {
          bestCenterEntry = new AbstractMap.SimpleEntry<>(potentialCenter, coveredByPotential);
        }
      }

      log.info("Found center covering {} zip codes", bestCenterEntry.getValue().size());
      RegionDto bestCenter = bestCenterEntry.getKey();
      facilities.add(bestCenter);
      remainingZipCodes.removeAll(bestCenterEntry.getValue());
    }

    return facilities;
  }

  public static int calculateUncoveredDemands(
      List<RegionDto> demands,
      List<RegionDto> facilities,
      ImmutableTable<String, String, Double> distanceMatrix) {
    Map<RegionDto, RegionDto> allocatedDemands =
        FacilityCandidateUtil.findNearestFacilities(demands, facilities, distanceMatrix);
    return demands.size() - allocatedDemands.size();
  }

  public static Set<RegionDto> getReachableZipcodes(
      RegionDto center, Table<String, String, Double> travelTimeMatrix, Double maxTravelTime) {
    return travelTimeMatrix.row(center.getId()).entrySet().stream()
        .filter(entry -> entry.getValue() <= maxTravelTime)
        .map(Map.Entry::getKey)
        .map(regionId -> new RegionDto().setId(regionId))
        .collect(Collectors.toSet());
  }

  public static Map<RegionDto, RegionDto> findNearestFacilities(
      List<RegionDto> regions,
      List<RegionDto> facilities,
      ImmutableTable<String, String, Double> distanceMatrix) {
    Map<RegionDto, RegionDto> nearestFacilities = new HashMap<>();

    for (RegionDto demand : regions) {
      double minDistance = Double.MAX_VALUE;
      RegionDto nearestFacility = null;

      for (RegionDto facility : facilities) {
        Double distance = distanceMatrix.get(demand.getId(), facility.getId());
        if (distance != null && distance < minDistance) {
          minDistance = distance;
          nearestFacility = facility;
        }
      }

      if (nearestFacility != null) {
        nearestFacilities.put(demand, nearestFacility);
      } else if (distanceMatrix.row(demand.getId()).size() == 1
          && distanceMatrix.contains(demand.getId(), demand.getId())) {
        nearestFacilities.put(
            new RegionDto().setId(demand.getId()),
            new RegionDto().setId(demand.getId()));
      }
    }

    return nearestFacilities;
  }

  public static List<AllocationDto> findNearestFacilitiesForDemands(
      List<AllocationDto> allocations,
      List<RegionDto> facilities,
      ImmutableTable<String, String, Double> distanceMatrix) {

    for (AllocationDto demand : allocations) {
      double minDistance = Double.MAX_VALUE;
      RegionDto nearestFacility = null;

      for (RegionDto facility : facilities) {
        Double distance = distanceMatrix.get(demand.getRegionId(), facility.getId());
        if (distance != null && distance < minDistance) {
          minDistance = distance;
          nearestFacility = facility;
        }
      }

      if (nearestFacility != null) {
        demand.setFacilityRegionId(nearestFacility.getId());
        demand.setTravelCost(minDistance);
      } else if (distanceMatrix.row(demand.getRegionId()).size() == 1
          && distanceMatrix.contains(demand.getRegionId(), demand.getRegionId())) {
        demand.setFacilityRegionId(demand.getRegionId());
        demand.setTravelCost(0.0);
      }
    }
    return allocations;
  }
}
