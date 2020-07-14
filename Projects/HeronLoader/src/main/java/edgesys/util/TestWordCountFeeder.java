package edgesys.util;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferOutput;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.twitter.heron.api.tuple.Fields;
import com.twitter.heron.api.tuple.Values;
import com.twitter.heron.api.utils.Utils;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class TestWordCountFeeder {

  public static void main(String[] args) {
    ByteBufferOutput output;
    Kryo kryo;
    String outputHost = "localhost";
    String outputExchange = "testExchange";
    Channel channel = null;
    Connection connection = null;
    String streamId = "default";
    String routingKey = "split_0";

    // Setup Kyro and buffers for serialization
    int bufferSize = 10240;
    kryo = new Kryo();
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

    List<String> tempFields = Arrays.asList("sentence");
    EdgeSysTuple tempTuple =
        new EdgeSysTuple(
            streamId,
            new Fields(tempFields), // fields
            new Values("the cow jumped over the moon"), // values
            null, // sourceComponent
            null // sourceTask
            );
    LinkedList<String> sendTargetList = new LinkedList<String>();

    // Publish message to queue, payload is byte array
    EdgeSysHeronTupleData tempPayload = new EdgeSysHeronTupleData();
    tempPayload.tuple = tempTuple;

    // kryo.writeObject(output, tempPayload);

    try {
      for (int i = 0; i < 1000; i++) {
        channel.basicPublish(
            outputExchange, // Exchange name
            routingKey, // routingKey
            null, // props
            Utils.serialize(tempPayload) // Payload
            );
        // Utils.sleep(200);
      }
    } catch (IOException e) {
      System.out.println("Error with sending tuple");
      e.printStackTrace();
    }

    System.out.println("Done with sending");
    try {
      channel.close();
      connection.close();
    } catch (Exception e) {
    }
    // public void nextTuple() {
    //     // Sleep for a bit
    //     Utils.sleep(50);
    //     // The sentences that are randomly emitted
    //     String[] sentences =
    //         new String[] {
    //           "the cow jumped over the moon",
    //           "an apple a day keeps the doctor away",
    //           "four score and seven years ago",
    //           "snow white and the seven dwarfs",
    //           "i am at two with nature"
    //         };
    //     // Randomly pick a sentence
    //     String sentence = sentences[rand.nextInt(sentences.length)];
    //     // Emit the sentence
    //     collector.emit(new Values(sentence));
    //   }

    //   // Declare the output fields. In this case, an sentence
    //   @Override
    //   public void declareOutputFields(OutputFieldsDeclarer declarer) {
    //     declarer.declare(new Fields("sentence"));
    //   }

  }
}
