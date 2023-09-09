package de.wigeogis.pmedian.optimization.utils;

import com.google.common.collect.ImmutableTable;
import de.wigeogis.pmedian.database.entity.Region;
import java.util.*;
import java.util.stream.Collectors;

public class RegionOptimizer {

  public static List<Region> findNRegions(
      List<Region> allRegions, ImmutableTable<String, String, Double> travelTimes, int T) {

    Set<String> selectedRegions = new HashSet<>();
    Set<String> uncoveredRegions =
        new HashSet<>(
            allRegions.stream()
                .collect(Collectors.toMap(Region::getRegionCode, Region::getRegionCode))
                .values());

    // Define the comparator for the priority queue
    Comparator<Map.Entry<String, Set<String>>> comparator =
        (e1, e2) -> Integer.compare(e2.getValue().size(), e1.getValue().size());

    // Precompute the coverage map
    Map<String, Set<String>> coverageMap = new HashMap<>();
    PriorityQueue<Map.Entry<String, Set<String>>> queue = new PriorityQueue<>(comparator);

    for (Region startRegion : allRegions) {
      Set<String> coverage = new HashSet<>();
      for (Region endRegion : allRegions) {
        Double time = travelTimes.get(startRegion.getRegionCode(), endRegion.getRegionCode());
        if (time != null && time <= T) {
          coverage.add(endRegion.getRegionCode());
        }
      }
      coverageMap.put(startRegion.getRegionCode(), coverage);
      queue.add(new AbstractMap.SimpleEntry<>(startRegion.getRegionCode(), coverage));
    }

    while (!uncoveredRegions.isEmpty()) {
      // Poll the region with the best coverage
      Map.Entry<String, Set<String>> bestEntry;
      Set<String> bestCoverage;
      do {
        bestEntry = queue.poll();
        if (bestEntry == null) {
          throw new RuntimeException(
              "Couldn't find a valid coverage. There might be regions that are isolated from others.");
        }
        bestCoverage = new HashSet<>(bestEntry.getValue());
        bestCoverage.retainAll(uncoveredRegions);
      } while (!uncoveredRegions.contains(
          bestEntry.getKey())); // Ensure the region is still uncovered

      String bestRegion = bestEntry.getKey();
      selectedRegions.add(bestRegion);

      uncoveredRegions.removeAll(bestCoverage);
      uncoveredRegions.remove(bestRegion);

      // Update the priority queue by recalculating coverage for remaining regions
      queue.clear();
      for (String r : uncoveredRegions) {
        Set<String> coverage = coverageMap.get(r);
        coverage.retainAll(uncoveredRegions);
        queue.add(new AbstractMap.SimpleEntry<>(r, coverage));
      }
    }

    return allRegions.stream().filter(r -> selectedRegions.contains(r.getRegionCode())).toList();
  }

  public static List<Region> selectRegions(
      List<Region> allRegions,
      ImmutableTable<String, String, Double> travelTimes,
      int N,
      Double T) {

    Set<String> selectedRegions = new HashSet<>();
    Set<String> uncoveredRegions =
        allRegions.stream().map(Region::getRegionCode).collect(Collectors.toSet());

    int totalRegions = allRegions.size();

    while (selectedRegions.size() < N || !uncoveredRegions.isEmpty()) {
      String bestRegion = null;
      int maxCoverage = 0;

      for (Region candidate : allRegions) {
        if (selectedRegions.contains(candidate.getRegionCode())) {
          continue;
        }

        int coverage = 0;
        for (String target : uncoveredRegions) {
          if (travelTimes.contains(candidate.getRegionCode(), target)
              && travelTimes.get(candidate.getRegionCode(), target) <= T) {
            coverage++;
          }
        }

        if (coverage > maxCoverage) {
          maxCoverage = coverage;
          bestRegion = candidate.getRegionCode();
        }
      }

      // If we cannot find a region that covers at least one uncovered region
      if (bestRegion == null) {
        throw new RuntimeException("Cannot find N regions meeting the criteria");
      }

      selectedRegions.add(bestRegion);
      String finalBestRegion = bestRegion;
      uncoveredRegions.removeIf(
          target ->
              travelTimes.contains(finalBestRegion, target)
                  && travelTimes.get(finalBestRegion, target) <= T);

      if (selectedRegions.size() == N && uncoveredRegions.isEmpty()) {
        break;
      }
    }

    return allRegions.stream()
        .filter(r -> selectedRegions.contains(r.getRegionCode()))
        .collect(Collectors.toList());
  }
}
