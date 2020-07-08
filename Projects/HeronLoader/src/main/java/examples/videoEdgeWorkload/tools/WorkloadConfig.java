// References
// -
// https://github.com/ADSC-Resa/resaVLDTopology/blob/master/src/main/java/topology/StormConfigManager.java

package examples.videoEdgeWorkload.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;

public class WorkloadConfig {

  public static HashMap<String, Object> readConfig(String path) throws FileNotFoundException {
    HashMap<String, Object> config = new HashMap<String, Object>();
    Yaml yaml = new Yaml();
    InputStream inputStream = new FileInputStream(new File(path));
    @SuppressWarnings("unchecked")
    HashMap<String, Object> map = (HashMap<String, Object>) yaml.load(inputStream);

    config.putAll(map);
    return config;
  }

  public static int getInt(Map<String, Object> config, String key) {
    Object obj = config.get(key);
    if (obj instanceof Integer) return (Integer) obj;
    return ((Long) config.get(key)).intValue();
  }

  public static int getInt(Map<String, Object> conf, String key, int defaultValue) {
    Object value = conf.get(key);
    if (value != null && value instanceof Number) {
      return ((Number) value).intValue();
    }
    return defaultValue;
  }

  public static long getLong(Map<String, Object> conf, String key, long defaultValue) {
    Object value = conf.get(key);
    if (value != null && value instanceof Number) {
      return ((Number) value).longValue();
    }
    return defaultValue;
  }

  public static double getDouble(Map<String, Object> conf, String key, double defaultValue) {
    Object value = conf.get(key);
    if (value != null && value instanceof Number) {
      return ((Number) value).doubleValue();
    }
    return defaultValue;
  }

  public static String getString(Map<String, Object> config, String key) {
    return (String) config.get(key);
  }

  public static String getString(Map<String, Object> config, String key, String defaultValue) {
    Object value = config.get(key);
    if (value != null && value instanceof String) {
      return (String) value;
    }
    return defaultValue;
  }

  public static boolean getBoolean(Map<String, Object> conf, String key, boolean defaultValue) {
    Object value = conf.get(key);
    if (value != null && value instanceof Boolean) {
      return ((Boolean) value).booleanValue();
    }
    return defaultValue;
  }

  public static List<String> getListOfStrings(Map<String, Object> config, String key) {
    @SuppressWarnings("unchecked")
    List<String> tempList = (List<String>) config.get(key);
    return tempList;
    // return (List<String>)config.get(key);
  }

  public static void printConfig(Map<String, Object> conf) {
    // Objects.requireNonNull(conf, "Conf is null").forEach((k, v) -> System.out.println(k + " : " +
    // v));
    for (Map.Entry<String, Object> entry : conf.entrySet()) {
      System.out.println("\t" + entry.getKey() + ":" + entry.getValue());
    }
  }
}
