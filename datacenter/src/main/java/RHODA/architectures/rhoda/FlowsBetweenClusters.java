package RHODA.architectures.rhoda;

import RHODA.architectures.api.Flow;
import RHODA.architectures.common.FlowComparator;

import java.util.PriorityQueue;

public class FlowsBetweenClusters {
  private final int srcClusterId;
  private final int dstClusterId;
  private final PriorityQueue<Flow> flowPriorityQueue;

  private double totalFlowTraffic;

  public FlowsBetweenClusters(final int srcClusterId, final int dstClusterId) {
    this.srcClusterId = srcClusterId;
    this.dstClusterId = dstClusterId;
    flowPriorityQueue = new PriorityQueue<>(new FlowComparator());
  }

  public int getSrcClusterId() {
    return srcClusterId;
  }

  public int getDstClusterId() {
    return dstClusterId;
  }

  public double getTotalFlowTraffic() {
    return totalFlowTraffic;
  }

  public void setTotalFlowTraffic(double totalFlowTraffic) {
    this.totalFlowTraffic = totalFlowTraffic;
  }

  public void addFlow(final Flow flow) {
    flowPriorityQueue.add(flow);
    totalFlowTraffic += flow.getTraffic();
  }

  public Flow pop() {
    Flow flow = flowPriorityQueue.poll();
    totalFlowTraffic -= flow.getTraffic();
    return flow;
  }
}
