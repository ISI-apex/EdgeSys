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
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ImageMarkBolt extends BaseRichBolt {
  private static final Logger LOG = LoggerFactory.getLogger(ImageMarkBolt.class);
  private static final long serialVersionUID = -2261338658311778214L;
  private OutputCollector collector;
  private long imageCount;
  private Serializable.Mat imageReceived;
  private CRC32 crcGen;
  private String configName = "FacialRecognition.yaml";
  private Integer loopCount;

  public ImageMarkBolt() {}

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
    HashMap<String, Object> workloadConfig = null;
    try {
      workloadConfig = WorkloadConfig.readConfig(tempConfigFilePath);
    } catch (Exception e) {
      LOG.error("Error getting config!" + tempConfigFilePath);
      LOG.error(e.toString());
    }
    this.loopCount =
        WorkloadConfig.getInt(workloadConfig, WorkloadConstants.BOLT_MARK_LOOP_COUNT, 1);
  }

  @Override
  @SuppressWarnings("unchecked")
  public void execute(Tuple tuple) {
    imageReceived = (Serializable.Mat) tuple.getValueByField(WorkloadConstants.FIELD_RAW_IMAGE_MAT);

    crcGen.reset();
    crcGen.update(imageReceived.toByteArray());

    ArrayList<Serializable.Rect> tempArrayListRect =
        (ArrayList<Serializable.Rect>)
            tuple.getValueByField(WorkloadConstants.FIELD_FACES_RECT_ARRAYLIST);

    // Mark faces on image
    Mat resultImage = imageReceived.toOpenCVMat();
    for (Integer i = 0; i < this.loopCount; i++) {
      resultImage = imageReceived.toOpenCVMat();
      for (Serializable.Rect tempSRect : tempArrayListRect) {
        Core.rectangle(
            resultImage,
            new Point(tempSRect.x, tempSRect.y),
            new Point(tempSRect.x + tempSRect.width, tempSRect.y + tempSRect.height),
            new Scalar(0, 255, 0));
      }
    }
    imageCount++;

    LOG.info(
        String.format(
            "\t\tImageId: %d\tMarked %s faces",
            tuple.getLongByField(WorkloadConstants.FIELD_IMAGE_ID), tempArrayListRect.size()));

    this.collector.emit(
        tuple,
        new Values(
            tuple.getLongByField(WorkloadConstants.FIELD_IMAGE_ID),
            tuple.getIntegerByField(WorkloadConstants.FIELD_IMAGE_MODE),
            imageReceived,
            new Serializable.Mat(resultImage)));
    this.collector.ack(tuple);
  }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer declarer) {
    declarer.declareStream(
        "default",
        new Fields(
            WorkloadConstants.FIELD_IMAGE_ID,
            WorkloadConstants.FIELD_IMAGE_MODE,
            WorkloadConstants.FIELD_RAW_IMAGE_MAT,
            WorkloadConstants.FIELD_RESULT_IMAGE_MAT));
  }
}
