package de.wigeogis.pmedian.optimizer.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.Map;

public class FileUtils {

  public static String printAlgorithmProgress(Map<Integer, Double> input) {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    return gson.toJson(input);
  }

  public static String printAlgorithmAggregation(Map<String, Map<String, Integer>> input) {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    return gson.toJson(input);
  }
}
