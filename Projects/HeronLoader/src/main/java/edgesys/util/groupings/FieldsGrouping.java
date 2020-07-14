package edgesys.util.groupings;

import edgesys.util.EdgeSysTuple;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class FieldsGrouping implements IGrouping {
  List<String> targetList;
  Integer currentIdx = 0;
  List<String> fields;
  int primeNumber = 633910111;

  public FieldsGrouping(String targetName, Integer numInstances, List<String> fields) {
    // System.out.println("New fields grouping for " + targetName + " with " + numInstances + "
    // instances and fields: " + fields);
    targetList = new ArrayList<String>();
    this.fields = fields;

    for (int i = 0; i < numInstances; ++i) {
      targetList.add(targetName + "_" + i);
    }
  }

  @Override
  public List<String> getNextTargetIds(EdgeSysTuple tuple) {
    List<String> result = new LinkedList<String>();
    int targetIndex = 0;
    for (String tempField : fields) {
      targetIndex += getHashCode(tuple.getValueByField(tempField)) % primeNumber;
    }
    result.add(targetList.get((targetIndex++) % targetList.size()));
    return result;
  }

  protected int getHashCode(Object o) {
    return o.hashCode();
  }
}
