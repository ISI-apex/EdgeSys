package examples.videoEdgeWorkload;

import com.twitter.heron.api.bolt.BaseRichBolt;
import com.twitter.heron.api.bolt.OutputCollector;
import com.twitter.heron.api.topology.OutputFieldsDeclarer;
import com.twitter.heron.api.topology.TopologyContext;
import com.twitter.heron.api.tuple.Fields;
import com.twitter.heron.api.tuple.Tuple;
import com.twitter.heron.api.tuple.Values;
import examples.videoEdgeWorkload.tools.NativeUtils;
import examples.videoEdgeWorkload.tools.Serializable;
import examples.videoEdgeWorkload.tools.WorkloadConfig;
import examples.videoEdgeWorkload.tools.WorkloadConstants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.CRC32;
import org.opencv.core.Core;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.objdetect.CascadeClassifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DetectFacesBolt extends BaseRichBolt {
  private static final Logger LOG = LoggerFactory.getLogger(DetectFacesBolt.class);
  private static final long serialVersionUID = -2267658618318978214L;
  private OutputCollector collector;
  private long imageCount;
  private Serializable.Mat imageReceived;
  private CRC32 crcGen;
  private CascadeClassifier faceDetector;
  private String configName = "FacialRecognition.yaml";
  private Integer loopCount;

  public DetectFacesBolt() {}

  @Override
  public void prepare(Map conf, TopologyContext context, OutputCollector acollector) {

    this.collector = acollector;
    this.imageCount = 0;
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
    this.loopCount =
        WorkloadConfig.getInt(workloadConfig, WorkloadConstants.BOLT_DETECT_LOOP_COUNT, 1);

    // Load classifier for OpenCV
    String tempClassifierPath = "";
    try {
      tempClassifierPath =
          NativeUtils.extractTmpFileFromJar(
                  WorkloadConstants.CLASSIFIER_FILE_PATH,
                  true,
                  Integer.toString(context.getThisTaskId()))
              .getAbsolutePath();
    } catch (Exception e) {
      LOG.error("Error getting classifier! " + tempClassifierPath);
      LOG.error(e.toString());
    }
    this.faceDetector = new CascadeClassifier(tempClassifierPath);
  }

  @Override
  public void execute(Tuple tuple) {
    // Unpack image
    imageReceived = (Serializable.Mat) tuple.getValueByField(WorkloadConstants.FIELD_RAW_IMAGE_MAT);

    // Test CRC of image
    crcGen.reset();
    crcGen.update(imageReceived.toByteArray());

    // Start processing image
    MatOfRect faceDetections = new MatOfRect();
    for (Integer i = 0; i < this.loopCount; i++) {
      faceDetections = new MatOfRect();
      faceDetector.detectMultiScale(imageReceived.toOpenCVMat(), faceDetections);
    }

    // Convert MatOfRect to ArrayList<Serializable.Rect>
    ArrayList<Serializable.Rect> faceDetectionArrayList = new ArrayList<Serializable.Rect>();
    for (Rect tempRect : faceDetections.toList()) {
      faceDetectionArrayList.add(new Serializable.Rect(tempRect));
    }

    LOG.info(
        String.format(
            "\t\tImageId: %d\tDetected %s faces",
            tuple.getLongByField(WorkloadConstants.FIELD_IMAGE_ID),
            faceDetections.toArray().length));

    // Emit figure with face rects
    this.collector.emit(
        tuple,
        new Values(
            tuple.getLongByField(WorkloadConstants.FIELD_IMAGE_ID),
            tuple.getIntegerByField(WorkloadConstants.FIELD_IMAGE_MODE),
            imageReceived,
            faceDetectionArrayList));
    this.collector.ack(tuple);
    imageCount++;
  }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer declarer) {
    declarer.declareStream(
        "default",
        new Fields(
            WorkloadConstants.FIELD_IMAGE_ID,
            WorkloadConstants.FIELD_IMAGE_MODE,
            WorkloadConstants.FIELD_RAW_IMAGE_MAT,
            WorkloadConstants.FIELD_FACES_RECT_ARRAYLIST));
  }
}
