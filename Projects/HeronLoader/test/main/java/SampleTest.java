/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

// package tutorial.util;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import backtype.storm.Constants;
import examples.videoEdgeWorkload.PrintDebugBolt;

import com.twitter.heron.api.tuple.Tuple;
import org.apache.storm.topology.IBasicBolt;
// import com.twitter.heron.api.bolt;
// import com.twitter.heron.api.bolt.IBolt;
import com.twitter.heron.api.bolt.IRichBolt;
// import com.twitter.heron.api.bolt.IBasicBolt;
// import com.twitter.heron.api.bolt.BasicBoltToRichBolt;
// import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.objenesis.strategy.SerializingInstantiatorStrategy;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.testng.annotations.Test;
import com.twitter.heron.api.utils.Utils;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.ByteBufferOutput;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SampleTest {

  public static class SampleTestHelper {

    private SampleTestHelper() {
    }

    // public static Tuple mockTickTuple() {
    // return mockTuple(Constants.SYSTEM_COMPONENT_ID,
    // Constants.SYSTEM_TICK_STREAM_ID);
    // }

    // public static Tuple mockTuple(String componentId, String streamId) {
    // Tuple tuple = mock(Tuple.class);
    // when(tuple.getSourceComponent()).thenReturn(componentId);
    // when(tuple.getSourceStreamId()).thenReturn(streamId);
    // return tuple;
    // }

    // public static Tuple mockAnyTuple() {
    // return mockTuple("testComponent", "testStreamId");
    // }
    // }
  }

  IBasicBolt loadBolt(String name) {

    ClassLoader classLoader = SampleTest.class.getClassLoader();

    BaseBasicBolt bClass;
    try {
      Class aClass = classLoader.loadClass("tutorial.SplitSentenceBolt");
      System.out.println("aClass.getName() = " + aClass.getName());

      bClass = (BaseBasicBolt) classLoader.loadClass("tutorial.SplitSentenceBolt").newInstance();

      IBasicBolt testIBasicBolt = (BaseBasicBolt) classLoader.loadClass("tutorial.SplitSentenceBolt").newInstance();

      // IRichBolt testiRichBolt = new BasicBoltToRichBolt((BaseBasicBolt)
      // classLoader.loadClass("tutorial.SplitSentenceBolt").newInstance());

      // IBolt testIBolt = (IBolt)
      // classLoader.loadClass("tutorial.SplitSentenceBolt").newInstance();
    } catch (ClassNotFoundException ex) {
      ex.printStackTrace();
      throw new RuntimeException(ex + " Bolt class must be in class path.");
    } catch (InstantiationException ex) {
      throw new RuntimeException(ex + " Bolt class must be concrete.");
    } catch (IllegalAccessException ex) {
      throw new RuntimeException(ex + " Bolt class must have a no-arg constructor.");
    }

    return bClass;
  }

  IBasicBolt loadBoltUsingForName(String name) {
    // BaseBasicBolt testClassLoader;
    IBasicBolt testClassLoader;

    Class testClass;

    try {
      // testClassLoader = (BaseBasicBolt)
      // Class.forName("tutorial.SplitSentenceBolt");
      testClassLoader = (BaseBasicBolt) Class.forName("tutorial.SplitSentenceBolt").newInstance();

      testClass = Class.forName("tutorial.SplitSentenceBolt");
      System.out.println("testClass.getName() = " + testClass.getName());
      System.out.println("org.apache.storm.topology.IBasicBolt = " + IBasicBolt.class.isInstance(testClassLoader));
      System.out.println("com.twitter.heron.api.bolt.IBasicBolt = "
          + com.twitter.heron.api.bolt.IBasicBolt.class.isInstance(testClassLoader));

      // System.out.println("testClass instanceOf bolt.IBasicBolt = " + testClass
      // instanceOf
      // bolt.IBasicBolt);

    } catch (ClassNotFoundException ex) {
      ex.printStackTrace();
      throw new RuntimeException(ex + " Bolt class must be in class path.");
    } catch (InstantiationException ex) {
      throw new RuntimeException(ex + " Bolt class must be concrete.");
    } catch (IllegalAccessException ex) {
      throw new RuntimeException(ex + " Bolt class must have a no-arg constructor.");
    }

    return testClassLoader;
  }

  IBasicBolt loadBoltFromBuffer() {

    IRichBolt testRichBolt = new PrintDebugBolt(true, "TEST");

    byte[] testArray = Utils.serialize(testRichBolt);

    byte[] data2=null;
    try {
      Path path = Paths.get("testBinDump.bin");
      Files.write(path, testArray);

      data2 = Files.readAllBytes(path);
    } catch (IOException e) {
      System.out.println("Error dumping to file");
      e.printStackTrace();
    }




    IRichBolt object2 = (IRichBolt) Utils.deserialize(data2);


    // int bufferSize = 10240;
    // Kryo kryo = new Kryo();
    // kryo.setInstantiatorStrategy(new SerializingInstantiatorStrategy());
    // kryo.register(IRichBolt.class);

    // // Output output = new Output(new FileOutputStream("file.bin"));
    // ByteBufferOutput output = new ByteBufferOutput(bufferSize);
    // // ByteBufferOutput output = new ByteBufferOutput(tempBuf);
    // kryo.writeObject(output, testRichBolt);
    // // Buffers so need to flush or close
    // output.close();

    // // Must
    // // ByteBuffer tempBuf = output.getByteBuffer();
    // ByteBufferInput input = new ByteBufferInput(output.toBytes());

    // // Input input = new Input(new FileInputStream("file.bin"));
    // IRichBolt object2 = kryo.readObject(input, IRichBolt.class);
    // input.close();



    // System.out.println(testRichBolt.acksEnabled);
    // System.out.println(object2.acksEnabled);

    // System.out.println(testRichBolt.tupleFields);
    // System.out.println(object2.tupleFields);


    System.out.println(PrintDebugBolt.class.isInstance(testRichBolt));
    System.out.println(PrintDebugBolt.class.isInstance(object2));

    // assertThat(testRichBolt).isEqualTo(object2);
    return null;
  }

  @Test
  public void testClassLoader() {

    IBasicBolt loadedClass;
    IBasicBolt loadedClass2;

    // Load using classsloader
    loadedClass = loadBolt("tutorial.SplitSentenceBolt");

    // Load using Class.forName
    loadedClass = loadBoltUsingForName("tutorial.SplitSentenceBolt");

    loadedClass2 = loadBoltFromBuffer();
  }

  @Test
  public void testIsTickTuple() {
    // assertThat(TupleHelpers.isTickTuple(SampleTestHelper.mockTickTuple())).isTrue();
    // assertThat(TupleHelpers.isTickTuple(SampleTestHelper.mockAnyTuple())).isFalse();

    assertThat(Constants.SYSTEM_COMPONENT_ID.equals("__system")).isEqualTo(true);
    assertThat(Constants.SYSTEM_TICK_STREAM_ID.equals("__tick")).isEqualTo(true);
  }
}
