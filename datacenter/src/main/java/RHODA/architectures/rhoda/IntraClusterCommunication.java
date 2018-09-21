package RHODA.architectures.rhoda;

import RHODA.architectures.api.Flow;
import RHODA.architectures.api.Path;
import RHODA.architectures.api.SrcDstPair;
import RHODA.architectures.api.Rack;
import RHODA.architectures.output.FlowInfo;
import RHODA.architectures.output.Metrics;

import java.util.Iterator;
import java.util.Map;

public class IntraClusterCommunication {
  private static final double NEGLIGIBLE_FLOW_TRAFFIC = 0.00001;

  private final Scheduler scheduler;
  private final DataCenterRHODA dataCenterRHODA;
  private final Map<Integer, Map<SrcDstPair, Path>> pathsWithinCluster;
  private final RoutingWithinCluster routingWithinCluster;

  public IntraClusterCommunication(final Scheduler scheduler,
                                   final DataCenterRHODA dataCenterRHODA,
                                   final Map<Integer, Map<SrcDstPair, Path>> pathsWithinCluster) {
    this.scheduler = scheduler;
    this.dataCenterRHODA = dataCenterRHODA;
    this.pathsWithinCluster = pathsWithinCluster;
    routingWithinCluster = new RoutingWithinCluster();
  }

  public void rackToRackTx() {
    for (int clusterId : pathsWithinCluster.keySet()) {
      Cluster cluster = dataCenterRHODA.getCluster(clusterId);
      Map<SrcDstPair, Path> paths = pathsWithinCluster.get(clusterId);

      for (int r = 0; r < cluster.getNumOfRacksInstalled(); r++) {
        RackRHODA rackRHODASrc = cluster.getRackBasedOnRackIdWithinCluster(r);
        Rack rackSrc = rackRHODASrc.getRack();

        Iterator<Flow> flowIntraIter = rackRHODASrc.flowForIntraClusterTxIter();
        while(flowIntraIter.hasNext()) {
          Flow flow = flowIntraIter.next();
          int rackIdDstWithinCluster = cluster.getRackIdWithinCluster(flow.getNodeIdDst());

          Path path = paths.get(new SrcDstPair(
              rackRHODASrc.getRackIdWithinCluster(), rackIdDstWithinCluster));
          double bd = routingWithinCluster.startRouting(cluster, path, flow.getTraffic());
          if (bd <= NEGLIGIBLE_FLOW_TRAFFIC) {
            continue;
          }

          rackRHODASrc.setTotalTrafficIntraCluster(rackRHODASrc.getTotalTrafficIntraCluster() - bd);

          int rackIdSrc = flow.getNodeIdSrc();
          int rackIdDst = flow.getNodeIdDst();

          if (flow.getTraffic() <= bd) {
            rackSrc.removeFlow(flow);
            flowIntraIter.remove();
          } else {
            flow.setTraffic(flow.getTraffic() - bd);
          }

          SrcDstPair srcDstPair = new SrcDstPair(rackIdSrc, rackIdDst);
          Metrics.addToOuptut(srcDstPair, path, bd);
          System.out.println("Intra " + (path.pathNodeIdListSize() - 1));
        }
      }
    }
  }
}
