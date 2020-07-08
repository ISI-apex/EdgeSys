package learningSamples;

import java.lang.Thread;
import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

public class KafkaProducerEx {
  private static final String TOPIC = "test";
  private static final String BOOTSTRAP_SERVERS =
      // "localhost:9092,localhost:9093,localhost:9094";
      "localhost:9092";

  private static Producer<Long, String> createProducer() {

    Properties props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
    props.put("acks", "all");
    props.put("retries", 0);
    props.put("batch.size", 16384);
    props.put("linger.ms", 1);
    props.put("buffer.memory", 33554432);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    return new KafkaProducer<Long, String>(props);
  }

  public static void main(String[] args) throws Exception {
    final Producer<Long, String> producer = createProducer();
    String topicName = TOPIC;

    for (int i = 0; i < 10; i++) {
      producer.send(
          new ProducerRecord<Long, String>(topicName, Long.valueOf(i), Integer.toString(i)));
      System.out.println("Message sent successfully");
    }
    producer.close();
  }
}
