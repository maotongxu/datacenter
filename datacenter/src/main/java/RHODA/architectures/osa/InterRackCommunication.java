package RHODA.architectures.osa;

import RHODA.architectures.api.*;
import RHODA.architectures.api.SrcDstPair;
import RHODA.architectures.common.RoutingRack;
import RHODA.architectures.output.FlowInfo;
import RHODA.architectures.output.Metrics;

import java.util.Iterator;
import java.util.Map;

public class InterRackCommunication {
  private final DataCenter dataCenter;
  private final Map<SrcDstPair, Path> paths;
  private final RoutingRack routing;

  public InterRackCommunication(final Scheduler scheduler,
                                final Map<SrcDstPair, Path> paths) {
    dataCenter = scheduler.getDataCenter();
    this.paths = paths;
    routing = new RoutingRack();
  }

  public void interTx() {
    Iterator<Rack> rackIterator = dataCenter.getRackListIterator();
    while (rackIterator.hasNext()) {
      Rack rack = rackIterator.next();
      Iterator<Flow> flowIterator = rack.getFlowsWaitingForTx().iterator();
      while (flowIterator.hasNext()) {
        Flow flow = flowIterator.next();
        SrcDstPair srcDstPair = new SrcDstPair(flow.getNodeIdSrc(), flow.getNodeIdDst());
        Path path = paths.get(srcDstPair);
        double bd = routing.startRouting(dataCenter, path, flow.getTraffic());
        if (bd < Double.MIN_VALUE) {
          continue;
        }

        rack.setTotalTraffic(rack.getTotalTraffic() - bd);
        if (flow.getTraffic() <= bd) {
          flowIterator.remove();
        } else {
          flow.setTraffic(flow.getTraffic() - bd);
        }
        Metrics.addToOuptut(srcDstPair, path, bd);
      }
    }
  }
}
