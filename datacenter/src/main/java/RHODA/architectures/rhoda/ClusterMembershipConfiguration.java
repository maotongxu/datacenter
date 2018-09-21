package RHODA.architectures.rhoda;

import RHODA.architectures.api.DataCenter;
import RHODA.architectures.api.Flow;
import RHODA.architectures.api.Rack;
import RHODA.architectures.common.FlowComparator;

import java.util.*;

public class ClusterMembershipConfiguration {
  private final DataCenterRHODA dataCenterRHODA;
  private final PriorityQueue<Flow> flowPriorityQueue;
  private final Set<Integer> rackInstalledSet;
  private final Map<MSwitchClusterPair, Integer> numOfConnectedClustersPerMSwitchMap;

  public ClusterMembershipConfiguration(final DataCenterRHODA dataCenterRHODA) {
    this.dataCenterRHODA = dataCenterRHODA;
    flowPriorityQueue = new PriorityQueue<>(new FlowComparator());
    rackInstalledSet = new HashSet<>();
    numOfConnectedClustersPerMSwitchMap = new HashMap<>();
  }

  public void configureClusterMembership() {
    addFlowsToPriorityQueue();
    addRackToClusters();
    splitFlowsToIntraInterQueues();
  }

  private void addFlowsToPriorityQueue() {
    DataCenter dataCenter = dataCenterRHODA.getDataCenter();
    for (int r = 0; r < dataCenter.getNumOfRacks(); r++) {
      Rack rack = dataCenter.getRack(r);
      Set<Flow> flowSet = rack.getFlowsWaitingForTx();
      for (Flow flow : flowSet) {
        flowPriorityQueue.add(flow);
      }
    }
  }

  private void addRackToClusters() {
    int clusterId = 0;

    while (!flowPriorityQueue.isEmpty()) {
      if (!canAddMoreRack(clusterId)) {
        clusterId++;
        if (clusterId >= dataCenterRHODA.getNumOfClusters()) {
          break;
        }
        continue;
      }

      Flow flow = flowPriorityQueue.poll();
      RackRHODA rackRHODASrc = dataCenterRHODA.getRackRHODA(flow.getNodeIdSrc());
      RackRHODA rackRHODADst = dataCenterRHODA.getRackRHODA(flow.getNodeIdDst());
      Rack rackSrc = rackRHODASrc.getRack();
      Rack rackDst = rackRHODADst.getRack();

      int mSwitchOfRackSrc = rackSrc.getRackId() / dataCenterRHODA.getNumOfRacksPerMSwitch();
      int mSwitchOfRackDst = rackDst.getRackId() / dataCenterRHODA.getNumOfRacksPerMSwitch();

      MSwitchClusterPair mSwitchClusterPairSrc = new MSwitchClusterPair(mSwitchOfRackSrc, clusterId);
      MSwitchClusterPair mSwitchClusterPairDst = new MSwitchClusterPair(mSwitchOfRackDst, clusterId);

      int numOfConnectedClustersSrc = numOfConnectedClustersPerMSwitchMap.getOrDefault(mSwitchClusterPairSrc, 0);

      if (!rackInstalledSet.contains(rackSrc.getRackId()) &&
          canAddMoreRack(clusterId) &&
          numOfConnectedClustersSrc < dataCenterRHODA.getMaxNumOfClustersPerMSwitch()) {
        dataCenterRHODA.getCluster(clusterId).addRack(rackRHODASrc);
        rackRHODASrc.setClusterId(clusterId);
        dataCenterRHODA.addRackIdClusterId(rackSrc.getRackId(), clusterId);
        rackInstalledSet.add(rackSrc.getRackId());
        numOfConnectedClustersPerMSwitchMap.put(mSwitchClusterPairSrc, numOfConnectedClustersSrc + 1);
      }

      int numOfConnectedClustersDst = numOfConnectedClustersPerMSwitchMap.getOrDefault(mSwitchClusterPairDst, 0);

      if (!rackInstalledSet.contains(rackDst.getRackId()) &&
          canAddMoreRack(clusterId) &&
          numOfConnectedClustersDst < dataCenterRHODA.getMaxNumOfClustersPerMSwitch()) {
        dataCenterRHODA.getCluster(clusterId).addRack(rackRHODADst);
        rackRHODADst.setClusterId(clusterId);
        dataCenterRHODA.addRackIdClusterId(rackDst.getRackId(), clusterId);
        rackInstalledSet.add(rackDst.getRackId());
        numOfConnectedClustersPerMSwitchMap.put(mSwitchClusterPairDst, numOfConnectedClustersDst + 1);
      }
    }

    if (rackInstalledSet.size() >= dataCenterRHODA.getNumOfRacks()) {
      // All racks are installed to clusters
      return;
    }

    /** */
    for (int rackId = 0; rackId < dataCenterRHODA.getNumOfRacks(); rackId++) {
      if (rackInstalledSet.contains(rackId)) {
        continue;
      }

      RackRHODA rackRHODA = dataCenterRHODA.getRackRHODA(rackId);
      Rack rack = rackRHODA.getRack();

      int mSwitchOfRack = rack.getRackId() / dataCenterRHODA.getNumOfRacksPerMSwitch();

      for (int c = 0; c < dataCenterRHODA.getNumOfClusters(); c++) {
        if (!canAddMoreRack(c)) {
          continue;
        }

        MSwitchClusterPair mSwitchClusterPair = new MSwitchClusterPair(mSwitchOfRack, c);
        int numOfConnectedClusters = numOfConnectedClustersPerMSwitchMap.getOrDefault(mSwitchClusterPair, 0);

        if (numOfConnectedClusters >= dataCenterRHODA.getMaxNumOfClustersPerMSwitch()) {
          continue;
        }

        dataCenterRHODA.getCluster(c).addRack(rackRHODA);
        rackRHODA.setClusterId(c);
        dataCenterRHODA.addRackIdClusterId(rack.getRackId(), c);
        rackInstalledSet.add(rack.getRackId());
        break;
      }
    }
  }

