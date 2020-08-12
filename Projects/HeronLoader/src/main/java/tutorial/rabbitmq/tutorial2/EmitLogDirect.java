package tutorial.rabbitmq.tutorial2;

import java.util.HashMap;
import java.util.Map;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class EmitLogDirect {

  private static final String EXCHANGE_NAME = "direct_logs";

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    try (Connection connection = factory.newConnection();
        Channel channel = connection.createChannel()) {

      // We declare exchanges instead of output queues
      channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);

      String severity = getSeverity(argv);
      String message = getMessage(argv);
      
      Map<String, Object> headers = new HashMap<String, Object>();
      headers.put("latitude",  51.5252949);
      headers.put("longitude", -0.0905493);
      headers.put("target", "testTarget");

      channel.basicPublish(
          EXCHANGE_NAME, // Exchange name
          severity, // routingKey
          new AMQP.BasicProperties.Builder()
               .headers(headers)
               .build(), // props
          message.getBytes("UTF-8") // Payload
          );
      System.out.println(" [x] Sent '" + severity + "':'" + message + "'");
    }
  }

  private static String getSeverity(String[] strings) {
    if (strings.length < 1) return "info";
    return strings[0];
  }

  private static String getMessage(String[] strings) {
    if (strings.length < 2) return "Hello World!";
    return joinStrings(strings, " ", 1);
  }

  private static String joinStrings(String[] strings, String delimiter, int startIndex) {
    int length = strings.length;
    if (length == 0) return "";
    if (length <= startIndex) return "";
    StringBuilder words = new StringBuilder(strings[startIndex]);
    for (int i = startIndex + 1; i < length; i++) {
      words.append(delimiter).append(strings[i]);
    }
    return words.toString();
  }
}
