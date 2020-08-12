// https://www.tutorialspoint.com/zookeeper/zookeeper_api.htm
package learningSamples;

import static org.fest.assertions.api.Assertions.assertThat;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.ByteBufferOutput;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.twitter.heron.api.tuple.Values;
import examples.videoEdgeWorkload.tools.NativeUtils;
import examples.videoEdgeWorkload.tools.Serializable;
import examples.videoEdgeWorkload.tools.WorkloadConstants;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Date;
import java.util.Properties;
import java.util.zip.CRC32;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

public class ValuesKafkaKyro {

  private static final String TOPIC = "test2";
  private static final String BOOTSTRAP_SERVERS =
      // "localhost:9092,localhost:9093,localhost:9094";
      "localhost:9092";

  private static Producer<Long, byte[]> createProducer() {

    Properties props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
    props.put("acks", "all");
    props.put("retries", 0);
    props.put("batch.size", 16384);
    props.put("linger.ms", 1);
    props.put("buffer.memory", 33554432);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
    return new KafkaProducer<Long, byte[]>(props);
  }

  private static Consumer<Long, byte[]> createConsumer() {

    final Properties props = new Properties();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "KafkaExampleConsumer");
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
    props.put(
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

    // Create the consumer using props.
    final Consumer<Long, byte[]> consumer = new KafkaConsumer<>(props);

    // Subscribe to the topic.
    consumer.subscribe(Collections.singletonList(TOPIC));
    return consumer;
  }

  // Own test
  public static void kyroKafkaProduce() throws Exception {
    // Important:
    // When getting output, get bytes as byteBuffer has offsets that it tracks

    // More autonomous management
    int bufferSize = 10240;

    Kryo kryo = new Kryo();
    kryo.register(SomeClass.class);

    // Load image
    Serializable.Mat imageToSend;
    // nu.pattern.OpenCV.loadShared();
    // System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    nu.pattern.OpenCV.loadLocally();
    CRC32 crcGen = new CRC32();
    String imageName = "2.png";
    System.out.println("Loading image: " + imageName);
    String tempImageFilePath = "";
    try {
      tempImageFilePath =
          NativeUtils.extractTmpFileFromJar(
                  WorkloadConstants.IMAGE_DATASET_DIRECTORY + imageName, true, "TestID")
              .getAbsolutePath();
    } catch (Exception e) {
      System.out.println("Error getting image!" + tempImageFilePath);
      System.out.println(e.toString());
    }
    Mat image = Highgui.imread(tempImageFilePath);
    Serializable.Mat testSMat = new Serializable.Mat(image);
    imageToSend = new Serializable.Mat(image);
    Values tempValues = new Values(212121, "this.currentMode", imageToSend);
    crcGen.reset();
    crcGen.update(((Serializable.Mat) tempValues.get(2)).toByteArray());
    System.out.println("input id: " + tempValues.get(0));
    System.out.println("output CRC: " + crcGen.getValue());

    SomeClass object = new SomeClass();
    object.value = "Hello Kryo (own example)!";

    // Output output = new Output(new FileOutputStream("file.bin"));
    ByteBufferOutput output = new ByteBufferOutput(bufferSize);
    // ByteBufferOutput output = new ByteBufferOutput(tempBuf);
    kryo.writeObject(output, tempValues);
    // Buffers so need to flush or close
    output.close();
    System.out.println(object.value);

    // Write to Kafka
    final Producer<Long, byte[]> producer = createProducer();
    producer.send(new ProducerRecord<Long, byte[]>(TOPIC, Long.valueOf(0), output.toBytes()));
    System.out.println("Message sent successfully");
    producer.close();

    runConsumer();
  }

  static void runConsumer() throws InterruptedException {
    // Create consumer
    final Consumer<Long, byte[]> consumer = createConsumer();

    Kryo kryo = new Kryo();
    kryo.register(SomeClass.class);

    final int giveUp = 5;
    int noRecordsCount = 0;

    while (true) {
      // Poll for new records
      final ConsumerRecords<Long, byte[]> consumerRecords = consumer.poll(1000);

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

            // Must
            // ByteBuffer tempBuf = output.getByteBuffer();
            ByteBufferInput input = new ByteBufferInput(record.value());

            // Input input = new Input(new FileInputStream("file.bin"));
            // SomeClass object2 = kryo.readObject(input, SomeClass.class);
            CRC32 crcGen = new CRC32();
            Values object2 = kryo.readObject(input, Values.class);
            System.out.println("input id: " + object2.get(0));
            crcGen.reset();
            crcGen.update(((Serializable.Mat) object2.get(2)).toByteArray());
            System.out.println("input CRC: " + crcGen.getValue());
            input.close();

            // assertThat(object2.value).isEqualTo(object.value);
            // System.out.println("Deserialized value: " + object2.value);

          });

      // Commit the offset back
      consumer.commitAsync();
    }
    consumer.close();
    System.out.println("DONE");
  }

  public static class SomeClass {
    String value;
  }

  public static void main(String[] args) throws Exception {
    kyroKafkaProduce();
  }
}
