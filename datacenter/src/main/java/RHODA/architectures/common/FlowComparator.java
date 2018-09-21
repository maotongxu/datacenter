package RHODA.architectures.common;

import RHODA.architectures.api.Flow;

import java.util.Comparator;

public   class FlowComparator implements Comparator<Flow> {
  public int compare(final Flow f1, final Flow f2) {
    return -Double.compare(f1.getTraffic(), f2.getTraffic());
  }
}
