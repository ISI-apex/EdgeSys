package edgesys.util.groupings;

import edgesys.util.EdgeSysTuple;
import java.util.List;

public interface IGrouping {
  // Queue names: boltInstanceName_InstanceIdx
  List<String> getNextTargetIds(EdgeSysTuple tuple);
}
