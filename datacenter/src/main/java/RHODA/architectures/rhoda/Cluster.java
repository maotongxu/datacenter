package RHODA.architectures.rhoda;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Cluster {
  private final int clusterId;
  private final List<RackRHODA> rackList;

  private final List<Cluster> clusterConnectedToList;
  private final Map<Integer, Integer> mapRackIdToRackIdWithinCluster;

  private final ClusterSwitch clusterSwitch;

  private int rackIdWithinCluster;

  public Cluster(final int clusterId) {
    this.clusterId = clusterId;
    rackList = new ArrayList<>();
    clusterConnectedToList = new ArrayList<>();
    mapRackIdToRackIdWithinCluster = new HashMap<>();
    clusterSwitch = new ClusterSwitch();
  }

  public int getClusterId() {
    return clusterId;
  }

  public void addRack(final RackRHODA rackRHODA) {
    mapRackIdToRackIdWithinCluster.put(rackRHODA.getRack().getRackId(), rackList.size());
    rackList.add(rackRHODA);

    rackRHODA.setRackIdWithinCluster(rackIdWithinCluster);
    rackIdWithinCluster++;
  }

  public int getRackIdWithinCluster(final int rackId) {
    return mapRackIdToRackIdWithinCluster.get(rackId);
  }

  public RackRHODA getRackBasedOnRackIdWithinCluster(final int rackIdWithinCluster) {
    return rackList.get(rackIdWithinCluster);
  }

  public void addClusterConnectedTo(final Cluster cluster) {
    clusterConnectedToList.add(cluster);
  }

  public int numClustersConnectedTo() {
    return clusterConnectedToList.size();
  }

  public int getNumOfRacksInstalled() {
    return rackList.size();
  }

  public List<RackRHODA> getRackList() {
    return rackList;
  }

  public ClusterSwitch getClusterSwitch() {
    return clusterSwitch;
  }
}
