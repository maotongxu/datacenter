package RHODA.architectures.rhoda.InterClusterTopology;

import RHODA.architectures.api.Flow;
import RHODA.architectures.api.SrcDstPair;
import RHODA.architectures.api.Rack;
import RHODA.architectures.common.Configuration;
import RHODA.architectures.common.FlowComparator;
import RHODA.architectures.rhoda.Cluster;
import RHODA.architectures.rhoda.DataCenterRHODA;
import RHODA.architectures.rhoda.RackRHODA;

import java.util.*;

public class InterClusterTopologyFully {
  private final DataCenterRHODA dataCenterRHODA;
  private final Map<Integer, Set<Integer>> connections;

  public InterClusterTopologyFully(final DataCenterRHODA dataCenterRHODA) {
    this.dataCenterRHODA = dataCenterRHODA;
    connections = new HashMap<>();
  }

  public void buildInterClusterTopology() {
    buildInterClusterTopology();
    connectClustersBasedOnTraffic();
  }

  public Map<Integer, Set<Integer>> getInterClusterTopology() {
    return connections;
  }

  private void buildCircleAmongClusters() {
    for (int c = 0; c < dataCenterRHODA.getNumOfClusters(); c++) {
      connections.put(c, new HashSet<>());
      int clusterIdDst = (c + 1) % dataCenterRHODA.getNumOfClusters();

      Cluster clusterSrc = dataCenterRHODA.getCluster(c);
      Cluster clusterDst = dataCenterRHODA.getCluster(clusterIdDst);

      connections.get(c).add(clusterIdDst);
      clusterSrc.addClusterConnectedTo(clusterDst);
    }
  }

  private void connectClustersBasedOnTraffic() {
    Map<SrcDstPair, Double> clustersTrafficMap = new HashMap<>();
    for (int clusterIdSrc = 0; clusterIdSrc < dataCenterRHODA.getNumOfClusters(); clusterIdSrc++) {
      Cluster clusterSrc = dataCenterRHODA.getCluster(clusterIdSrc);
      for (int r = 0; r < clusterSrc.getNumOfRacksInstalled(); r++) {
        RackRHODA rackRHODA = clusterSrc.getRackBasedOnRackIdWithinCluster(r);
        Rack rack = rackRHODA.getRack();
        Set<Flow> flowSet = rack.getFlowsWaitingForTx();

        for (Flow flow : flowSet) {
          int rackIdDst = flow.getNodeIdDst();
          int clusterIdDst = dataCenterRHODA.getClusterIdFromRackId(rackIdDst);

          SrcDstPair clusterPair = new SrcDstPair(clusterIdSrc, clusterIdDst);
          clustersTrafficMap.put(clusterPair,
              clustersTrafficMap.getOrDefault(clusterPair, 0.0) + flow.getTraffic());
        }
      }
    }

    PriorityQueue<Flow> flowPriorityQueue = new PriorityQueue<>(new FlowComparator());
    for (SrcDstPair p : clustersTrafficMap.keySet()) {
      Flow flow = new Flow(p.getNodeIdSrc(), p.getNodeIdDst(), clustersTrafficMap.get(p));
      flowPriorityQueue.add(flow);
    }

    while (!flowPriorityQueue.isEmpty()) {
      Flow flowBetweenClusters = flowPriorityQueue.poll();
      int clusterIdSrc = flowBetweenClusters.getNodeIdSrc();
      int clusterIdDst = flowBetweenClusters.getNodeIdDst();

      Cluster clusterSrc = dataCenterRHODA.getCluster(clusterIdSrc);
      Cluster clusterDst = dataCenterRHODA.getCluster(clusterIdDst);

      if (connections.get(clusterIdSrc).contains(clusterIdDst)) {
        continue;
      }
      if (!canConnectWithNewCluster(clusterSrc)) {
        continue;
      }

      connections.get(clusterIdSrc).add(clusterIdDst);
      clusterSrc.addClusterConnectedTo(clusterDst);
    }

    for (int c = 0; c < dataCenterRHODA.getNumOfClusters(); c++) {
      Cluster cluster = dataCenterRHODA.getCluster(c);
      int clusterIdDst = 0;
      while (clusterIdDst < dataCenterRHODA.getNumOfClusters()) {
        if (!canConnectWithNewCluster(cluster)) {
          break;
        }
        while (c == clusterIdDst || connections.get(c).contains(clusterIdDst)) {
          clusterIdDst++;
        }
        if (clusterIdDst >= dataCenterRHODA.getNumOfClusters()) {
          break;
        }

        connections.get(c).add(clusterIdDst);
        cluster.addClusterConnectedTo(dataCenterRHODA.getCluster(clusterIdDst));

        clusterIdDst++;
      }
    }
  }

  private boolean canConnectWithNewCluster(final Cluster cluster) {
    return cluster.numClustersConnectedTo() < Configuration.getInstance().getOutDegreePerCluster();
  }
}
