package tutorial.rabbitmq.tutorial1;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class Send {
  private static final String QUEUE_NAME = "hello";

  public static void main(String[] argv) throws Exception {

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
      channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
      System.out.println(" [x] Sent '" + message + "'");
    }
  }
}
