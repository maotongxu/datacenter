package RHODA.architectures.fattree;

import RHODA.architectures.common.Configuration;
import RHODA.architectures.common.TrafficPattern;
import RHODA.architectures.output.Metrics;

public class Scheduler {
  public static Switch[][] sw_core;
  public static Switch[][] sw_agg;
  public static Switch[][] sw_edge;
  public static Host[][][] host;

  public static void start() {
    ConfigurationFT.setConfBasedOnMinNumOfRacks(Configuration.getInstance().getNumOfRacks());
    Configuration.getInstance().setNumOfRacks(ConfigurationFT.TOTAL_NUM_OF_RACKS);

    SchedulerInit.initScheduler();
    SchedulerInit.initSwitch();
    SchedulerInit.assignIP();

    double[][] flowMatrix = TrafficPattern.generateFlowMatrix();
    Routing.loadTrafficToHosts(flowMatrix);
    Routing.txPktUp();
    Routing.txPktDown();
    outputSwitchLoad();
  }

  public static void outputSwitchLoad() {
    int switchId = 0;
    for (int i = 0; i < ConfigurationFT.NUM_OF_CORE_GROUP; i++) {
      for (int j = 0; j < ConfigurationFT.NUM_OF_CORE_SWITCH_PER_GROUP; j++) {
        Metrics.rackIdRackLoadMap.put(switchId, sw_core[i][j].getLoadSW());
        switchId++;
      }
    }

    for (int i = 0; i < ConfigurationFT.NUM_OF_POD; i++) {
      for (int j = 0; j < ConfigurationFT.NUM_OF_AGG_SWITCH_PER_POD; j++) {
        Metrics.rackIdRackLoadMap.put(switchId, sw_agg[i][j].getLoadSW());
        switchId++;
      }
    }

    for (int i = 0; i < ConfigurationFT.NUM_OF_POD; i++) {
      for (int j = 0; j < ConfigurationFT.NUM_OF_EDGE_SWITCH_PER_POD; j++) {
        Metrics.rackIdRackLoadMap.put(switchId, sw_edge[i][j].getLoadSW());
        switchId++;
      }
    }
  }

  public static void main(String[] args) {
    Metrics.init();
    Scheduler scheduler = new Scheduler();
    scheduler.start();
    outputSwitchLoad();
    System.out.println("NumOfHops " + Metrics.calculateAvgNumOfHops() +
        " AvgRackLoad " + Metrics.calculateAvgRackLoad() +
        " TotalTraffic " + Metrics.calculateTotalTraffic());
  }
}
