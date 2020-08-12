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
import java.util.HashMap;
import java.util.Map;
import java.util.zip.CRC32;
import org.opencv.core.Core;
import org.opencv.highgui.Highgui;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ImageWriteBolt extends BaseRichBolt {
  private static final Logger LOG = LoggerFactory.getLogger(ImageWriteBolt.class);
  private static final long serialVersionUID = -2261338123317778214L;
  private OutputCollector collector;
  private long imageCount;
  private Serializable.Mat imageReceived;
  private CRC32 crcGen;
  private boolean bWriteImage = false;
  private String configName = "FacialRecognition.yaml";
  private Integer loopCount;

  public ImageWriteBolt(boolean bWriteImage) {
    this.bWriteImage = bWriteImage;
  }

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
        WorkloadConfig.getInt(workloadConfig, WorkloadConstants.BOLT_WRITE_LOOP_COUNT, 1);
  }

  @Override
  public void execute(Tuple tuple) {
    imageReceived = (Serializable.Mat) tuple.getValueByField(WorkloadConstants.FIELD_RAW_IMAGE_MAT);
    Serializable.Mat resultImage;
    if (tuple.contains(WorkloadConstants.FIELD_RESULT_IMAGE_MAT)) {
      resultImage =
          (Serializable.Mat) tuple.getValueByField(WorkloadConstants.FIELD_RESULT_IMAGE_MAT);
      crcGen.reset();
      crcGen.update(imageReceived.toByteArray());
      LOG.info(
          "\t"
              + tuple.getLongByField(WorkloadConstants.FIELD_IMAGE_ID)
              + "\tImageWrite Received CRC: "
              + crcGen.getValue());

      if (bWriteImage) {
        for (Integer i = 0; i < this.loopCount; i++) {
          Highgui.imwrite(
              WorkloadConstants.OUTPUT_IMAGE_FILE_PATH
                  + "-"
                  + tuple.getLongByField(WorkloadConstants.FIELD_IMAGE_ID)
                  + ".png",
              resultImage.toOpenCVMat());
        }
      }
    } else {
      // Must be testing write since there is no result image
      if (bWriteImage) {
        for (Integer i = 0; i < this.loopCount; i++) {
          Highgui.imwrite(
              WorkloadConstants.OUTPUT_IMAGE_FILE_PATH + ".png", imageReceived.toOpenCVMat());
        }
      }
    }

    imageCount++;
    this.collector.emit(
        tuple,
        new Values(
            tuple.getLongByField(WorkloadConstants.FIELD_IMAGE_ID),
            tuple.getIntegerByField(WorkloadConstants.FIELD_IMAGE_MODE)));
    this.collector.ack(tuple);
  }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer declarer) {
    declarer.declareStream(
        "default",
        new Fields(WorkloadConstants.FIELD_IMAGE_ID, WorkloadConstants.FIELD_IMAGE_MODE));
  }
}
