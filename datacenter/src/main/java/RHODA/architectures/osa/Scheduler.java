package RHODA.architectures.osa;

import RHODA.architectures.api.DataCenter;
import RHODA.architectures.api.SrcDstPair;
import RHODA.architectures.api.Path;
import RHODA.architectures.common.ShortestPathAmongNodes;
import RHODA.architectures.common.TrafficPattern;
import RHODA.architectures.output.FlowInfo;
import RHODA.architectures.output.Metrics;

import java.util.Map;
import java.util.Set;

public class Scheduler {
  private DataCenter dataCenter;

  public Scheduler() {

  }

  public void init() {
    dataCenter = new DataCenter();
    TrafficPattern trafficPattern = new TrafficPattern(dataCenter);
    trafficPattern.importTraffic();
  }

  public void start() {
    InterRackTopologyConfiguration interRackTopology =
        new InterRackTopologyConfiguration(dataCenter);
    interRackTopology.buildInterRackTopology();
    Map<Integer, Set<Integer>> connections = interRackTopology.getConnections();

    ShortestPathAmongNodes shortestPath = new ShortestPathAmongNodes(connections);
    shortestPath.findShortestPath();
    Map<SrcDstPair, Path> paths = shortestPath.getPairPathMap();

    InterRackCommunication interRackCommunication =
        new InterRackCommunication(this, paths);
    interRackCommunication.interTx();
  }

  public DataCenter getDataCenter() {
    return dataCenter;
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
