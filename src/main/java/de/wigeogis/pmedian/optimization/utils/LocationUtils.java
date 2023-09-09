package de.wigeogis.pmedian.optimization.utils;

import de.wigeogis.pmedian.optimization.model.BasicGenome;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class LocationUtils {

  public static List<String> findFacilityCandidates(
      Integer numberOfFacilities,
      Double maxTravelTime,
      List<String> regions,
      Random random,
      ConcurrentHashMap<String, ConcurrentHashMap<String, Double>> costMatrix) {

    Set<String> remainingFacilities = new HashSet<>(regions);
    List<String> facilities = new ArrayList<>();

    while (facilities.size() < numberOfFacilities && !remainingFacilities.isEmpty()) {
      String centerRegionCode =
          new ArrayList<>(remainingFacilities).get(random.nextInt(remainingFacilities.size()));
      Set<String> coveredZipcodes = getReachableZipcodes(centerRegionCode, costMatrix, maxTravelTime);
      if (!coveredZipcodes.isEmpty()) {
        facilities.add(centerRegionCode);
      }

      // To ensure the center itself gets removed from remainingZipcodes.
      coveredZipcodes.add(centerRegionCode);
      remainingFacilities.removeAll(coveredZipcodes);
    }

    return facilities;
  }

  public static List<String> findMinimumFacilityCandidates(
      List<String> regions, ConcurrentHashMap<String, ConcurrentHashMap<String, Double>> costMatrix,  Double maxTravelTime) {

    Set<String> remainingZipCodes = new HashSet<>(regions);
    List<String> facilities = new ArrayList<>();

    while (!remainingZipCodes.isEmpty()) {

      // Greedily find the center that covers the most zip codes
      Map.Entry<String, Set<String>> bestCenterEntry = null;

      for (String potentialCenter : remainingZipCodes) {
        Set<String> coveredByPotential = getReachableZipcodes(potentialCenter, costMatrix, maxTravelTime);
        if (bestCenterEntry == null
            || coveredByPotential.size() > bestCenterEntry.getValue().size()) {
          bestCenterEntry = new AbstractMap.SimpleEntry<>(potentialCenter, coveredByPotential);
        }
      }

      log.info("Found center covering {} zip codes", bestCenterEntry.getValue().size());
      String bestCenter = bestCenterEntry.getKey();
      facilities.add(bestCenter);
      remainingZipCodes.removeAll(bestCenterEntry.getValue());
    }

    return facilities;
  }

  public static int calculateUncoveredRegions(
      List<String> facilities,
      List<String> demands,
      ConcurrentHashMap<String, ConcurrentHashMap<String, Double>> costMatrix) {
    int covered =
        LocationUtils.findNearestFacilities(demands, facilities, costMatrix).size();
    return demands.size() - covered;
  }

  public static Set<String> getReachableZipcodes(
      String center, ConcurrentHashMap<String, ConcurrentHashMap<String, Double>> costMatrix, Double maxTravelTime) {
    return costMatrix.get(center).entrySet().stream()
        .filter(entry -> entry.getValue() <= maxTravelTime)
        .map(Map.Entry::getKey)
        .collect(Collectors.toSet());
  }

  public static Map<String, String> findNearestFacilities(
      List<String> regions,
      List<String> facilities,
      ConcurrentHashMap<String, ConcurrentHashMap<String, Double>> costMatrix) {
    Map<String, String> nearestFacilities = new HashMap<>();

    for (String demand : regions) {
      double minDistance = Double.MAX_VALUE;
      String nearestFacility = null;

      for (String facility : facilities) {
        Double distance = costMatrix.get(demand).get(facility);
        if (distance != null && distance < minDistance) {
          minDistance = distance;
          nearestFacility = facility;
        }
      }

      if (nearestFacility != null) {
        nearestFacilities.put(demand, nearestFacility);
      } else if (costMatrix.get(demand).size() == 1 && costMatrix.get(demand).containsKey(demand)) {
        nearestFacilities.put(demand,demand);
      }
    }

    return nearestFacilities;
  }
}
