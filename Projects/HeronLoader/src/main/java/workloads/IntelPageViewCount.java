package workloads;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.twitter.heron.api.Config;
import com.twitter.heron.api.HeronSubmitter;
import com.twitter.heron.api.bolt.BaseBasicBolt;
import com.twitter.heron.api.bolt.BasicOutputCollector;
import com.twitter.heron.api.spout.BaseRichSpout;
import com.twitter.heron.api.spout.SpoutOutputCollector;
import com.twitter.heron.api.topology.OutputFieldsDeclarer;
import com.twitter.heron.api.topology.TopologyBuilder;
import com.twitter.heron.api.topology.TopologyContext;
import com.twitter.heron.api.tuple.Fields;
import com.twitter.heron.api.tuple.Tuple;
import com.twitter.heron.api.tuple.Values;

import examples.videoEdgeWorkload.PrintDebugBolt;
import workloads.IntelStreamBench.PageView;
import workloads.IntelStreamBench.PageView.Item;
import workloads.util.MetricSpout;
import edgesys.util.EdgeSysTopologyBuilder;

public final class IntelPageViewCount {

	public static final int DEFAULT_SPOUT_NUM = 1;
	public static final int DEFAULT_VIEW_BOLT_NUM = 1;
	public static final int DEFAULT_COUNT_BOLT_NUM = 1;

	private IntelPageViewCount() {
	}


	public static void main(String[] args) throws Exception {
		TopologyBuilder builder = new TopologyBuilder();
        // TopologyBuilder builder = new EdgeSysTopologyBuilder();

        final int spoutNum =  DEFAULT_SPOUT_NUM;
		final int viewBoltNum = DEFAULT_VIEW_BOLT_NUM;
		final int cntBoltNum = DEFAULT_COUNT_BOLT_NUM;

		final int parallelism = 3;


		// config.put(KafkaUtils.KAFKA_ROOT_PATH, "/testTopic");
		// config.put(KafkaUtils.TOPIC, "testTopic");
		// config.put(KafkaUtils.CLIENT_ID, "testKafkaReadTopology");

        if(builder instanceof EdgeSysTopologyBuilder) {
            builder.setSpout(
                "kafkaSpout",
                new KafkaStubSpout(),
                spoutNum
            );
        } else {
            builder.setSpout(
                "kafkaSpout",
                new MetricSpout(
                    new KafkaStubSpout(), 100
                ),
                spoutNum
            );
        }

		builder.setBolt("pageViewBolt", new PageViewBolt(Item.URL, Item.ONE), viewBoltNum)
			.shuffleGrouping("kafkaSpout");
		builder.setBolt("countBolt", new CountBolt(), cntBoltNum)
			.fieldsGrouping("pageViewBolt", new Fields(Item.URL.toString()));


        if(builder instanceof EdgeSysTopologyBuilder) {
            System.out.println("Adding print bolt for terminator");
            builder.setBolt(
                "printBolt", 
                new PrintDebugBolt(true, Arrays.asList("word", "count")), 
                1
                )
                .shuffleGrouping("countBolt", "default");    
        }

		Config conf = new Config();
		// conf.setDebug(true);
		conf.setMaxSpoutPending(1);
		conf.setMessageTimeoutSecs(600);

		Config.setSerializationClassName(conf,
				"com.twitter.heron.api.serializer.JavaSerializer");

		if (args != null && args.length > 0) {
			conf.setNumStmgrs(parallelism);
			HeronSubmitter.submitTopology(args[0], conf, builder.createTopology());
        }
    }


/**
 * This spout randomly emits sentences
 */
public static class KafkaStubSpout extends BaseRichSpout {
    private static final long serialVersionUID = 1901208148299349473L;
    // Collector used to emit output
	SpoutOutputCollector collector;
	//Used to generate a random number
	Random rand;
	//Random seed
	int randomSeed=-1;
	// Counter for offset
	int curOffset=0;
	// Counter for partition
	int myPartition;

