package edgesys.util;

import com.twitter.heron.api.Config;
import com.twitter.heron.api.HeronTopology;
import com.twitter.heron.api.bolt.BasicBoltExecutor;
import com.twitter.heron.api.bolt.IBasicBolt;
import com.twitter.heron.api.bolt.IRichBolt;
import com.twitter.heron.api.bolt.OutputCollector;
import com.twitter.heron.api.generated.TopologyAPI;
import com.twitter.heron.api.grouping.CustomStreamGrouping;
import com.twitter.heron.api.spout.IRichSpout;
import com.twitter.heron.api.spout.SpoutOutputCollector;
import com.twitter.heron.api.topology.BoltDeclarer;
import com.twitter.heron.api.topology.OutputFieldsDeclarer;
import com.twitter.heron.api.topology.SpoutDeclarer;
import com.twitter.heron.api.topology.TopologyBuilder;
import com.twitter.heron.api.topology.TopologyContext;
import com.twitter.heron.api.tuple.Fields;
import com.twitter.heron.api.tuple.Tuple;
import com.twitter.heron.api.utils.Utils;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;

// import dynamo2.tools.DynamoConstants;

public class EdgeSysTopologyBuilder extends TopologyBuilder {
  private LinkedList<DeploymentInfo> deploymentInfos = new LinkedList<DeploymentInfo>();
  Map<Object, Object> runTimeConfig = new HashMap<Object, Object>();

  LinkedList<DelegateBoltDeclarer> tempBoltDeclarers = new LinkedList<DelegateBoltDeclarer>();

  public HeronTopology createTopology() {

    System.out.println("Create topology");

    DeploymentInfo tempInfo;

    // Debug output
    System.out.format("Info\t%-15s%-50s%-15s\n", "Name", "Class", "Index");
    for (int i = 0; i < deploymentInfos.size(); ++i) {
      tempInfo = deploymentInfos.get(i);
      System.out.format("\t%-15s%-50s%-15s\n", tempInfo.name, tempInfo.className, tempInfo.idx);
    }

    // Write out deployment commands to file
    FileWriter fileWriter;
    try {
      fileWriter = new FileWriter("testOutputCommands.txt");
      PrintWriter printWriter = new PrintWriter(fileWriter);
      // printWriter.print("Some String");
      // printWriter.printf("Product name is %s and its price is %d $", "iPhone", 1000);
      for (int i = 0; i < deploymentInfos.size(); ++i) {
        tempInfo = deploymentInfos.get(i);
        printWriter.print(
            "java -cp \"target/*\" edgesys.EdgeSysHeronExecutor "
                // "java -cp \"JAR_FILE\" edgesys.EdgeSysHeronExecutor "
                + tempInfo.className
                + " "
                + tempInfo.name
                + " "
                + tempInfo.idx
                // Test stuff
                + " "
                + "testOutputConfig.yaml"
                + " localhost" // inputHostName
                + " localhost" // outputHostname
                // End test stuff

                + " \n");
        // System.out.println(
        //     "java -cp \"JAR_FILE\" edgesys.EdgeSysHeronExecutor "
        //         + tempInfo.className
        //         + " "
        //         + tempInfo.name
        //         + " "
        //         + tempInfo.idx
        //         + " "
        //         );
      }
      printWriter.close();
    } catch (IOException e1) {
      // TODO Auto-generated catch block
      System.out.println("Error writing commands to file");
      e1.printStackTrace();
    }

    // Build targets for streams

    // Debug print
    Yaml yaml2 = new Yaml();
    String output = yaml2.dump(runTimeConfig);
    System.out.println(output);

    for (DelegateBoltDeclarer boltDeclarer : tempBoltDeclarers) {
      for (LinkedList<String> shuffleGrouping : boltDeclarer.shuffleGroupings) {
        System.out.println(shuffleGrouping);

        // Store shuffle groupings
        HashMap<String, String> tempShuffleTarget = new HashMap<String, String>();
        tempShuffleTarget.put("type", "shuffle");

        if (runTimeConfig.containsKey(shuffleGrouping.get(0))) {
          @SuppressWarnings("unchecked")
          HashMap<String, Object> targetConfig =
              ((HashMap<String, Object>) runTimeConfig.get(shuffleGrouping.get(0)));

          @SuppressWarnings("unchecked")
          HashMap<String, Object> streamConfigs =
              ((HashMap<String, Object>) targetConfig.get("streams"));

          @SuppressWarnings("unchecked")
          HashMap<String, Object> streamConfig =
              ((HashMap<String, Object>) streamConfigs.get(shuffleGrouping.get(1)));

          @SuppressWarnings("unchecked")
          HashMap<String, Object> targetConfigs =
              ((HashMap<String, Object>) streamConfig.get("targets"));
          targetConfigs.put(boltDeclarer.name, tempShuffleTarget);
        } else {
          System.out.println("Skipping " + shuffleGrouping.get(0) + " as it must be a spout");
        }
      }

      for (LinkedList<Object> fieldsGrouping : boltDeclarer.fieldsGroupings) {
        System.out.println(fieldsGrouping);
        HashMap<String, Object> tempFieldsTarget = new HashMap<String, Object>();
        tempFieldsTarget.put("type", "fields");
        tempFieldsTarget.put("targetFields", fieldsGrouping.get(2));

        if (runTimeConfig.containsKey(fieldsGrouping.get(0))) {
          @SuppressWarnings("unchecked")
          HashMap<String, Object> targetConfig =
              ((HashMap<String, Object>) runTimeConfig.get(fieldsGrouping.get(0)));

          @SuppressWarnings("unchecked")
          HashMap<String, Object> streamConfigs =
              ((HashMap<String, Object>) targetConfig.get("streams"));

          @SuppressWarnings("unchecked")
          HashMap<String, Object> streamConfig =
              ((HashMap<String, Object>) streamConfigs.get(fieldsGrouping.get(1)));

          @SuppressWarnings("unchecked")
          HashMap<String, Object> targetConfigs =
              ((HashMap<String, Object>) streamConfig.get("targets"));
          targetConfigs.put(boltDeclarer.name, tempFieldsTarget);
        } else {
          System.out.println("Skipping " + fieldsGrouping.get(0) + " as it must be a spout");
        }
      }
    }

    // Write stream targets to configs
    Yaml yaml = new Yaml();
    try {
      FileWriter writer = new FileWriter("testOutputConfig.yaml");
      yaml.dump(runTimeConfig, writer);
    } catch (Exception e) {
      System.out.println("Failed to write output config");
    }

    System.out.println("Deployment commands and config file generated! " + "Exiting now.");
    System.exit(0);
    return null;
  }