  private void splitFlowsToIntraInterQueues() {
    for (int clusterIdSrc = 0; clusterIdSrc < dataCenterRHODA.getNumOfClusters(); clusterIdSrc++) {
      Cluster cluster = dataCenterRHODA.getCluster(clusterIdSrc);
      for (int r = 0; r < cluster.getNumOfRacksInstalled(); r++) {
        RackRHODA rackRHODA = cluster.getRackBasedOnRackIdWithinCluster(r);
        Rack rack = rackRHODA.getRack();
        Set<Flow> flowSet = rack.getFlowsWaitingForTx();

        for (Flow flow : flowSet) {
          int rackIdDst = flow.getNodeIdDst();
          int clusterIdDst = dataCenterRHODA.getClusterIdFromRackId(rackIdDst);
          if (clusterIdSrc == clusterIdDst) {
            rackRHODA.addFlowForIntraClusterTx(flow);
          } else {
            rackRHODA.addFlowForInterClusterTx(flow);
          }
        }
      }
    }
  }

  private boolean canAddMoreRack(final int clusterId) {
    return dataCenterRHODA.getCluster(clusterId).getNumOfRacksInstalled() <
        dataCenterRHODA.getNumOfRacksPerCluster();
  }

  class MSwitchClusterPair {
    private final int mSwitchId;
    private final int clusterId;
    public MSwitchClusterPair(final int mSwitchId,
                              final int clusterId) {
      this.mSwitchId = mSwitchId;
      this.clusterId = clusterId;
    }

    public int getmSwitchId() {
      return mSwitchId;
    }

    public int getClusterId() {
      return clusterId;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      MSwitchClusterPair that = (MSwitchClusterPair) o;
      return mSwitchId == that.mSwitchId &&
          clusterId == that.clusterId;
    }

    @Override
    public int hashCode() {
      return Objects.hash(mSwitchId, clusterId);
    }
  }
}
