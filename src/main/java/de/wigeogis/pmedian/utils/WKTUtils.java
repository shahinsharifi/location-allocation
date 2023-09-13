package de.wigeogis.pmedian.utils;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WKTUtils {

  public static String toWKTPolygon(List<Double[]> spatialQuery) {
    StringBuilder wkt = new StringBuilder("POLYGON((");

    for (Double[] coordinate : spatialQuery) {
      // Append each coordinate to the string
      wkt.append(coordinate[0]).append(" ").append(coordinate[1]).append(", ");
    }

    // Close the polygon by repeating the first coordinate
    wkt.append(spatialQuery.get(0)[0]).append(" ").append(spatialQuery.get(0)[1]);

    wkt.append("))");

    return wkt.toString();
  }

}

