package edgesys.util.groupings;

import edgesys.util.EdgeSysTuple;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ShuffleGrouping implements IGrouping {
  List<String> targetList;
  Integer currentIdx = 0;

  public ShuffleGrouping(String targetName, Integer numInstances) {
    // System.out.println("************** New shuffle grouping for " + targetName + " with " +
    // numInstances + " instances");
    targetList = new ArrayList<String>();

    for (int i = 0; i < numInstances; ++i) {
      targetList.add(targetName + "_" + i);
    }
  }

  @Override
  public List<String> getNextTargetIds(EdgeSysTuple tuple) {
    // TODO Auto-generated method stub
    List<String> result = new LinkedList<String>();
    result.add(targetList.get((currentIdx++) % targetList.size()));
    return result;
  }
}
