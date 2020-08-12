// Bulk comes from
// https://github.com/ADSC-Resa/resaVLDTopology/blob/master/src/main/java/tool/Serializable.java

package examples.videoEdgeWorkload.tools;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import java.io.*;

// import org.opencv.core;

public class Serializable {

  /**
   * Kryo Serializable Mat class. Essential fields are image data itself, rows and columns count and
   * type of the data.
   */
  public static class Mat implements KryoSerializable, java.io.Serializable {
    private byte[] data;
    private int rows, cols, type;

    public int getRows() {
      return rows;
    }

    public int getCols() {
      return cols;
    }

    public int getType() {
      return type;
    }

    public Mat() {}

    /**
     * Creates new serializable Mat given its format and data.
     *
     * @param rows Number of rows in the Mat object
     * @param cols Number of columns in the Mat object
     * @param type OpenCV type of the data in the Mat object
     * @param data Byte data containing image.
     */
    public Mat(int rows, int cols, int type, byte[] data) {
      this.rows = rows;
      this.cols = cols;
      this.type = type;
      this.data = data;
    }

    /**
     * Creates new serializable Mat from org.opencv.Mat
     *
     * @param mat The org.opencv.Mat
     */
    public Mat(org.opencv.core.Mat mat) {
      if (!mat.isContinuous()) mat = mat.clone();

      this.rows = mat.rows();
      this.cols = mat.cols();
      this.type = mat.type();
      this.data = new byte[(int) (mat.total() * mat.elemSize())];
      mat.get(0, 0, this.data);
    }

    /**
     * Creates new serializable Mat given its format and data.
     *
     * @param input Byte data containing image.
     */
    public Mat(byte[] input) {
      ByteArrayInputStream bis = new ByteArrayInputStream(input);
      ObjectInput in = null;
      try {
        in = new ObjectInputStream(bis);
        this.rows = in.readInt();
        this.cols = in.readInt();
        this.type = in.readInt();
        int size = in.readInt();
        this.data = new byte[size];
        int readed = 0;
        while (readed < size) {
          readed += in.read(data, readed, size - readed);
        }
        // System.out.println("in: " + this.rows + "-" + this.cols + "-" + this.type + "-" + size +
        // "-" + readed);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    public byte[] toByteArray() {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ObjectOutput out = null;
      try {
        out = new ObjectOutputStream(bos);
        out.writeInt(this.rows);
        out.writeInt(this.cols);
        out.writeInt(this.type);
        out.writeInt(this.data.length);
        out.write(this.data);
        out.close();
        byte[] int_bytes = bos.toByteArray();
        bos.close();

        // System.out.println("out: " + this.rows + "-" + this.cols + "-" + this.type + "-" +
        // this.data.length + "-" + int_bytes.length);
        return int_bytes;
      } catch (IOException e) {
        e.printStackTrace();
      }
      return null;
    }

    public static Serializable.Mat[] toSMat(byte[] input) {
      ByteArrayInputStream bis = new ByteArrayInputStream(input);
      ObjectInput in = null;
      Serializable.Mat rawFrame = new Serializable.Mat();
      Serializable.Mat optFlow = new Serializable.Mat();

      try {
        in = new ObjectInputStream(bis);
        rawFrame.rows = in.readInt();
        rawFrame.cols = in.readInt();
        rawFrame.type = in.readInt();
        int size = in.readInt();
        rawFrame.data = new byte[size];
        int readed = 0;
        while (readed < size) {
          readed += in.read(rawFrame.data, readed, size - readed);
        }
        optFlow.rows = in.readInt();
        optFlow.cols = in.readInt();
        optFlow.type = in.readInt();
        size = in.readInt();
        optFlow.data = new byte[size];
        readed = 0;
        while (readed < size) {
          readed += in.read(optFlow.data, readed, size - readed);
        }

        return new Serializable.Mat[] {rawFrame, optFlow};
      } catch (IOException e) {
        e.printStackTrace();
      }
      return null;
    }

    /** @return Converts this Serializable Mat into JavaCV's Mat */
    public org.opencv.core.Mat toOpenCVMat() {
      org.opencv.core.Mat mat = new org.opencv.core.Mat(rows, cols, type);
      mat.put(0, 0, data);
      return mat;
    }

    @Override
    public void write(Kryo kryo, Output output) {
      output.writeInt(this.rows);
      output.writeInt(this.cols);
      output.writeInt(this.type);
      output.writeInt(this.data.length);
      output.writeBytes(this.data);
    }

    @Override
    public void read(Kryo kryo, Input input) {
      this.rows = input.readInt();
      this.cols = input.readInt();
      this.type = input.readInt();
      int size = input.readInt();
      this.data = input.readBytes(size);
    }
  }

  /** Kryo Serializable Rect class. */
  public static class Rect implements KryoSerializable, java.io.Serializable {
    /**
     * x, y, width, height - x and y coordinates of the left upper corner of the rectangle, its
     * width and height
     */
    public int x, y, width, height;

    public Rect() {}

    public Rect(org.opencv.core.Rect rect) {
      x = rect.x;
      y = rect.y;
      width = rect.width;
      height = rect.height;
    }

    public Rect(int x, int y, int width, int height) {
      this.x = x;
      this.y = y;
      this.height = height;
      this.width = width;
    }

    // public org.opencv.core.Rect toJavaCVRect() {
    public org.opencv.core.Rect toOpenCVRect() {
      return new org.opencv.core.Rect(x, y, width, height);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Rect rect = (Rect) o;

      if (height != rect.height) return false;
      if (width != rect.width) return false;
      if (x != rect.x) return false;
      if (y != rect.y) return false;

      return true;
    }

    @Override
    public int hashCode() {
      int result = x;
      result = 31 * result + y;
      result = 31 * result + width;
      result = 31 * result + height;
      return result;
    }

    @Override
    public void write(Kryo kryo, Output output) {
      output.writeInt(x);
      output.writeInt(y);
      output.writeInt(width);
      output.writeInt(height);
    }

    @Override
    public void read(Kryo kryo, Input input) {
      x = input.readInt();
      y = input.readInt();
      width = input.readInt();
      height = input.readInt();
    }
  }
}
