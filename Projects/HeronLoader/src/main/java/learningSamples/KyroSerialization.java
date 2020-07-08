// https://www.tutorialspoint.com/zookeeper/zookeeper_api.htm
package learningSamples;

import static org.fest.assertions.api.Assertions.assertThat;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.ByteBufferOutput;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;

public class KyroSerialization {

  // Own test
  public static void kyroOwnByteBufferTest() throws Exception {

    // Important:
    // When getting output, get bytes as byteBuffer has offsets that it tracks

    // Manually manage bytebuffer
    // int bufferSize = 10240;

    // byte[] bytes = new byte[bufferSize];
    // ByteBuffer tempBuf = ByteBuffer.wrap(bytes);

    // Kryo kryo = new Kryo();
    // kryo.register(SomeClass.class);

    // SomeClass object = new SomeClass();
    // object.value = "Hello Kryo!";

    // // Output output = new Output(new FileOutputStream("file.bin"));
    // // ByteBufferOutput output = new ByteBufferOutput(bufferSize);
    // ByteBufferOutput output = new ByteBufferOutput(tempBuf);
    // kryo.writeObject(output, object);
    // // Buffers so need to flush or close
    // output.close();

    // // ByteBuffer tempBuf = output.getByteBuffer();
    // ByteBufferInput input = new ByteBufferInput(output.toBytes());

    // // works?
    // // ByteBufferInput input = new ByteBufferInput(output.toBytes().length);
    // // input.setBuffer(output.toBytes());

    // // Input input = new Input(new FileInputStream("file.bin"));
    // SomeClass object2 = kryo.readObject(input, SomeClass.class);
    // input.close();

    // assertThat(object2.value).isEqualTo(object.value);
    // System.out.println(object2.value);
    // System.out.println(object.value);

    // More autonomous management
    int bufferSize = 10240;

    // byte[] bytes = new byte[bufferSize];
    // ByteBuffer tempBuf = ByteBuffer.wrap(bytes);

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

    // Must
    // ByteBuffer tempBuf = output.getByteBuffer();
    ByteBufferInput input = new ByteBufferInput(output.toBytes());

    // Input input = new Input(new FileInputStream("file.bin"));
    SomeClass object2 = kryo.readObject(input, SomeClass.class);
    input.close();

    assertThat(object2.value).isEqualTo(object.value);
    System.out.println(object2.value);
    System.out.println(object.value);
  }

  // From Kyro github
  public static void kyroGithubTest() throws Exception {
    Kryo kryo = new Kryo();
    kryo.register(SomeClass.class);

    SomeClass object = new SomeClass();
    object.value = "Hello Kryo (github example)!";

    Output output = new Output(new FileOutputStream("file.bin"));
    kryo.writeObject(output, object);
    // Buffers so need to flush or close
    output.close();

    Input input = new Input(new FileInputStream("file.bin"));
    SomeClass object2 = kryo.readObject(input, SomeClass.class);
    input.close();

    assertThat(object2.value).isEqualTo(object.value);
    System.out.println(object2.value);
    System.out.println(object.value);
  }

  public static class SomeClass {
    String value;
  }

  // From https://www.baeldung.com/kryo
  // private Input localInput;
  // private Output localOutput;
  // private Kryo localKryo = new Kryo();

  // public void init() throws Exception {
  //   localKryo = new Kryo();
  //   localOutput = new Output(new FileOutputStream("file.dat"));
  //   localInput = new Input(new FileInputStream("file.dat"));
  // }

  // public void givenObject_whenSerializing_thenReadCorrectly() {
  //   Object someObject = "Some string";

  //   localKryo.writeClassAndObject(localOutput, someObject);
  //   localOutput.close();

  //   Object theObject = localKryo.readClassAndObject(localInput);
  //   localInput.close();

  //   // assertEquals(theObject, "Some string");
  //   assertThat(theObject).isEqualTo("Some string");
  // }

  // public void givenObjects_whenSerializing_thenReadCorrectly() {
  //   String someString = "Multiple Objects";
  //   Date someDate = new Date(915170400000L);

  //   localKryo.writeObject(localOutput, someString);
  //   localKryo.writeObject(localOutput, someDate);
  //   localOutput.close();

  //   String readString = localKryo.readObject(localInput, String.class);
  //   Date readDate = localKryo.readObject(localInput, Date.class);
  //   localInput.close();

  //   // assertEquals(readString, "Multiple Objects");
  //   assertThat(readString).isEqualTo("Multiple Objects");
  //   // assertEquals(readDate.getTime(), 915170400000L);
  //   assertThat(readDate.getTime()).isEqualTo(915170400000L);
  // }

  public static void main(String[] args) throws Exception {
    kyroGithubTest();
    kyroOwnByteBufferTest();
  }
}
