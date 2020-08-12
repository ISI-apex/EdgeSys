// https://www.tutorialspoint.com/zookeeper/zookeeper_api.htm
package learningSamples;

import static org.fest.assertions.api.Assertions.assertThat;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.ByteBufferOutput;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.twitter.heron.api.utils.Utils;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Date;
import java.util.Properties;

public class RabbitmqKyroProduceTest {

  private static final String QUEUE_NAME = "testMessage";

  // Own test
  public static void kyroKafkaProduce() throws Exception {
    // Important:
    // When getting output, get bytes as byteBuffer has offsets that it tracks

    // More autonomous management
    int bufferSize = 10240;

    Kryo kryo = new Kryo();
    kryo.register(SomeClass.class);

    SomeClass object = new SomeClass();
    object.value = "Hello Kryo (own example)!";

    // Output output = new Output(new FileOutputStream("file.bin"));
    ByteBufferOutput output = new ByteBufferOutput(bufferSize);
    // ByteBufferOutput output = new ByteBufferOutput(tempBuf);
    kryo.writeObject(output, object);
    // Buffers so need to flush or close
    output.close();
    System.out.println(object.value);

    // Write to rabbitmq
    // Create connection
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    // Try to create connection and channel
    try (Connection connection = factory.newConnection();
        Channel channel = connection.createChannel()) {
      // Declare queue (only created if doesn't exist)
      channel.queueDeclare(QUEUE_NAME, false, false, false, null);
      String message = "Hello World!";
      // Publish message to queue, payload is byte array
      channel.basicPublish("", QUEUE_NAME, null, output.toBytes());
      System.out.println(" [x] Sent '" + message + "'");
      System.out.println("Message sent successfully");
    }
  }

  static void runConsumer() throws Exception {
    Kryo kryo = new Kryo();
    kryo.register(SomeClass.class);

    // Create connection and channel
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    // Declare queue so that it exists before we consume from it
    channel.queueDeclare(QUEUE_NAME, false, false, false, null);
    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

    DeliverCallback deliverCallback =
        (consumerTag, delivery) -> {
          // ByteBuffer tempBuf = output.getByteBuffer();
          ByteBufferInput input = new ByteBufferInput(delivery.getBody());

          // Input input = new Input(new FileInputStream("file.bin"));
          SomeClass object2 = kryo.readObject(input, SomeClass.class);
          input.close();

          // assertThat(object2.value).isEqualTo(object.value);
          System.out.println("Deserialized value: " + object2.value);

          // String message = new String(delivery.getBody(), "UTF-8");
          System.out.println(" [x] Received '" + object2.value + "'");
        };
    channel.basicConsume(
        QUEUE_NAME, // queue name
        true, // autoAck
        deliverCallback, // DeliverCallback
        consumerTag -> {} // CancelCallback
        );

    Utils.sleep(10000);
    channel.close();
    connection.close(); // This will hold program open if we don't quit

    System.out.println("Ready to quit? ");
  }

  public static class SomeClass {
    String value;
  }

  public static void main(String[] args) throws Exception {
    kyroKafkaProduce();
    runConsumer();
  }
}
