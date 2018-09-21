package RHODA.architectures.osa;

import RHODA.architectures.api.DataCenter;
import RHODA.architectures.api.Flow;
import RHODA.architectures.api.Rack;
import RHODA.architectures.common.Configuration;
import RHODA.architectures.common.FlowComparator;

import java.util.*;

public class InterRackTopologyConfiguration {
  private final DataCenter dataCenter;
  private final Map<Integer, Set<Integer>> connections;

  public InterRackTopologyConfiguration(final DataCenter dataCenter) {
    this.dataCenter = dataCenter;
    connections = new HashMap<>();
  }

  public void buildInterRackTopology() {
    buildCircleAmongRacks();
    connectRacksBasedOnTraffic();
  }

  private void buildCircleAmongRacks() {
    for (int r = 0; r < dataCenter.getNumOfRacks(); r++) {
      connections.put(r, new HashSet<Integer>());
      int dstRackId = (r + 1) % dataCenter.getNumOfRacks();
      connections.get(r).add(dstRackId);

      Rack rackSrc = dataCenter.getRack(r);
      Rack rackDst = dataCenter.getRack(dstRackId);

      rackSrc.incrNumOfDstConnected();
      rackDst.incrNumOfSrcConnected();
    }
  }

  private void connectRacksBasedOnTraffic() {
    PriorityQueue<Flow> queue = new PriorityQueue<Flow>(new FlowComparator());

    for (int r = 0; r < dataCenter.getNumOfRacks(); r++) {
      Rack rack = dataCenter.getRack(r);
      queue.addAll(rack.getFlowsWaitingForTx());
    }

    while (!queue.isEmpty()) {
      Flow flow = queue.poll();
      int rackIdSrc = flow.getNodeIdSrc();
      int rackIdDst = flow.getNodeIdDst();
      Rack rackSrc = dataCenter.getRack(rackIdSrc);
      Rack rackDst = dataCenter.getRack(rackIdDst);

      if (connections.get(rackIdSrc).contains(rackIdDst)) {
        continue;
      }
      if (!canBeConnected(rackSrc, rackDst)) {
        continue;
      }

      connections.get(rackIdSrc).add(rackIdDst);
      rackSrc.incrNumOfDstConnected();
      rackDst.incrNumOfSrcConnected();
    }

    final int numOfRacks = Configuration.getInstance().getNumOfRacks();
    final int numOfTransceivers = Configuration.getInstance().getNumOfTransceiversPerRack();
    for (int srcId = 0; srcId < numOfRacks; srcId++) {
      Rack rackSrc = dataCenter.getRack(srcId);

      for (int dstId = 0; dstId < numOfRacks; dstId++) {
        if (rackSrc.getNumOfDstConnected() >= numOfTransceivers) {
          break;
        }
        if (srcId == dstId) {
          continue;
        }

        Rack rackDst = dataCenter.getRack(dstId);
        if (connections.get(srcId).contains(dstId)) {
          continue;
        }
        if (rackDst.getNumOfSrcConnected() >= numOfTransceivers) {
          continue;
        }

        connections.get(srcId).add(dstId);
        rackSrc.incrNumOfDstConnected();
        rackDst.incrNumOfSrcConnected();
      }
    }
  }

  private boolean canBeConnected(final Rack rackSrc, final Rack rackDst) {
    final int degree = Configuration.getInstance().getDegreeOfRackOSA();
    return (rackSrc.getNumOfDstConnected() < degree &&
        rackDst.getNumOfSrcConnected() < degree);
  }

  public Map<Integer, Set<Integer>> getConnections() {
    return connections;
  }
}
