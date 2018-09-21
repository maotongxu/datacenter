package RHODA.architectures.rhoda;

import RHODA.architectures.api.DataCenter;
import RHODA.architectures.api.Rack;
import RHODA.architectures.common.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataCenterRHODA {
  private final DataCenter dataCenter;
  private final List<RackRHODA> rackRHODAList;
  private final List<Cluster> clusterList;
  private final Map<Integer, Integer> rackIdClusterIdMap;
  private final Map<Integer, RackRHODA> rackIdRackRHODAMap;
  private final Map<Integer, Cluster> clusterIdClusterMap;

  private final int numOfRacks;
  private final int numOfRacksPerCluster;
  private final int outDegreePerCluster;
  private final int numOfClusters;

  private final int membershipRatio;
  private final int numOfRacksPerMSwitch;
  private final int maxNumOfClustersPerMSwitch;
  private final int numOfMSwitches;

  public DataCenterRHODA(final DataCenter dataCenter) {
    this.dataCenter = dataCenter;
    rackRHODAList = new ArrayList<>();
    clusterList = new ArrayList<>();
    rackIdClusterIdMap = new HashMap<>();
    rackIdRackRHODAMap = new HashMap<>();
    clusterIdClusterMap = new HashMap<>();

    numOfRacks = Configuration.getInstance().getNumOfRacks();
    numOfRacksPerCluster = Configuration.getInstance().getNumOfRacksPerCluster();
    outDegreePerCluster = Configuration.getInstance().getOutDegreePerCluster();
    numOfClusters = (int) Math.ceil((double) numOfRacks / numOfRacksPerCluster);

    membershipRatio = Configuration.getInstance().getMembershipRatio();
    numOfRacksPerMSwitch = membershipRatio * numOfClusters;
    maxNumOfClustersPerMSwitch = membershipRatio;
    numOfMSwitches = (int) Math.ceil((double) numOfRacks / numOfRacksPerMSwitch);

    addRackRHODAToDataCenter();
    addClusterToDataCenter();
  }

  public DataCenter getDataCenter() {
    return dataCenter;
  }

  public void addRackRHODAToDataCenter() {
    for (int r = 0; r < numOfRacks; r++) {
      Rack rack = dataCenter.getRack(r);
      RackRHODA rackRHODA = new RackRHODA(rack);
      rackRHODAList.add(rackRHODA);
      rackIdRackRHODAMap.put(rack.getRackId(), rackRHODA);
    }
  }

  private void addClusterToDataCenter() {
    for (int c = 0; c < numOfClusters; c++) {
      Cluster cluster = new Cluster(c);
      clusterList.add(cluster);
      clusterIdClusterMap.put(cluster.getClusterId(), cluster);
    }
  }

  public RackRHODA getRackRHODA(final int rackRHODAId) {
    return rackRHODAList.get(rackRHODAId);
  }

  public Cluster getCluster(final int clusterId) {
    return clusterList.get(clusterId);
  }

  public void addRackIdClusterId(final int rackId, final int clusterId) {
    rackIdClusterIdMap.put(rackId, clusterId);
  }

  public int getClusterIdFromRackId(final int rackId) {
    if (!rackIdClusterIdMap.containsKey(rackId)) {
      throw new IllegalArgumentException("rack has been installed to a cluster " + rackId);
    }
    return rackIdClusterIdMap.get(rackId);
  }

  public int getNumOfRacks() {
    return numOfRacks;
  }

  public int getNumOfRacksPerCluster() {
    return numOfRacksPerCluster;
  }

  public int getOutDegreePerCluster() {
    return outDegreePerCluster;
  }

  public int getNumOfClusters() {
    return numOfClusters;
  }

  public int getMembershipRatio() {
    return membershipRatio;
  }

  public int getNumOfRacksPerMSwitch() {
    return numOfRacksPerMSwitch;
  }

  public int getMaxNumOfClustersPerMSwitch() {
    return maxNumOfClustersPerMSwitch;
  }

  public int getNumOfMSwitches() {
    return numOfMSwitches;
  }

  public RackRHODA getRackRHODABasedOnRackId(final int rackId) {
    return rackIdRackRHODAMap.get(rackId);
  }
}