	int pageCount=0;
	String pageBase="foo.com";
	int status=0;
	int zipVal=0;
	int userId=0;



	public KafkaStubSpout() {
	}

	//Open is called when an instance of the class is created
	@Override
	public void open(Map map, TopologyContext topologyContext, SpoutOutputCollector collector) {
		//Set the instance collector to the one passed in
		this.collector = collector;
		// Init parameters
		this.myPartition = topologyContext.getThisTaskIndex();
		//For randomness
		if(randomSeed==-1)
			this.rand = new Random();
		else
			this.rand = new Random(this.randomSeed);
	}

	//Emit data to the stream
	@Override
	public void nextTuple() {
		// Sleep is in ms
		// Utils.sleep(500);

		// Calculate random values for tuples
		pageCount=getRandomNumberInRange(0,99);
		status=getRandomNumberInRange(200,500);
		zipVal=getRandomNumberInRange(0,99);
		userId=getRandomNumberInRange(0,99);

		// Construct tuple and emit
		// System.out.println(String.format("Sending: [%d.%s\t%d\t%d\t%d]", pageCount, pageBase, status, zipVal, userId));
		this.collector.emit(new Values(
			String.format("%d.%s\t%d\t%d\t%d", pageCount, pageBase, status, zipVal, userId), 
			this.myPartition, 
			this.curOffset
			),
			this.curOffset
		);
		// Incr offset
		this.curOffset++;
	}

	//Declare the output fields. In this case, an sentence
	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("string", "partition", "offset"));
	}

	@Override
	public void ack(Object msgId) {
	}

  @Override
  public void fail(Object o) {
		// Calculate random values for tuples
		pageCount=getRandomNumberInRange(0,99);
		status=getRandomNumberInRange(200,500);
		zipVal=getRandomNumberInRange(0,99);
		userId=getRandomNumberInRange(0,99);

		// Construct tuple and emit
		// System.out.println(String.format("Sending: [%d.%s\t%d\t%d\t%d]", pageCount, pageBase, status, zipVal, userId));
		this.collector.emit(new Values(
			String.format("%d.%s\t%d\t%d\t%d", pageCount, pageBase, status, zipVal, userId), 
			this.myPartition, 
			o
			),
			o
		);
  }

	// https://www.mkyong.com/java/java-generate-random-integers-in-a-range/
	private  int getRandomNumberInRange(int min, int max) {
		if (min >= max) {
			throw new IllegalArgumentException("max must be greater than min");
		}

		Random r = new Random();
		return r.nextInt((max - min) + 1) + min;
	}
}


public static class PageViewBolt extends BaseBasicBolt {

    private static final long serialVersionUID = -523726932372993856L;
    public final Item field1;
    public final Item field2;
  
    public PageViewBolt(Item field1, Item field2) {
      this.field1 = field1;
      this.field2 = field2;
    }
  
    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
      PageView view = PageView.fromString(input.getString(0));
      collector.emit(new Values(view.getValue(field1), view.getValue(field2)));
    }
  
    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
      declarer.declare(new Fields(field1.toString(), field2.toString()));
    }
  
  }



  public static class CountBolt extends BaseBasicBolt {
    public static final String FIELDS_WORD = "word";
    public static final String FIELDS_COUNT = "count";
  
    Map<String, Integer> counts = new HashMap<String, Integer>();
  
    @Override
    public void prepare(Map stormConf, TopologyContext context) {
    }
  
    @Override
    public void execute(Tuple tuple, BasicOutputCollector collector) {
      String word = tuple.getString(0);
      Integer count = counts.get(word);
      if (count == null)
        count = 0;
      count++;
      counts.put(word, count);
      collector.emit(new Values(word, count));
    }
  
    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
      declarer.declare(new Fields(FIELDS_WORD, FIELDS_COUNT));
    }
  }







}