  public BoltDeclarer setBolt(String boltName, IRichBolt bolt, Number parallelismHint) {
    // Create delegate declarer: used because groupings are defined later
    DelegateBoltDeclarer tempBoltDeclarer =
        new DelegateBoltDeclarer(super.setBolt(boltName, bolt, parallelismHint), boltName);
    tempBoltDeclarers.add(tempBoltDeclarer);

    Map<Object, Object> tempObject = new HashMap<Object, Object>();

    // Dump serialized instance to file
    byte[] testArray = Utils.serialize(bolt);
    try {
      Path path = Paths.get("serializedBolt-" + boltName + ".bin");
      Files.write(path, testArray);
    } catch (IOException e) {
      System.out.println("Error dumping to file");
      e.printStackTrace();
    }

    // Get stream and fields information from bolt
    DelegateOutputFieldsDeclarer tempDeclarer = new DelegateOutputFieldsDeclarer();
    bolt.declareOutputFields(tempDeclarer);

    // Update config
    tempObject.put("streams", tempDeclarer.streams);
    tempObject.put("instances", parallelismHint);

    // Add to deployment info
    for (int i = 0; i < ((Integer) parallelismHint); ++i) {
      // bolt.getClass().getName()
      deploymentInfos.add(new DeploymentInfo(boltName, boltName, i));
    }

    runTimeConfig.put(boltName, tempObject);

    // return super.setBolt(boltName, bolt, parallelismHint);
    return tempBoltDeclarer;
  }

  public SpoutDeclarer setSpout(String id, IRichSpout spout, Number parallelismHint) {
    // TODO: do stuff here, setup input queues
    return super.setSpout(id, spout, parallelismHint);
  }

  public SpoutDeclarer setSpout(String id, IRichSpout spout) {
    return setSpout(id, spout, null);
  }

  public BoltDeclarer setBolt(String id, IRichBolt bolt) {
    return setBolt(id, bolt, null);
  }

