package RHODA.architectures.rhoda.IntraClusterTopology;

import RHODA.architectures.api.Flow;
import RHODA.architectures.api.Rack;
import RHODA.architectures.common.Configuration;
import RHODA.architectures.common.FlowComparator;

import RHODA.architectures.rhoda.Cluster;
import RHODA.architectures.rhoda.DataCenterRHODA;
import RHODA.architectures.rhoda.RackRHODA;

import com.google.common.annotations.VisibleForTesting;

import java.util.*;

public class IntraClusterTopologyConfigurationH {
  private final DataCenterRHODA dataCenterRHODA;
  private final int numOfRacksPerCluster;
  private final Map<Integer, Map<Integer, Set<Integer>>> withinClusterConnections;

  public IntraClusterTopologyConfigurationH(final DataCenterRHODA dataCenterRHODA) {
    this.dataCenterRHODA = dataCenterRHODA;
    numOfRacksPerCluster = Configuration.getInstance().getNumOfRacksPerCluster();
    withinClusterConnections = new HashMap<>();
  }

  public void buildConnections() {
    connectTopology();
    for (int c = 0; c < dataCenterRHODA.getNumOfClusters(); c++) {
      Cluster cluster = dataCenterRHODA.getCluster(c);
      connectRacksBasedOnTraffic(cluster);
    }
  }

  public Map<Integer, Map<Integer, Set<Integer>>> getWithinClusterConnections() {
    return withinClusterConnections;
  }

  public void connectTopology() {
    for (int c = 0; c < dataCenterRHODA.getNumOfClusters(); c++) {
      Cluster cluster = dataCenterRHODA.getCluster(c);
      connectTopology(cluster);
    }
  }

  private void connectTopology(final Cluster cluster) {
    RackRHODA rackRHODASrc = selectARackRHODA(cluster);

    Set<Integer> installedRackId = new HashSet<>();
    Map<Integer, Set<Integer>> connectionsOfACluster= new HashMap<>();

    RackRHODA rackRHODAFirst = rackRHODASrc;
    RackRHODA rackRHODALast = null;
    for (int r = 1; r < numOfRacksPerCluster; r++) {
      installedRackId.add(rackRHODASrc.getRack().getRackId());

      PriorityQueue<Flow> queueForIntraFlows = new PriorityQueue<>(new FlowComparator());
      Iterator<Flow> flowIterator = rackRHODASrc.flowForIntraClusterTxIter();

      while (flowIterator.hasNext()) {
        Flow flow = flowIterator.next();
        queueForIntraFlows.add(flow);
      }

      RackRHODA rackRHODADst = null;
      while (!queueForIntraFlows.isEmpty()) {
        Flow flow = queueForIntraFlows.poll();
        int rackIdDst = flow.getNodeIdDst();
        if (installedRackId.contains(rackIdDst)) {
          continue;
        }
        rackRHODADst = dataCenterRHODA.getRackRHODABasedOnRackId(rackIdDst);
      }

      if (rackRHODADst == null) {
        for (int i = 0; i < numOfRacksPerCluster; i++) {
          RackRHODA rackRHODA = cluster.getRackBasedOnRackIdWithinCluster(i);
          if (!installedRackId.contains(rackRHODA.getRack().getRackId())) {
            rackRHODADst = rackRHODA;
            break;
          }
        }
      }

      Rack rackSrc = rackRHODASrc.getRack();
      Rack rackDst = rackRHODADst.getRack();

      rackSrc.incrNumOfDstConnected();
      rackDst.incrNumOfSrcConnected();

      connectionsOfACluster.put(rackRHODASrc.getRackIdWithinCluster(), new HashSet<>());
      connectionsOfACluster.get(rackRHODASrc.getRackIdWithinCluster()).add(rackRHODADst.getRackIdWithinCluster());

      rackRHODASrc = rackRHODADst;
      rackRHODALast = rackRHODADst;
    }

    connectionsOfACluster.put(rackRHODALast.getRackIdWithinCluster(), new HashSet<>());
    connectionsOfACluster.get(rackRHODALast.getRackIdWithinCluster()).add(rackRHODAFirst.getRackIdWithinCluster());
    withinClusterConnections.put(cluster.getClusterId(), connectionsOfACluster);
  }

