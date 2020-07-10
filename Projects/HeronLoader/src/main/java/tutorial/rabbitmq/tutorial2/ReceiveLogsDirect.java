package tutorial.rabbitmq.tutorial2;

import com.rabbitmq.client.*;

public class ReceiveLogsDirect {

    private static final String EXCHANGE_NAME = "direct_logs";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // Declare exchange instead of queue
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);

        // Get temporary queue that will be automatically deleted
        String queueName = channel.queueDeclare().getQueue();

        if (argv.length < 1) {
            System.err.println("Usage: ReceiveLogsDirect [info] [warning] [error]");
            System.exit(1);
        }

        // Bind queues to exchange name based on routing key
        for (String severity : argv) {
            channel.queueBind(
              queueName, // Queue name
              EXCHANGE_NAME, // Exchange name
              severity // routingKey
              );
        }
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        // Create callback that rabbitmq uses to deliver messages
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received '" + delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };
        channel.basicConsume(
          queueName, // queue name
          true, // autoack
          deliverCallback, // called when message is delivered 
          consumerTag -> {} // called when consumer is cancelled
        );
    }
}