package edgesys;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.ByteBufferOutput;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
// import dynamo2.tools.DynamoConstants;
import com.twitter.heron.api.Config;
import com.twitter.heron.api.bolt.BasicBoltExecutor;
import com.twitter.heron.api.bolt.IBasicBolt;
import com.twitter.heron.api.bolt.IOutputCollector;
import com.twitter.heron.api.bolt.IRichBolt;
import com.twitter.heron.api.bolt.OutputCollector;
import com.twitter.heron.api.generated.TopologyAPI;
import com.twitter.heron.api.grouping.CustomStreamGrouping;
import com.twitter.heron.api.hooks.ITaskHook;
import com.twitter.heron.api.metric.CombinedMetric;
import com.twitter.heron.api.metric.ICombiner;
import com.twitter.heron.api.metric.IMetric;
import com.twitter.heron.api.metric.IReducer;
import com.twitter.heron.api.metric.ReducedMetric;
import com.twitter.heron.api.spout.IRichSpout;
import com.twitter.heron.api.spout.SpoutOutputCollector;
import com.twitter.heron.api.topology.BoltDeclarer;
import com.twitter.heron.api.topology.ComponentConfigurationDeclarer;
import com.twitter.heron.api.topology.IComponent;
import com.twitter.heron.api.topology.OutputFieldsDeclarer;
import com.twitter.heron.api.topology.SpoutDeclarer;
import com.twitter.heron.api.topology.TopologyBuilder;
import com.twitter.heron.api.topology.TopologyContext;
import com.twitter.heron.api.tuple.Fields;
import com.twitter.heron.api.tuple.Tuple;
import com.twitter.heron.api.tuple.Values;
import com.twitter.heron.api.utils.Utils;
import com.twitter.heron.common.utils.topology.GeneralTopologyContextImpl;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.yaml.snakeyaml.Yaml;

import edgesys.util.EdgeSysTuple;
import examples.videoEdgeWorkload.tools.WorkloadConstants;

// Todos:
// - Test executing tuple and sending tuples
// - Test receiving tuples

class EdgeSysHeronExecutor {

  public static void main(String[] args) throws Exception {
    String boltClassName;
    String boltInstanceName;
    Integer instanceIndex;
    String configFileName;
    String inputHostName;
    String outputHostName;

    IRichBolt boltInstance=null;

    String inputQueueName;
    String inputExchangeName;
    String outputExchangeName;

    System.out.println("Hello world: #args: " + args.length);
    System.out.println(Arrays.toString(args));

    // Get configuration from command line
    if (args.length != 6) {
      System.out.println("Usage: " + EdgeSysHeronExecutor.class.getName()
          + " BoltClassName instanceBoltName instanceIndex configFile inputHost outputHost");
    }
    boltClassName = args[0];
    boltInstanceName = args[1];
    instanceIndex = Integer.valueOf(args[2]);
    configFileName = args[3];
    inputHostName = args[4];
    outputHostName = args[5];

    // If invalid, quit with message

    // Load configuration file and configs
    Yaml yaml = new Yaml();
    InputStream inputStream = new FileInputStream(new File(configFileName));

    // Load from file in JAR
    // InputStream inputStream = this.getClass()
    // .getClassLoader()
    // .getResourceAsStream("customer.yaml");

    @SuppressWarnings("unchecked")
    Map<String, Object> runTimeConfig = (Map<String, Object>) yaml.load(inputStream);

    String output = yaml.dump(runTimeConfig);
    System.out.println(output);

    // Create OutputCollectors
    outputExchangeName = "testExchange";
    EdgeSysOutputCollector edgeSysOutputCollector = new EdgeSysOutputCollector(outputHostName, outputExchangeName,
        runTimeConfig);

    // Load bolt class
    // boltInstance = loadRichBolt(boltClassName);
    // Load serialized instance instead of new
    byte[] data2=null;
    try {
      Path path = Paths.get("serializedBolt-"+boltInstanceName+".bin");
      data2 = Files.readAllBytes(path);
      boltInstance = (IRichBolt) Utils.deserialize(data2);
    } catch (IOException e) {
      System.out.println("Error reading from file");
      e.printStackTrace();
      System.exit(0);
    }
    boltInstance.prepare(
        // Note: Might need to construct heron config here
        null, new EdgeSysStubTopologyContext(), edgeSysOutputCollector);

    // **** TODO: remove this
    // At this point we should be able to test outputs
    for (int i = 0; i < 5; ++i) {
      Tuple tempTuple = new EdgeSysTuple(
        "default", 
        new Fields(WorkloadConstants.FIELD_IMAGE_ID, "message"),
        new Values(2121+i, "HelloWorldTest"),
        "testComponentStub",
        5);
      boltInstance.execute(tempTuple);
    }

    // Create input readers, loop and execute
    inputQueueName = boltInstanceName + "_" + instanceIndex;
    inputExchangeName = "testExchange";
    // runConsumer(
    // boltInstance, edgeSysOutputCollector, inputQueueName, inputHostName,
    // inputExchangeName);

    edgeSysOutputCollector.close();
    System.out.println("End of main...");
  }

