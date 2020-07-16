package edgesys.util;

import java.text.BreakIterator;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.twitter.heron.common.basics.ByteAmount;
import com.twitter.heron.api.bolt.BaseBasicBolt;
import com.twitter.heron.api.bolt.BaseRichBolt;
import com.twitter.heron.api.bolt.BasicOutputCollector;
import com.twitter.heron.api.bolt.OutputCollector;
import com.twitter.heron.api.spout.BaseRichSpout;
import com.twitter.heron.api.spout.SpoutOutputCollector;
import com.twitter.heron.api.topology.OutputFieldsDeclarer;
import com.twitter.heron.api.topology.TopologyContext;
import com.twitter.heron.api.tuple.Fields;
import com.twitter.heron.api.tuple.Tuple;
import com.twitter.heron.api.tuple.Values;
import com.twitter.heron.api.utils.Utils;
import com.twitter.heron.api.topology.TopologyBuilder;
import com.twitter.heron.api.Config;


import examples.videoEdgeWorkload.PrintDebugBolt;
import tutorial.util.HelperRunner;

/** This is driver as well the topology graph generator */
public class TestWordCountTopology {

  private TestWordCountTopology() {}

  // Entry point for the topology
  public static void main(String[] args) throws Exception {

    TopologyBuilder builder = new EdgeSysTopologyBuilder();

    builder.setSpout("sentence", 
      new RandomSentenceSpout(), 1);

    builder.setBolt("split", 
      new SplitSentenceBolt(), 1)
      .shuffleGrouping("sentence");

    builder.setBolt("count", 
      new WordCountBolt(), 1)
      .fieldsGrouping("split", new Fields("word"));
    
    builder.setBolt("print", 
      new PrintDebugBolt(false, Arrays.asList("word", "count")), 1)
      .shuffleGrouping("count");



      Config conf = new Config();

    // Resource Configs
    com.twitter.heron.api.Config.setComponentRam(conf, "sentence", ByteAmount.fromGigabytes(1));
    com.twitter.heron.api.Config.setComponentRam(conf, "split", ByteAmount.fromGigabytes(1));
    com.twitter.heron.api.Config.setComponentRam(conf, "count", ByteAmount.fromGigabytes(1));
    com.twitter.heron.api.Config.setContainerCpuRequested(conf, 3);

    // submit the topology
    builder.createTopology();
    // HelperRunner.runTopology(args, builder.createTopology(), conf);
  }


  static public class RandomSentenceSpout extends BaseRichSpout {
    private static final long serialVersionUID = 6609868287233339880L;
    // Collector used to emit output
    SpoutOutputCollector collector;
    // Used to generate a random number
    Random rand;
  
    // Open is called when an instance of the class is created
    @Override
    public void open(Map map, TopologyContext topologyContext, SpoutOutputCollector collector) {
      // Set the instance collector to the one passed in
      this.collector = collector;
      // For randomness
      this.rand = new Random();
    }
  
    // Emit data to the stream
    @Override
    public void nextTuple() {
      // Sleep for a bit
      Utils.sleep(50);
      // The sentences that are randomly emitted
      String[] sentences =
          new String[] {
            "the cow jumped over the moon",
            "an apple a day keeps the doctor away",
            "four score and seven years ago",
            "snow white and the seven dwarfs",
            "i am at two with nature"
          };
      // Randomly pick a sentence
      String sentence = sentences[rand.nextInt(sentences.length)];
      // Emit the sentence
      collector.emit(new Values(sentence));
    }
  
    // Declare the output fields. In this case, an sentence
    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
      declarer.declare(new Fields("sentence"));
    }
  }


  static public class WordCountBolt extends BaseBasicBolt {
    // Create logger for this class
    // private static final Logger logger = LogManager.getLogger(WordCountBolt.class);
    private static final long serialVersionUID = 6846681598718531798L;
    // For holding words and counts
    private Map<String, Integer> counts = new HashMap<>();
  
    public WordCountBolt() {
    }
  
    public WordCountBolt(int emitFrequency) {
    }
  
    // Configure frequency of tick tuples for this bolt
    // This delivers a 'tick' tuple on a specific interval,
    // which is used to trigger certain actions
    @Override
    public Map<String, Object> getComponentConfiguration() {
      Config conf = new Config();
      // conf.put(Config.TOPOLOGY_TICK_TUPLE_FREQ_SECS, emitFrequency);
      return conf;
    }
  
    // execute is called to process tuples
    @Override
    public void execute(Tuple tuple, BasicOutputCollector collector) {
      // Get the word contents from the tuple
      String word = tuple.getString(0);
      // Have we counted any already?
      Integer count = counts.get(word);
      if (count == null) count = 0;
      // Increment the count and store it
      count++;
      counts.put(word, count);
      collector.emit(new Values(word, count));
    }
  
    // Declare that this emits a tuple containing two fields; word and count
    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
      declarer.declare(new Fields("word", "count"));
    }
  }


  static public class SplitSentenceBolt extends BaseBasicBolt {
    private static final long serialVersionUID = 1L;

    // Execute is called to process tuples
    @Override
    public void execute(Tuple tuple, BasicOutputCollector collector) {
      // Get the sentence content from the tuple
      String sentence = tuple.getString(0);
      System.out.println("Sentence: " + sentence);
      // An iterator to get each word
      BreakIterator boundary = BreakIterator.getWordInstance();
      // Give the iterator the sentence
      boundary.setText(sentence);
      // Find the beginning first word
      int start = boundary.first();
      // Iterate over each word and emit it to the output stream
      for (int end = boundary.next(); end != BreakIterator.DONE; start = end, end = boundary.next()) {
        // get the word
        String word = sentence.substring(start, end);
        // If a word is whitespace characters, replace it with empty
        word = word.replaceAll("\\s+", "");
        // if it's an actual word, emit it
        if (!word.equals("")) {
          collector.emit(new Values(word));
        }
      }
    }
  
    // Declare that emitted tuples contain a word field
    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
      declarer.declare(new Fields("word"));
    }
  }


}
