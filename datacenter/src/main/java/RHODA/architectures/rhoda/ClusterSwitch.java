package RHODA.architectures.rhoda;

import RHODA.architectures.api.Flow;

import java.util.Set;
import java.util.HashSet;

public class ClusterSwitch {
  private final Set<Flow> flowSet = new HashSet<>();

  public ClusterSwitch() {

  }

  public void addFlow(final Flow flow) {
    flowSet.add(flow);
  }

  public Set<Flow> getFlowSet() {
    return flowSet;
  }
}
