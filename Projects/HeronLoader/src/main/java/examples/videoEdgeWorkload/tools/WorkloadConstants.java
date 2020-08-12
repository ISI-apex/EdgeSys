package examples.videoEdgeWorkload.tools;

public interface WorkloadConstants {
  // Config keys

  // Topology level
  public static final String RANDOM_SEED = "topology.randomSeed";
  public static final String CONFIG_DIRECTORY = "/config/";
  public static final String IMAGE_DATASET_DIRECTORY = "/imageDatasets/source/";

  // public static final String FIELD_TARGETS = "TARGETS";
  public static final String FIELD_TUPLE_TYPE = "TUPLE_TYPE";
  public static final String FIELD_IMAGE_ID = "IMAGE_ID";
  public static final String FIELD_IMAGE_MODE = "IMAGE_MODE";
  public static final String FIELD_RAW_IMAGE_MAT = "RAW_IMAGE_MAT";
  public static final String FIELD_FACES_RECT_ARRAYLIST = "FACES_RECT_ARRAYLIST";
  public static final String FIELD_RESULT_IMAGE_MAT = "RESULT_IMAGE";
  public static final String FIELD_NEW_ALLOCATION = "NEW_ALLOCATION";

  // public static final String MONITOR_PORT = "MONITOR_PORT";
  // public static final String MONITOR_URL = "MONITOR_URL";

  // Spout
  public static final String SPOUT_INPUT_DELAY_MS = "topology.inputDelayMs";
  public static final String DATASET_IMAGE_NAMES = "dataset.ImageNames";
  public static final String MODE_INITIAL = "topology.modeInitial";
  public static final String MODE_CHANGE_PROBABILITY = "topology.probabilityChangeMode";
  public static final String MODE_CHANGE_PERIOD = "topology.modeChangePeriod";
  public static final String UPDATE_ALLOCATION_PERIOD = "topology.updateAllocationPeriod";
  // Bolt
  public static final String CLASSIFIER_FILE_PATH =
      "/imageDatasets/classifiers/lbpcascade_frontalface.xml";
  public static final String OUTPUT_IMAGE_FILE_PATH = "./ouputImageWriteBolt";
  public static final String BOLT_DETECT_LOOP_COUNT = "topology.detectFaces.boltLoops";
  public static final String BOLT_MARK_LOOP_COUNT = "topology.imageMark.boltLoops";
  public static final String BOLT_WRITE_LOOP_COUNT = "topology.imageWrite.boltLoops";

  // Tuple types
  public static final String TUPLE_TYPE_COMMAND = "tupleCommand";
  public static final String TUPLE_TYPE_NORMAL = "tupleNormal";

  // Stream types
  // public static final String STREAM_DEFAULT = "default";
  // public static final String STREAM_COMMAND = "COMMAND_STREAM";

}
