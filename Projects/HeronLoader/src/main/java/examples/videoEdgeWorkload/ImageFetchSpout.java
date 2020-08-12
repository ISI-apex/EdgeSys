package examples.videoEdgeWorkload;

import com.twitter.heron.api.metric.AssignableMetric;
import com.twitter.heron.api.spout.BaseRichSpout;
import com.twitter.heron.api.spout.SpoutOutputCollector;
import com.twitter.heron.api.topology.OutputFieldsDeclarer;
import com.twitter.heron.api.topology.TopologyContext;
import com.twitter.heron.api.tuple.Fields;
import com.twitter.heron.api.tuple.Values;
import examples.videoEdgeWorkload.tools.NativeUtils;
import examples.videoEdgeWorkload.tools.Serializable;
import examples.videoEdgeWorkload.tools.WorkloadConfig;
import examples.videoEdgeWorkload.tools.WorkloadConstants;
import java.util.*;
import java.util.zip.CRC32;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ImageFetchSpout extends BaseRichSpout {
  private static final Logger LOG = LoggerFactory.getLogger(ImageFetchSpout.class);

  private static final long serialVersionUID = -630302749138406294L;
  private SpoutOutputCollector collector;

  private String configName = "FacialRecognition.yaml";
  private long imageCount;
  private Serializable.Mat imageToSend;
  private CRC32 crcGen;
  private long nextTupleDelay = 1234;
  private ArrayList<Serializable.Mat> images = new ArrayList<Serializable.Mat>();
  private List<String> imageList;
  private Random rand;
  private int randomSeed;
  private int currentMode = 0;
  private int probabilityChangeMode = 0;
  private long lastTimeCheckedForModeChange = 0;
  private long modeChangePeriodNs = 0;
  private long lastTimeCheckedForAllocation = 0;
  private long nextEmitTime = 0;

  private AssignableMetric<Long> currentModeMetric;

  private long ackCount;

  public ImageFetchSpout() {}

  @Override
  public void open(
      Map<String, Object> conf, TopologyContext context, SpoutOutputCollector newCollector) {
    this.collector = newCollector;
    this.imageCount = 0;
    this.ackCount = 0;
    this.crcGen = new CRC32();

    // ** Load libraries
    nu.pattern.OpenCV.loadShared();
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

    // Load config
    String tempConfigFilePath = "";
    try {
      tempConfigFilePath =
          NativeUtils.extractTmpFileFromJar(
                  WorkloadConstants.CONFIG_DIRECTORY + this.configName,
                  true,
                  Integer.toString(context.getThisTaskId()))
              .getAbsolutePath();
    } catch (Exception e) {
      LOG.error("Error getting image!" + tempConfigFilePath);
      LOG.error(e.toString());
    }
    LOG.info("Config unpacked to: " + tempConfigFilePath);
    HashMap<String, Object> workloadConfig = null;
    try {
      workloadConfig = WorkloadConfig.readConfig(tempConfigFilePath);
    } catch (Exception e) {
      LOG.error("Error getting config!" + tempConfigFilePath);
      LOG.error(e.toString());
    }

    // Load workload parameters
    this.nextTupleDelay =
        WorkloadConfig.getLong(workloadConfig, WorkloadConstants.SPOUT_INPUT_DELAY_MS, 1122);
    this.imageList =
        WorkloadConfig.getListOfStrings(workloadConfig, WorkloadConstants.DATASET_IMAGE_NAMES);
    this.randomSeed = WorkloadConfig.getInt(workloadConfig, WorkloadConstants.RANDOM_SEED, 1);
    this.currentMode = WorkloadConfig.getInt(workloadConfig, WorkloadConstants.MODE_INITIAL, 0);
    this.probabilityChangeMode =
        WorkloadConfig.getInt(workloadConfig, WorkloadConstants.MODE_CHANGE_PROBABILITY, 50);
    if (this.probabilityChangeMode < 0 || this.probabilityChangeMode > 100) {
      LOG.error("ERROR: probabilityChangeMode out of range " + this.probabilityChangeMode);
      this.probabilityChangeMode = 50;
    }
    this.modeChangePeriodNs =
        (long)
            (WorkloadConfig.getInt(workloadConfig, WorkloadConstants.MODE_CHANGE_PERIOD, 10) * 1e9);
    this.rand = new Random(randomSeed);

    // Create metrics for Heron
    this.currentModeMetric = new AssignableMetric<Long>(Long.valueOf(this.currentMode));
    this.currentModeMetric.setValue(Long.valueOf(this.currentMode));
    context.registerMetric("imageFetchCurrentMode", this.currentModeMetric, 1);

    // Prepare output data
    for (String imageName : imageList) {
      LOG.info("Loading image: " + imageName);
      String tempImageFilePath = "";
      try {
        tempImageFilePath =
            NativeUtils.extractTmpFileFromJar(
                    WorkloadConstants.IMAGE_DATASET_DIRECTORY + imageName,
                    true,
                    Integer.toString(context.getThisTaskId()))
                .getAbsolutePath();
      } catch (Exception e) {
        LOG.error("Error getting image!" + tempImageFilePath);
        LOG.error(e.toString());
      }

      // Load image
      Mat image = Highgui.imread(tempImageFilePath);

      // Construct derived types
      Serializable.Mat testSMat = new Serializable.Mat(image);
      imageToSend = new Serializable.Mat(image);

      crcGen.reset();
      crcGen.update(imageToSend.toByteArray());

      images.add(imageToSend);
    }
  }

  @Override
  public void nextTuple() {
    if (System.nanoTime() < this.nextEmitTime) {
      // System.out.println("Not time yet");
      return;
    } else {
      this.nextEmitTime = System.nanoTime() + this.nextTupleDelay * (long) 1e6;
    }

    // Check for if mode should change
    if ((System.nanoTime() - this.lastTimeCheckedForModeChange) > this.modeChangePeriodNs) {
      if (this.rand.nextInt(100) < this.probabilityChangeMode) {
        this.currentMode = this.rand.nextInt(this.imageList.size());
        LOG.info("Mode change to: " + this.currentMode);
      } else {
      }
      this.lastTimeCheckedForModeChange = System.nanoTime();
    }
    this.currentModeMetric.setValue(Long.valueOf(this.currentMode));

    // Prepare output data
    Serializable.Mat tempSendImage = images.get(this.currentMode);
    crcGen.reset();
    crcGen.update(tempSendImage.toByteArray());

    LOG.info("\tImageId: " + imageCount + "\tnextTuple() CRC: " + crcGen.getValue());

    // Actually send data
    this.collector.emit(new Values(imageCount, this.currentMode, tempSendImage), imageCount);
    this.imageCount++;
  }

  public void declareOutputFields(OutputFieldsDeclarer declarer) {
    // declarer.declareStream(DynamoWorkloadConstants.STREAM_DEFAULT,
    declarer.declare(
        new Fields(
            WorkloadConstants.FIELD_IMAGE_ID,
            WorkloadConstants.FIELD_IMAGE_MODE,
            WorkloadConstants.FIELD_RAW_IMAGE_MAT));
  }

  @Override
  public void ack(Object msgId) {
    this.ackCount++;
    System.out.println("Got ack for: " + msgId);
  }
}