  public BoltDeclarer setBolt(String id, IBasicBolt bolt) {
    return setBolt(id, bolt, null);
  }

  public BoltDeclarer setBolt(String id, IBasicBolt bolt, Number parallelismHint) {
    return setBolt(id, new BasicBoltExecutor(bolt), parallelismHint);
  }

  public static class DeploymentInfo {
    String name;
    String className;
    Integer idx;

    DeploymentInfo(String name, String className, Integer idx) {
      this.name = name;
      this.className = className;
      this.idx = idx;
    }
  }

  public static class DelegateOutputFieldsDeclarer implements OutputFieldsDeclarer {
    Map<String, Object> streams = new HashMap<String, Object>();

    public void declare(com.twitter.heron.api.tuple.Fields fields) {
      declareStream("default", fields);
    }

    public void declare(boolean direct, com.twitter.heron.api.tuple.Fields fields) {
      System.out.println("NOT IMPLEMENTED");
    }

    public void declareStream(
        java.lang.String streamId, com.twitter.heron.api.tuple.Fields fields) {
      Map<Object, Object> tempStreamInfo = new HashMap<Object, Object>();
      tempStreamInfo.put("fields", fields.toList());
      tempStreamInfo.put("targets", new HashMap<Object, Object>());

      streams.put(streamId, tempStreamInfo);
    }

    public void declareStream(
        java.lang.String streamId, boolean direct, com.twitter.heron.api.tuple.Fields fields) {
      System.out.println("NOT IMPLEMENTED");
    }
  }

  public static class DelegateBoltDeclarer extends BoltDeclarer {
    // private static final Logger LOG =
    // 		LoggerFactory.getLogger(DelegateBoltDeclarer.class);
    private BoltDeclarer delegate;
    private String name;

    private LinkedList<LinkedList<String>> shuffleGroupings = new LinkedList<LinkedList<String>>();
    private LinkedList<LinkedList<Object>> fieldsGroupings = new LinkedList<LinkedList<Object>>();

    public DelegateBoltDeclarer(BoltDeclarer delegate, String name) {
      super(null, new StubComponent(), null);
      this.delegate = delegate;
      this.name = name;
    }

    public BoltDeclarer addConfiguration(java.lang.String config, java.lang.Object value) {
      return this.delegate.addConfiguration(config, value);
    }

    public BoltDeclarer addConfigurations(java.util.Map<java.lang.String, java.lang.Object> conf) {
      return this.delegate.addConfigurations(conf);
    }

    public BoltDeclarer setDebug(boolean debug) {
      return this.delegate.setDebug(debug);
    }

    public BoltDeclarer setMaxSpoutPending(java.lang.Number val) {
      return this.delegate.setMaxSpoutPending(val);
    }

    // public String getName() {
    // 	return this.delegate.getName();
    // }

    public void dump(TopologyAPI.Topology.Builder bldr) {
      this.delegate.dump(bldr);
    }

    public BoltDeclarer returnThis() {
      return this.delegate.returnThis();
    }

    /************************START HERE********************/
    public BoltDeclarer allGrouping(String componentName) {
      return this.allGrouping(componentName, Utils.DEFAULT_STREAM_ID);
    }

    public BoltDeclarer allGrouping(String componentName, String streamId) {
      return this.delegate.allGrouping(componentName, streamId);
    }

    public BoltDeclarer customGrouping(String componentName, CustomStreamGrouping grouping) {
      return this.customGrouping(componentName, Utils.DEFAULT_STREAM_ID, grouping);
    }

    public BoltDeclarer customGrouping(
        String componentName, String streamId, CustomStreamGrouping grouping) {
      return this.delegate.customGrouping(componentName, streamId, grouping);
    }

    public BoltDeclarer directGrouping(String componentName) {
      return this.directGrouping(componentName, Utils.DEFAULT_STREAM_ID);
    }

    public BoltDeclarer directGrouping(String componentName, String streamId) {
      return this.delegate.directGrouping(componentName, streamId);
    }

    public BoltDeclarer fieldsGrouping(String componentName, Fields fields) {
      return this.fieldsGrouping(componentName, Utils.DEFAULT_STREAM_ID, fields);
    }