  public RackRHODA selectARackRHODA(final Cluster cluster) {
    double largestFlowTraffic = -1;
    RackRHODA result = null;
    for (int r = 0; r < numOfRacksPerCluster; r++) {
      RackRHODA rackRHODASrc = cluster.getRackBasedOnRackIdWithinCluster(r);
      Flow flow = rackRHODASrc.peekFlowforIntraClusterTx();
      if (flow != null && flow.getTraffic() > largestFlowTraffic) {
        largestFlowTraffic = flow.getTraffic();
        result = rackRHODASrc;
      }
    }
    if (result == null) {
      result = cluster.getRackBasedOnRackIdWithinCluster(0);
    }
    return result;
  }

  private void connectRacksBasedOnTraffic(final Cluster cluster) {

    int numOfTRPerRack = Configuration.getInstance().getNumOfTransceiversPerRack();

    PriorityQueue<Flow> queueForIntraFlows = new PriorityQueue<>(new FlowComparator());

    Map<Integer, Set<Integer>> connectionOfACluster =
        withinClusterConnections.get(cluster.getClusterId());

    // Sort all flows for intra-cluster communication based on traffic
    for (int r = 0; r < numOfRacksPerCluster; r++) {
      RackRHODA rackRHODA = cluster.getRackBasedOnRackIdWithinCluster(r);
      Iterator<Flow> flowIterator = rackRHODA.flowForIntraClusterTxIter();
      while (flowIterator.hasNext()) {
        Flow flow = flowIterator.next();
        if (flow != null) {
          queueForIntraFlows.add(flow);
        }
      }
    }

    while (!queueForIntraFlows.isEmpty()) {
      Flow flow = queueForIntraFlows.poll();
      int rackIdSrcWithinCluster = cluster.getRackIdWithinCluster(flow.getNodeIdSrc());
      int rackIdDstWithinCluster = cluster.getRackIdWithinCluster(flow.getNodeIdDst());

      RackRHODA rackRHODASrc = cluster.getRackBasedOnRackIdWithinCluster(rackIdSrcWithinCluster);
      RackRHODA rackRHODADst = cluster.getRackBasedOnRackIdWithinCluster(rackIdDstWithinCluster);

      Rack rackSrc = rackRHODASrc.getRack();
      Rack rackDst = rackRHODADst.getRack();

      if (connectionOfACluster.get(rackIdSrcWithinCluster).contains(rackIdDstWithinCluster)) {
        continue;
      }
      if (rackSrc.getNumOfDstConnected() >= numOfTRPerRack ||
          rackDst.getNumOfSrcConnected() >= numOfTRPerRack) {
        continue;
      }

      connectionOfACluster.get(rackIdSrcWithinCluster).add(rackIdDstWithinCluster);
      rackSrc.incrNumOfDstConnected();
      rackDst.incrNumOfSrcConnected();
    }


    // Check rack by rack to see if we can build more connections
    for (int srcId = 0 ; srcId < numOfRacksPerCluster; srcId++) {
      RackRHODA rackRHODASrc = cluster.getRackBasedOnRackIdWithinCluster(srcId);
      Rack rackSrc = rackRHODASrc.getRack();

      for (int dstId = 0; dstId < numOfRacksPerCluster; dstId++) {
        if (rackSrc.getNumOfDstConnected() >= numOfTRPerRack) {
          break;
        }
        if (srcId == dstId ||
            connectionOfACluster.get(srcId).contains(dstId)) {
          continue;
        }

        RackRHODA rackRHODADst = cluster.getRackBasedOnRackIdWithinCluster(dstId);
        Rack rackDst = rackRHODADst.getRack();
        if (rackDst.getNumOfSrcConnected() >= numOfTRPerRack) {
          continue;
        }

        connectionOfACluster.get(srcId).add(dstId);
        rackSrc.incrNumOfDstConnected();
        rackDst.incrNumOfSrcConnected();
      }
    }

    withinClusterConnections.put(cluster.getClusterId(), connectionOfACluster);
  }
}
