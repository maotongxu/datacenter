package RHODA.architectures.rhoda;

import RHODA.architectures.api.DataCenter;
import RHODA.architectures.api.SrcDstPair;
import RHODA.architectures.api.Path;
import RHODA.architectures.common.ShortestPathAmongNodes;
import RHODA.architectures.common.TrafficPattern;
import RHODA.architectures.output.Metrics;
import RHODA.architectures.rhoda.InterClusterTopology.InterClusterTopologyPartial;
import RHODA.architectures.rhoda.IntraClusterTopology.IntraClusterTopologyConfigurationH;
import RHODA.architectures.rhoda.IntraClusterTopology.IntraClusterTopologyConfigurationSN;
import com.google.common.annotations.VisibleForTesting;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Scheduler {
  private DataCenterRHODA dataCenterRHODA;

  public Scheduler() {

  }

  public void init() {
    DataCenter dataCenter = new DataCenter();
    TrafficPattern trafficPattern = new TrafficPattern(dataCenter);
    trafficPattern.importTraffic();

    dataCenterRHODA = new DataCenterRHODA(dataCenter);
  }

  public void start() {
    ClusterMembershipConfiguration clusterMembership =
        new ClusterMembershipConfiguration(dataCenterRHODA);
    clusterMembership.configureClusterMembership();

    IntraClusterTopologyConfigurationH intraClusterTopology =
        new IntraClusterTopologyConfigurationH(dataCenterRHODA);
    intraClusterTopology.buildConnections();
    Map<Integer, Map<Integer, Set<Integer>>> withinClusterConnections =
        intraClusterTopology.getWithinClusterConnections();
    Map<Integer, Map<SrcDstPair, Path>> pathsWithinCluster =
        findShortestPathIntra(withinClusterConnections);

    IntraClusterCommunication intraClusterCommunication =
        new IntraClusterCommunication(this, dataCenterRHODA, pathsWithinCluster);
    intraClusterCommunication.rackToRackTx();

    InterClusterTopologyPartial interClusterTopologyPartial =
        new InterClusterTopologyPartial(dataCenterRHODA);
    interClusterTopologyPartial.buildInterClusterTopology();
    Map<Integer, Set<Integer>> connectionsAmongClusters =
        interClusterTopologyPartial.getInterClusterTopology();

    ShortestPathAmongNodes shortestPathAmongNodes =
        new ShortestPathAmongNodes(connectionsAmongClusters);
    shortestPathAmongNodes.findShortestPath();
    Map<SrcDstPair, Path> paths = shortestPathAmongNodes.getPairPathMap();

    InterClusterCommunication interClusterCommunication =
        new InterClusterCommunication(this, dataCenterRHODA, paths);
    interClusterCommunication.loadFlowsToClusterSwitch();
    interClusterCommunication.interTx();
  }

  @VisibleForTesting
  public Map<Integer, Map<SrcDstPair, Path>> findShortestPathIntra(
      Map<Integer, Map<Integer, Set<Integer>>> withinClusterConnections) {
    Map<Integer, Map<SrcDstPair, Path>> result = new HashMap<>();
    for (int clusterId : withinClusterConnections.keySet()) {
      Map<Integer, Set<Integer>> connections = withinClusterConnections.get(clusterId);
      ShortestPathAmongNodes shortestPath = new ShortestPathAmongNodes(connections);
      shortestPath.findShortestPath();
      Map<SrcDstPair, Path> paths = shortestPath.getPairPathMap();
      result.put(clusterId, paths);
    }
    return result;
  }

  public static void main(String[] args) {
    Metrics.init();
    Scheduler scheduler = new Scheduler();
    scheduler.init();
    scheduler.start();
    System.out.println("NumOfHops " + Metrics.calculateAvgNumOfHops() +
        " AvgRackLoad " + Metrics.calculateAvgRackLoad() +
        " TotalTraffic " + Metrics.calculateTotalTraffic());
  }
}
