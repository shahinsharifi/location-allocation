package de.wigeogis.pmedian.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import java.util.ArrayList;

public class VectorTileUtils {

  private static final Random random = new Random();

  public static String getRandomColor() {
    int r = random.nextInt(256);
    int g = random.nextInt(256);
    int b = random.nextInt(256);
    return String.format("#%02x%02x%02x", r, g, b);
  }

  public static Map<String, Object> getPaintForValues(
      String columnName, List<String> distinctValues) {
    List<Object> stops = new ArrayList<>();
    for (String value : distinctValues) {
      stops.add(new Object[] {value, getRandomColor()});
    }

    Map<String, Object> paint = new HashMap<>();
    paint.put(
        "fill-color",
        Map.of(
            "property", columnName,
            "type", "categorical",
            "stops", stops));
    paint.put("fill-opacity", 0.6);
    return paint;
  }

  public static Map<String, Object> getLayoutForCenters(String columnName, List<String> centers) {
    List<Object> iconStops = new ArrayList<>();

    for (String value : centers) {
      iconStops.add(new Object[] {value, "facility"});  // This assumes that each facility type has an icon named as facilityType_icon
    }

    Map<String, Object> layout = new HashMap<>();
    layout.put("icon-image",
        Map.of(
            "property", columnName,
            "type", "categorical",
            "stops", iconStops));
    layout.put("icon-size", 0.25);

    return layout;
  }


}
