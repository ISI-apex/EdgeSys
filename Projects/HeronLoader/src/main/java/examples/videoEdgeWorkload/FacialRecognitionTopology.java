package workloads.topology;

import com.twitter.heron.api.Config;
import com.twitter.heron.api.HeronSubmitter;
import com.twitter.heron.api.topology.TopologyBuilder;
import com.twitter.heron.api.utils.Utils;
import com.twitter.heron.simulator.Simulator;
import examples.videoEdgeWorkload.DetectFacesBolt;
import examples.videoEdgeWorkload.ImageFetchSpout;
import examples.videoEdgeWorkload.ImageMarkBolt;
import examples.videoEdgeWorkload.ImageWriteBolt;
import examples.videoEdgeWorkload.PrintDebugBolt;
import examples.videoEdgeWorkload.tools.WorkloadConstants;

/** This is a basic example of a Storm topology. */
public final class FacialRecognitionTopology {

  private FacialRecognitionTopology() {}

  public static void main(String[] args) throws Exception {
    TopologyBuilder builder = new TopologyBuilder();
    int parallelism = 1;

    builder.setSpout("imageFetch", new ImageFetchSpout(), parallelism);
    builder
        .setBolt("detectFaces", new DetectFacesBolt(), parallelism)
        .shuffleGrouping("imageFetch", "default");
    builder
        .setBolt("imageMark", new ImageMarkBolt(), parallelism)
        .shuffleGrouping("detectFaces", "default");
    builder
        .setBolt("imageWrite", new ImageWriteBolt(true), parallelism)
        .shuffleGrouping("imageMark", "default");

    builder
        .setBolt(
            "printBolt", new PrintDebugBolt(true, WorkloadConstants.FIELD_IMAGE_ID), parallelism)
        .shuffleGrouping("imageWrite", "default");

    Config conf = new Config();
    // conf.setNumAckers(1);
    conf.setEnableAcking(true);
    conf.setDebug(true);
    conf.setMaxSpoutPending(10);
    conf.setMessageTimeoutSecs(600);
    // conf.put(Config.TOPOLOGY_WORKER_CHILDOPTS, "-XX:+HeapDumpOnOutOfMemoryError");
    // com.twitter.heron.api.Config.setComponentRam(conf, "spout", ByteAmount.fromGigabytes(0.5));
    // com.twitter.heron.api.Config.setComponentRam(conf, "bolt", ByteAmount.fromGigabytes(0.5));
    // com.twitter.heron.api.Config.setContainerDiskRequested(conf, ByteAmount.fromGigabytes(5));
    // com.twitter.heron.api.Config.setContainerCpuRequested(conf, 5);

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
