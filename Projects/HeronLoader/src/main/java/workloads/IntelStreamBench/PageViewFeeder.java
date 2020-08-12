package workloads.IntelStreamBench;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferOutput;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.twitter.heron.api.tuple.Fields;
import com.twitter.heron.api.tuple.Values;
import com.twitter.heron.api.utils.Utils;

import edgesys.util.EdgeSysFlags;
import edgesys.util.EdgeSysHeronTupleData;
import edgesys.util.EdgeSysTuple;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class PageViewFeeder {

  public static void main(String[] args) {
    ByteBufferOutput output;
    Kryo kryo;
    String outputHost = "localhost";
    String outputExchange;
    Channel channel = null;
    Connection connection = null;
    String streamId = "default";
    String routingKey;

    String userName = "cat";
    String password = "meow";
    String virtualHost = "/";
    Integer portNumber = 5672;
    int edgeSysID=0;

    String targetInstanceName = args[0];
    Integer targetInstanceCount = Integer.valueOf(args[1]);
    Integer numToSend = Integer.valueOf(args[2]);
    Integer timeBetweenSendMs = Integer.valueOf(args[3]);
    int currentTargetInstance = 0;

    if(EdgeSysFlags.runLocal) {
      outputExchange = "testExchange";
      routingKey = String.format("%s_0", targetInstanceName);
    } else {
      outputExchange = "testExchangeOut";
      routingKey = "testRoutingKeyExternal";
    }

    // Stuff for tuple
    int pageCount=0;
    String pageBase="foo.com";
    int status=0;
    int zipVal=0;
    int userId=0;
    

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

    List<String> tempFields = Arrays.asList("string", "partition", "offset");

    Map<String, Object> headers = new HashMap<String, Object>();

    try {
      for (int i = 0; i < numToSend; i++) {

        //   TODO: iterate through all parallel instances
        headers.put("target", String.format("%s_%d", 
          targetInstanceName, 
          ((currentTargetInstance++)%targetInstanceCount)));

        // TODO: generate edgesys_id
        headers.put("edgesys_id", edgeSysID++);

        // Generate proper tuple for PageViewCount
        // Calculate random values for tuples
        pageCount=getRandomNumberInRange(0,99, edgeSysID);
        status=getRandomNumberInRange(200,500, edgeSysID);
        zipVal=getRandomNumberInRange(0,99, edgeSysID);
        userId= getRandomNumberInRange(0, 99, edgeSysID);

        EdgeSysTuple tempTuple = new EdgeSysTuple(streamId, new Fields(tempFields), // fields
            // new Values("the cow jumped over the moon"), // values
            new Values(String.format("%d.%s\t%d\t%d\t%d", 
              pageCount, 
              pageBase, 
              status, 
              zipVal, 
              userId)), // values
            null, // sourceComponent
            null // sourceTask
        );
        // Publish message to queue, payload is byte array
        EdgeSysHeronTupleData tempPayload = new EdgeSysHeronTupleData();
        tempPayload.tuple = tempTuple;

        channel.basicPublish(outputExchange, // Exchange name
            routingKey, // routingKey
            new AMQP.BasicProperties.Builder()
              .headers(headers)
              .build(), // props
            Utils.serialize(tempPayload) // Payload
        );
        // FileUtils.writeToFile("metrics-feeder.txt", (System.nanoTime())+"\n");

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

  }

  // https://www.mkyong.com/java/java-generate-random-integers-in-a-range/
  private static int getRandomNumberInRange(int min, int max, int seed) {
		if (min >= max) {
			throw new IllegalArgumentException("max must be greater than min");
		}

		Random r = new Random(seed);
		return r.nextInt((max - min) + 1) + min;
	}

}