  public static class EdgeSysHeronTupleData {
    String value;
    EdgeSysTuple tuple;
  }

  static void runConsumer(IRichBolt boltInstance, EdgeSysOutputCollector outputCollector, String routingKey,
      String inputHost, String inputExchange) throws Exception {

    Kryo kryo = new Kryo();
    kryo.register(EdgeSysHeronTupleData.class);

    // Create connection and channel
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(inputHost);
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    // Create exchange
    channel.exchangeDeclare(inputExchange, BuiltinExchangeType.DIRECT);

    // // Declare queue so that it exists before we consume from it
    // channel.queueDeclare(queueName, false, false, false, null);

    // Get temporary queue that will be automatically deleted
    String tempQueueName = channel.queueDeclare().getQueue();

    // Bind specific routing key
    channel.queueBind(
      tempQueueName, // Queue name
      inputExchange, // Exchange name
      routingKey // routingKey
      );

    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
      // ByteBuffer tempBuf = output.getByteBuffer();
      ByteBufferInput input = new ByteBufferInput(delivery.getBody());

      // Deserialize data
      EdgeSysHeronTupleData receivedData = kryo.readObject(input, EdgeSysHeronTupleData.class);
      input.close();

      // Error handling
      // // assertThat(receivedData.value).isEqualTo(object.value);
      // System.out.println("Deserialized value: " + receivedData.value);

      // // String message = new String(delivery.getBody(), "UTF-8");
      // System.out.println(" [x] Received '" + receivedData.value + "'");

      // Edgesys stuff
      Tuple tempTuple = receivedData.tuple;
      outputCollector.setContext(tempTuple);

      boltInstance.execute(tempTuple);
    };

    // Start consuming
    channel.basicConsume(tempQueueName, // queue name
        true, // autoAck
        deliverCallback, // DeliverCallback
        consumerTag -> {
        } // CancelCallback
    );

