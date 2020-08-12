package edgesys.util;

import com.twitter.heron.api.generated.TopologyAPI.StreamId;
import com.twitter.heron.api.tuple.Fields;
import com.twitter.heron.api.tuple.Tuple;
import java.util.List;

public class EdgeSysTuple implements Tuple {
  private static final long serialVersionUID = 7546440749804431895L;
  private List<Object> values;
  private Fields fields;
  private String stream;

  // Need to init these
  private String sourceComponent;
  private String sourceGlobalStreamId;
  private String sourceStreamId;
  private Integer sourceTask;

  public EdgeSysTuple() {}

  public EdgeSysTuple(
      String stream,
      Fields fields,
      List<Object> values,
      String sourceComponent,
      Integer sourceTask) {
    this.stream = stream;
    this.fields = fields;
    this.values = values;
    this.sourceComponent = sourceComponent;
    this.sourceTask = sourceTask;
  }

  String getStream() {
    return this.stream;
  }

  void setValues(List<Object> values) {
    this.values = values;
  }

  void setFields(Fields fields) {
    this.fields = fields;
  }

  @Override
  public boolean contains(String field) {
    return fields.contains(field);
  }

  @Override
  public int fieldIndex(String field) {
    return fields.fieldIndex(field);
  }

  @Override
  public byte[] getBinary(int i) {
    return (byte[]) values.get(i);
  }

  @Override
  public byte[] getBinaryByField(String field) {
    return (byte[]) values.get(fieldIndex(field));
  }

  @Override
  public Boolean getBoolean(int i) {
    return (Boolean) values.get(i);
  }

  @Override
  public Boolean getBooleanByField(String field) {
    return (Boolean) values.get(fieldIndex(field));
  }

  @Override
  public Byte getByte(int i) {
    return (Byte) values.get(i);
  }

  @Override
  public Byte getByteByField(String field) {
    return (Byte) values.get(fieldIndex(field));
  }

  @Override
  public Double getDouble(int i) {
    return (Double) values.get(i);
  }

  @Override
  public Double getDoubleByField(String field) {
    return (Double) values.get(fieldIndex(field));
  }

  @Override
  public Fields getFields() {
    return this.fields;
  }

  @Override
  public Float getFloat(int i) {
    return (Float) values.get(i);
  }

  @Override
  public Float getFloatByField(String field) {
    return (Float) values.get(fieldIndex(field));
  }

  @Override
  public Integer getInteger(int i) {
    return (Integer) values.get(i);
  }

  @Override
  public Integer getIntegerByField(String field) {
    return (Integer) values.get(fieldIndex(field));
  }

  @Override
  public Long getLong(int i) {
    return (Long) values.get(i);
  }

  @Override
  public Long getLongByField(String field) {
    return (Long) values.get(fieldIndex(field));
  }

  @Override
  public Short getShort(int i) {
    return (Short) values.get(i);
  }

  @Override
  public Short getShortByField(String field) {
    return (Short) values.get(fieldIndex(field));
  }

  @Override
  public String getSourceComponent() {
    System.out.println("Not implemented");
    return sourceComponent;
  }

  @Override
  public StreamId getSourceGlobalStreamId() {
    System.out.println("Not implemented");
    return null;
  }

  @Override
  public String getSourceStreamId() {
    System.out.println("Not implemented");
    return sourceStreamId;
  }

  @Override
  public int getSourceTask() {
    System.out.println("Not implemented");
    return sourceTask;
  }

  @Override
  public String getString(int i) {
    return (String) values.get(i);
  }

  @Override
  public String getStringByField(String field) {
    return (String) values.get(fieldIndex(field));
  }

  @Override
  public Object getValue(int i) {
    return values.get(i);
  }

  @Override
  public Object getValueByField(String field) {
    return values.get(fieldIndex(field));
  }

  @Override
  public List<Object> getValues() {
    return values;
  }

  @Override
  public void resetValues() {
    values = null;
  }

  @Override
  public List<Object> select(Fields selector) {
    return getFields().select(selector, values);
  }

  @Override
  public int size() {
    return values.size();
  }
}
