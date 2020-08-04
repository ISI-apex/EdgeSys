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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TestWordCountFeeder {

  public static void main(String[] args) {
    ByteBufferOutput output;
    Kryo kryo;
    String outputHost = "localhost";
    String outputExchange;
    Channel channel = null;
    Connection connection = null;
    String streamId = "default";
    String routingKey;

    if(EdgeSysFlags.runLocal) {
      outputExchange = "testExchange";
      routingKey = "split_0";
    } else {
      outputExchange = "testExchangeOut";
      routingKey = "testRoutingKeyExternal";
    }

    String userName = "cat";
    String password = "meow";
    String virtualHost = "/";
    Integer portNumber = 5672;

    Integer numToSend = Integer.valueOf(args[0]);
    Integer timeBetweenSendMs = Integer.valueOf(args[1]);


    // Setup Kyro and buffers for serialization
    int bufferSize = 10240;
    kryo = new Kryo();
    kryo.register(EdgeSysHeronTupleData.class);
    output = new ByteBufferOutput(bufferSize);

    // Create output channel
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(outputHost);
    factory.setUsername(userName);
    factory.setPassword(password);
    factory.setVirtualHost(virtualHost);
    factory.setPort(portNumber);
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

    Map<String, Object> headers = new HashMap<String, Object>();
    headers.put("target", "split_0");

    EdgeSysTuple tempTuple =
        new EdgeSysTuple(
            streamId,
            new Fields(tempFields), // fields
            // new Values("the cow jumped over the moon"), // values
            new Values("USCUSCUSCUSCUSC"), // values
            null, // sourceComponent
            null // sourceTask
            );
    LinkedList<String> sendTargetList = new LinkedList<String>();

    // Publish message to queue, payload is byte array
    EdgeSysHeronTupleData tempPayload = new EdgeSysHeronTupleData();
    tempPayload.tuple = tempTuple;

    // kryo.writeObject(output, tempPayload);

    try {
      for (int i = 0; i < numToSend; i++) {
        channel.basicPublish(
            outputExchange, // Exchange name
            routingKey, // routingKey
            null, // props
            Utils.serialize(tempPayload) // Payload
            );
        FileUtils.writeToFile("metrics-feeder.txt", (System.nanoTime())+"\n");

        Utils.sleep(timeBetweenSendMs);
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
