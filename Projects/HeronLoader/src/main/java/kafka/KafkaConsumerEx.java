package learningSamples;

import java.util.Collections;
import java.util.Properties;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.zookeeper.ZKUtil;
import org.apache.zookeeper.ZooKeeper;

public class KafkaConsumerEx {

  private static final String TOPIC = "test";
  private static final String BOOTSTRAP_SERVERS =
      // "localhost:9092,localhost:9093,localhost:9094";
      "localhost:9092";

  private static Consumer<Long, String> createConsumer() {

    final Properties props = new Properties();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "KafkaExampleConsumer");
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

    // Create the consumer using props.
    final Consumer<Long, String> consumer = new KafkaConsumer<>(props);

    // Subscribe to the topic.
    consumer.subscribe(Collections.singletonList(TOPIC));
    return consumer;
  }

  // Sample usage
  static void runConsumer() throws InterruptedException {
    // Create consumer
    final Consumer<Long, String> consumer = createConsumer();

    final int giveUp = 100;
    int noRecordsCount = 0;

    while (true) {
      // Poll for new records
      final ConsumerRecords<Long, String> consumerRecords = consumer.poll(1000);

      if (consumerRecords.count() == 0) {
        System.out.println("Empty record");
        noRecordsCount++;
        if (noRecordsCount > giveUp) break;
        else continue;
      }

      // Iterate through records
      consumerRecords.forEach(
          record -> {
            System.out.printf(
                "Consumer Record:(%d, %s, %d, %d)\n",
                record.key(), record.value(), record.partition(), record.offset());
          });

      // Commit the offset back
      consumer.commitAsync();
    }
    consumer.close();
    System.out.println("DONE");
  }

  public static void main(String[] args) throws Exception {
    runConsumer();
  }
}
