package edgesys.util;

import com.twitter.heron.api.Config;
import com.twitter.heron.api.HeronSubmitter;
import com.twitter.heron.api.topology.TopologyBuilder;
import com.twitter.heron.api.tuple.Fields;
import com.twitter.heron.api.utils.Utils;
import com.twitter.heron.simulator.Simulator;
import edgesys.util.EdgeSysTopologyBuilder;
import examples.videoEdgeWorkload.DetectFacesBolt;
import examples.videoEdgeWorkload.ImageFetchSpout;
import examples.videoEdgeWorkload.ImageMarkBolt;
import examples.videoEdgeWorkload.ImageWriteBolt;
import examples.videoEdgeWorkload.PrintDebugBolt;
import examples.videoEdgeWorkload.tools.WorkloadConstants;

/** This is a basic example of a Storm topology. */
public final class TestBuilder {

  private TestBuilder() {}

  public static void main(String[] args) throws Exception {
    TopologyBuilder builder = new EdgeSysTopologyBuilder();
    int parallelism = 3;

    // builder.setSpout("imageFetch", new ImageFetchSpout(), parallelism);
    // builder
    //     .setBolt("detectFaces", new DetectFacesBolt(), parallelism)
    //     .shuffleGrouping("imageFetch", "default");
    // builder
    //     .setBolt("imageMark", new ImageMarkBolt(), parallelism)
    //     .shuffleGrouping("detectFaces", "default");
    builder
        .setBolt("imageWrite", new ImageWriteBolt(true), parallelism)
        // .shuffleGrouping("imageMark", "default");
        .fieldsGrouping(
            "imageMark",
            "default",
            new Fields("IMAGE_ID", "IMAGE_MODE", "RAW_IMAGE_MAT", "RESULT_IMAGE"));

    builder
        .setBolt(
            "printBolt", new PrintDebugBolt(true, WorkloadConstants.FIELD_IMAGE_ID), parallelism)
        .shuffleGrouping("imageWrite", "default");

    // builder.setSpout("imageFetch", new ImageFetchSpout(), parallelism);
    // builder
    //     .setBolt("detectFaces", new DetectFacesBolt(), parallelism)
    //     .shuffleGrouping("imageFetch", "default");
    // builder
    //     .setBolt("imageMark", new ImageMarkBolt(), parallelism)
    //     .shuffleGrouping("detectFaces", "default");
    // builder
    //     .setBolt("imageWrite", new ImageWriteBolt(true), parallelism)
    //     // .shuffleGrouping("imageMark", "default");
    //     .fieldsGrouping(
    //         "imageMark",
    //         "default",
    //         new Fields("IMAGE_ID", "IMAGE_MODE", "RAW_IMAGE_MAT", "RESULT_IMAGE"));

    // builder
    //     .setBolt(
    //         "printBolt", new PrintDebugBolt(true, WorkloadConstants.FIELD_IMAGE_ID), parallelism)
    //     .shuffleGrouping("imageWrite", "default");

    Config conf = new Config();
    // conf.setNumAckers(1);
    conf.setEnableAcking(true);
    conf.setDebug(true);
    conf.setMaxSpoutPending(10);
    conf.setMessageTimeoutSecs(600);

    System.out.println("About to build");
    builder.createTopology();
    System.exit(0);
    if (args != null && args.length > 0) {
      // if(true)
      // 	throw new RuntimeException("direct Grouping not implemented");

      conf.setNumStmgrs(parallelism);
      HeronSubmitter.submitTopology(args[0], conf, builder.createTopology());
    } else {
      System.out.println("Topology name not provided as an argument, running in simulator mode.");
      Simulator simulator = new Simulator();
      simulator.submitTopology("test", conf, builder.createTopology());
      Utils.sleep(10000);
      simulator.killTopology("test");
      simulator.shutdown();
    }
  }
}
