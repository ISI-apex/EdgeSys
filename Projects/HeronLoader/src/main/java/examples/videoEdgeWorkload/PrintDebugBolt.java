package examples.videoEdgeWorkload;

import com.twitter.heron.api.bolt.BaseRichBolt;
import com.twitter.heron.api.bolt.OutputCollector;
import com.twitter.heron.api.topology.OutputFieldsDeclarer;
import com.twitter.heron.api.topology.TopologyContext;
import com.twitter.heron.api.tuple.Fields;
import com.twitter.heron.api.tuple.Tuple;
// import tools.DebugTools;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class PrintDebugBolt extends BaseRichBolt {
  private static final long serialVersionUID = -2267338678317778214L;
  private OutputCollector collector;
  private long nItems;
  private boolean acksEnabled;
  private String name;
  // private String tupleField;
  private List<String> tupleFields;

  public PrintDebugBolt() {
    this(false, "message");
  }

  public PrintDebugBolt(boolean enableAck) {
    this(enableAck, "message");
  }

  public PrintDebugBolt(String tupleField) {
    this(false, tupleField);
  }

  public PrintDebugBolt(boolean enableAck, String tupleField) {
    this(enableAck, new ArrayList<String>(Arrays.asList(tupleField)));
  }

  public PrintDebugBolt(boolean enableAck, List<String> tupleFields) {
    // System.out.println("Got: " + enableAck + " " + tupleFields);
    this.acksEnabled = enableAck;
    this.tupleFields = tupleFields;
  }

  @Override
  @SuppressWarnings("rawtypes")
  public void prepare(Map conf, TopologyContext context, OutputCollector acollector) {
    collector = acollector;
    nItems = 0;

    this.name = String.format("%s_%s", context.getThisComponentId(), context.getThisTaskId());
    // DebugTools.printOpenData(this.getClass().getName(), conf, context);

  }

  @Override
  public void execute(Tuple tuple) {
    ++nItems;
    // System.out.println("\t**************************" + tuple.getFields() +
    // "**************************");
    String outputString = String.format("%s", this.name);
    for (int i = 0; i < this.tupleFields.size(); i++) {
      outputString = outputString + " " + tuple.getValueByField(this.tupleFields.get(i));
    }
    System.out.println(outputString);
    // System.out.println(this.name + " got: " + tuple.getValueByField(this.tupleField).toString());

    if (acksEnabled) collector.ack(tuple);
  }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer declarer) {
    declarer.declareStream("defaultStream", new Fields());
  }
}