    // Program will loop here as channel and connection are open
    // Utils.sleep(10000);
    // channel.close();
    // connection.close(); // This will hold program open if we don't quit

  }

  static IRichBolt loadRichBolt(String name) {

    ClassLoader classLoader = EdgeSysHeronExecutor.class.getClassLoader();

    IRichBolt bClass;
    try {
      // Class aClass = classLoader.loadClass(name);
      // System.out.println("aClass.getName() = " + aClass.getName());

      // Class<MyClass> clazz = MyClass.class;
      // Constructor<MyClass> ctor = clazz.getDeclaredConstructor(String.class);
      // MyClass instance = ctor.newInstance("foo")

      bClass = (IRichBolt) classLoader.loadClass(name).newInstance();

      // IRichBolt testIBasicBolt =
      // (IRichBolt) classLoader.loadClass(name).newInstance();

    } catch (ClassNotFoundException ex) {
      ex.printStackTrace();
      throw new RuntimeException(ex + " Bolt class must be in class path.");
    } catch (InstantiationException ex) {
      throw new RuntimeException(ex + " Bolt class must be concrete.");
    } catch (IllegalAccessException ex) {
      throw new RuntimeException(ex + " Bolt class must have a no-arg constructor.");
    }

    return bClass;
  }

  public static final class EdgeSysStubTopologyContext implements TopologyContext {
    // TODO: eventually finish implementing

    public int getThisTaskId() {
      return 0;
    }

    public String getThisComponentId() {
      return null;
    }

    public Fields getThisOutputFields(String streamId) {
      return null;
    }

    public Set<String> getThisStreams() {
      return null;
    }

    public int getThisTaskIndex() {
      return 0;
    }

    public Map<TopologyAPI.StreamId, TopologyAPI.Grouping> getThisSources() {
      return null;
    }

    public Map<String, Map<String, TopologyAPI.Grouping>> getThisTargets() {
      return null;
    }

    public void setTaskData(String name, Object data) {
    }

    public Object getTaskData(String name) {
      return null;
    }

    public void addTaskHook(ITaskHook hook) {
    }

    public Collection<ITaskHook> getHooks() {
      return null;
    }

    public <T, U, V> ReducedMetric<T, U, V> registerMetric(String name, IReducer<T, U, V> reducer,
        int timeBucketSizeInSecs) {
      return null;
    }

    public <T> CombinedMetric<T> registerMetric(String name, ICombiner<T> combiner, int timeBucketSizeInSecs) {
      return null;
    }

    public String getTopologyId() {
      return null;
    }

    public TopologyAPI.Topology getRawTopology() {
      return null;
    }

    public String getComponentId(int taskId) {
      return null;
    }

    public Set<String> getComponentStreams(String componentId) {
      return null;
    }

    public List<Integer> getComponentTasks(String componentId) {
      return null;
    }

    public Fields getComponentOutputFields(String componentId, String streamId) {
      return null;
    }

    public Map<TopologyAPI.StreamId, TopologyAPI.Grouping> getSources(String componentId) {
      return null;
    }

    public Map<String, Map<String, TopologyAPI.Grouping>> getTargets(String componentId) {
      return null;
    }

    public Map<Integer, String> getTaskToComponent() {
      return null;
    }

    public Set<String> getComponentIds() {
      return null;
    }

    public int maxTopologyMessageTimeout() {
      return 0;
    }

    public <T extends IMetric<U>, U> T registerMetric(String name, T metric, int timeBucketSizeInSecs) {
      return null;
    }
  }

  public static final class EdgeSysOutputCollector extends OutputCollector {

    private IOutputCollector delegate;
    private boolean curTupleIsTick = false;

    String outputExchange;
    Kryo kryo;
    ByteBufferOutput output;
    Connection connection;
    Channel channel;
    Map<String, Object> runTimeConfig;

    public EdgeSysOutputCollector(IOutputCollector delegate, String outputHost, String outputExchange,
        Map<String, Object> runTimeConfig) {
      super(delegate);

      this.outputExchange = outputExchange;
      this.runTimeConfig = runTimeConfig;

      // Setup Kyro and buffers for serialization
      int bufferSize = 10240;
      this.kryo = new Kryo();
      kryo.register(EdgeSysHeronTupleData.class);
      output = new ByteBufferOutput(bufferSize);

      // Create output channel
      ConnectionFactory factory = new ConnectionFactory();
      factory.setHost(outputHost);
      // Try to create connection and channel
      try {
        connection = factory.newConnection();
        channel = connection.createChannel();

        // We declare exchanges instead of output queues
        channel.exchangeDeclare(outputExchange, BuiltinExchangeType.DIRECT);
      } catch (Exception e) {
        System.out.println("Error creating output channel/connection ! ");
      }
    }

    void close() {
      try {
        channel.close();
        connection.close();
      } catch (Exception e) {
      }
    }

    public EdgeSysOutputCollector(String outputHost, String outputExchange, Map<String, Object> runTimeConfig) {
      this(null, outputHost, outputExchange, runTimeConfig);
    }

    public List<Integer> emit(String streamId, Tuple anchor, List<Object> tuple) {
      return emit(streamId, Arrays.asList(anchor), tuple);
    }

    public List<Integer> emit(String streamId, List<Object> tuple) {
      return emit(streamId, (Collection<Tuple>) null, tuple);
    }

    public List<Integer> emit(Collection<Tuple> anchors, List<Object> tuple) {
      return emit(Utils.DEFAULT_STREAM_ID, anchors, tuple);
    }

    public List<Integer> emit(Tuple anchor, List<Object> tuple) {
      return emit(Utils.DEFAULT_STREAM_ID, anchor, tuple);
    }

    public List<Integer> emit(List<Object> tuple) {
      return emit(Utils.DEFAULT_STREAM_ID, tuple);
    }

    public void emitDirect(int taskId, String streamId, Tuple anchor, List<Object> tuple) {
      emitDirect(taskId, streamId, Arrays.asList(anchor), tuple);
    }

    public void emitDirect(int taskId, String streamId, List<Object> tuple) {
      emitDirect(taskId, streamId, (Collection<Tuple>) null, tuple);
    }

    public void emitDirect(int taskId, Collection<Tuple> anchors, List<Object> tuple) {
      emitDirect(taskId, Utils.DEFAULT_STREAM_ID, anchors, tuple);
    }

    public void emitDirect(int taskId, Tuple anchor, List<Object> tuple) {
      emitDirect(taskId, Utils.DEFAULT_STREAM_ID, anchor, tuple);
    }

    public void emitDirect(int taskId, List<Object> tuple) {
      emitDirect(taskId, Utils.DEFAULT_STREAM_ID, tuple);
    }

    public void setContext(Tuple tuple) {
      // TupleData tupleData;
      // if(!TupleInfo.isTickTuple(tuple))
      // {
      // if(tuple.getValue(tuple.size()-1) instanceof TupleData) {
      // tupleData = (TupleData)tuple.getValue(tuple.size()-1);
      // } else {
      // tupleData = (TupleData)tuple.getValue(tuple.size()-2);
      // }
      // } else {
      // tupleData=null;
      // }

    }

    @Override
    public List<Integer> emit(String streamId, Collection<Tuple> anchors, List<Object> tuple) {
      // LOG.trace(String.format("sending %s", tuple));
      LinkedList<Integer> sentIds = new LinkedList<Integer>(); // IDs that we sent to
      String routingKey = null;

      // TODO: figure out which instance to send using groupings

      // Build tuple into EdgeSys and send
      List<String> tempFields = null;
      EdgeSysTuple tempTuple = new EdgeSysTuple(
        streamId, 
        new Fields(tempFields), // fields
        tuple, // values
        null, // sourceComponent
        null // sourceTask
      );

      // Publish message to queue, payload is byte array

      EdgeSysHeronTupleData tempPayload = new EdgeSysHeronTupleData();
      tempPayload.tuple = tempTuple;

      kryo.writeObject(output, tempPayload);
      // channel.basicPublish("", QUEUE_NAME, null, output.toBytes());
      // System.out.println(" [x] Sent '" + message + "'");
      // System.out.println("Message sent successfully");

      try {
        channel.basicPublish(outputExchange, // Exchange name
            routingKey, // routingKey
            null, // props
            output.toBytes() // Payload
        );
      } catch (IOException e) {
        System.out.println("Error with sending tuple");
        e.printStackTrace();
      }

      return null;
    }

    @Override
    public void emitDirect(
        int taskId, String streamId, Collection<Tuple> anchors, List<Object> tuple) {
      // delegate.emitDirect(taskId, streamId, anchors, tuple);
    }

    @Override
    public void ack(Tuple input) {
      // delegate.ack(input);
    }

    @Override
    public void fail(Tuple input) {
      // delegate.fail(input);
    }

    @Override
    public void reportError(Throwable error) {
      // delegate.reportError(error);
    }
  }
}