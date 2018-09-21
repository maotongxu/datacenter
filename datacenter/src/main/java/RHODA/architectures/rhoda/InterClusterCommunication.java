package RHODA.architectures.rhoda;

import RHODA.architectures.api.Flow;
import RHODA.architectures.api.Path;
import RHODA.architectures.api.SrcDstPair;
import RHODA.architectures.api.Rack;

import RHODA.architectures.common.RoutingRack;
import RHODA.architectures.output.FlowInfo;
import RHODA.architectures.output.Metrics;

import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class InterClusterCommunication {
  private final Scheduler scheduler;
  private final DataCenterRHODA dataCenterRHODA;
  private final Map<SrcDstPair, Path> pathsAmongClusters;
  private final RoutingRack routingRack;

  public InterClusterCommunication(final Scheduler scheduler,
                                   final DataCenterRHODA dataCenterRHODA,
                                   final Map<SrcDstPair, Path> pathsAmongClusters) {
    this.scheduler = scheduler;
    this.dataCenterRHODA = dataCenterRHODA;
    this.pathsAmongClusters = pathsAmongClusters;
    routingRack = new RoutingRack();
  }

  public void loadFlowsToClusterSwitch() {
    for (int c = 0; c < dataCenterRHODA.getNumOfClusters(); c++) {
      Cluster cluster = dataCenterRHODA.getCluster(c);
      ClusterSwitch clusterSwitch = cluster.getClusterSwitch();
      for (RackRHODA rackRHODA : cluster.getRackList()) {
        Iterator<Flow> flowIterator = rackRHODA.flowForInterClusterTxIter();
        while (flowIterator.hasNext()) {
          Flow flow = flowIterator.next();
          Flow flowClone = new Flow(flow.getNodeIdSrc(), flow.getNodeIdDst(), flow.getTraffic());
          clusterSwitch.addFlow(flowClone);
          flowIterator.remove();
        }
      }
    }
  }

  public void interTx() {
    for (int c = 0; c < dataCenterRHODA.getNumOfClusters(); c++) {
      Cluster clusterSrc = dataCenterRHODA.getCluster(c);
      ClusterSwitch clusterSwitch = clusterSrc.getClusterSwitch();

      Iterator<Flow> flowInterIter = clusterSwitch.getFlowSet().iterator();
      while(flowInterIter.hasNext()) {
        Flow flow = flowInterIter.next();
        int clusterIdDst = dataCenterRHODA.getClusterIdFromRackId(flow.getNodeIdDst());
        Cluster clusterDst = dataCenterRHODA.getCluster(clusterIdDst);

        Path path = pathsAmongClusters.get(
            new SrcDstPair(clusterSrc.getClusterId(), clusterDst.getClusterId()));

        routeAmongClusters(flow, path);

        flowInterIter.remove();
      }
    }
  }

  public void routeAmongClusters(final Flow flow, final Path path) {
    for (int c = 1; c < path.pathNodeIdListSize(); c++) {
      int clusterId = path.getPathNodeId(c);
      Cluster cluster = dataCenterRHODA.getCluster(clusterId);
      RackRHODA rackRHODAWithMinTraffic = null;
      double minTraffic = Double.MAX_VALUE;
      for (RackRHODA rackRHODA : cluster.getRackList()) {
        if (minTraffic > rackRHODA.getTotalTrafficInterCluster()) {
          minTraffic = rackRHODA.getTotalTrafficInterCluster();
          rackRHODAWithMinTraffic = rackRHODA;
        }
      }
      rackRHODAWithMinTraffic.setTotalTrafficInterCluster(
          rackRHODAWithMinTraffic.getTotalTrafficInterCluster() + flow.getTraffic());
      int rackId = rackRHODAWithMinTraffic.getRack().getRackId();
      Metrics.rackIdRackLoadMap.put(rackId, Metrics.rackIdRackLoadMap.getOrDefault(rackId, 0.0) + flow.getTraffic());
    }
    SrcDstPair srcDstPair = new SrcDstPair(flow.getNodeIdSrc(), flow.getNodeIdDst());
    FlowInfo flowInfo = new FlowInfo(flow.getTraffic(), path.pathNodeIdListSize() - 1);
    Metrics.srcDstPairFlowInfoMap.put(srcDstPair, flowInfo);
    System.out.println(path.pathNodeIdListSize() - 1);
  }
}