    public BoltDeclarer fieldsGrouping(String componentName, String streamId, Fields fields) {

      // Save fields grouping and fields
      LinkedList<Object> tempGrouping = new LinkedList<Object>();
      tempGrouping.add(componentName);
      tempGrouping.add(streamId);
      tempGrouping.add(fields.toList());
      fieldsGroupings.add(tempGrouping);

      // We declare everything as direct grouping so that Heron does not send
      // two tuples, one as direct and one as the "actual" grouping
      // return this.delegate.fieldsGrouping(componentName, streamId, fields);
      return this.delegate.directGrouping(componentName, streamId);
    }

    public BoltDeclarer globalGrouping(String componentName) {
      return this.globalGrouping(componentName, Utils.DEFAULT_STREAM_ID);
    }

    public BoltDeclarer globalGrouping(String componentName, String streamId) {
      return this.delegate.globalGrouping(componentName, streamId);
    }

    public BoltDeclarer localOrShuffleGrouping(String componentName) {
      return this.localOrShuffleGrouping(componentName, Utils.DEFAULT_STREAM_ID);
    }

    public BoltDeclarer localOrShuffleGrouping(String componentName, String streamId) {
      return this.delegate.localOrShuffleGrouping(componentName, streamId);
    }

    public BoltDeclarer noneGrouping(String componentName) {
      return this.noneGrouping(componentName, Utils.DEFAULT_STREAM_ID);
    }

    public BoltDeclarer noneGrouping(String componentName, String streamId) {
      return this.delegate.noneGrouping(componentName, streamId);
    }

    public BoltDeclarer shuffleGrouping(String componentName) {
      return this.shuffleGrouping(componentName, Utils.DEFAULT_STREAM_ID);
    }

    public BoltDeclarer shuffleGrouping(String componentName, String streamId) {
      LinkedList<String> tempGrouping = new LinkedList<String>();
      tempGrouping.add(componentName);
      tempGrouping.add(streamId);
      shuffleGroupings.add(tempGrouping);

      // We declare everything as direct grouping so that Heron does not send
      // two tuples, one as direct and one as the "actual" grouping
      // return this.delegate.shuffleGrouping(componentName, streamId);
      return this.delegate.directGrouping(componentName, streamId);
    }
  }

  public static class DelegateSpoutDeclarer extends SpoutDeclarer {
    // private static final Logger LOG =
    // 		LoggerFactory.getLogger(DelegateSpoutDeclarer.class);
    private SpoutDeclarer delegate;
    private String name;
    private Config conf;

    public DelegateSpoutDeclarer(SpoutDeclarer delegate, String name, Config conf) {
      super(null, new StubComponent(), null);
      this.delegate = delegate;
      this.name = name;
      this.conf = conf;
    }

    public SpoutDeclarer addConfiguration(java.lang.String config, java.lang.Object value) {
      return this.delegate.addConfiguration(config, value);
    }

    public SpoutDeclarer addConfigurations(java.util.Map<java.lang.String, java.lang.Object> conf) {
      return this.delegate.addConfigurations(conf);
    }

    public SpoutDeclarer setDebug(boolean debug) {
      return this.delegate.setDebug(debug);
    }

    public SpoutDeclarer setMaxSpoutPending(java.lang.Number val) {
      return this.delegate.setMaxSpoutPending(val);
    }

    // public String getName() {
    // 	return this.delegate.getName();
    // }

    public void dump(TopologyAPI.Topology.Builder bldr) {
      this.delegate.dump(bldr);
    }

    public SpoutDeclarer returnThis() {
      return this.delegate.returnThis();
    }
  }

  private static class StubComponent implements IRichSpout, IRichBolt {
    private static final long serialVersionUID = 2779421697541595810L;

    public void declareOutputFields(OutputFieldsDeclarer declarer) {}

    public Map<String, Object> getComponentConfiguration() {
      return null;
    }

    public void open(
        Map<String, Object> conf, TopologyContext context, SpoutOutputCollector collector) {}

    public void close() {}

    public void activate() {}

    public void deactivate() {}

    public void nextTuple() {}

    public void ack(Object msgId) {}

    public void fail(Object msgId) {}

    public void prepare(
        Map<String, Object> heronConf, TopologyContext context, OutputCollector collector) {}

    public void execute(Tuple input) {}

    public void cleanup() {}
  }
}
